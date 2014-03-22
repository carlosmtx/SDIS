import sun.awt.Mutex;

import java.io.*;
import java.net.*;

import java.util.*;
/**
 * Created by Sr. Lelo da Purificacao(Malucos do Riso desde 1995) on 26-02-2014.
 * Class Peer
 */
public class Peer {
    static public String version="1.0";
    static public int chunkSize=128;                                                                                    /*MaxSize of each chunk to be sent*/
    static public  int repDegree=3;
    static public boolean endProgram=false;
    InetAddress MCBackupVIP;                                                                                            /*Adress of multicast backUp channel*/
    InetAddress MCRecoveryVIP;                                                                                          /*Adress of multicast recovery channel*/
    InetAddress MCControlVIP;                                                                                           /*Adress of multicast control channel*/

    int MCControlPort;                                                                                                  /*Port used for control channel*/
    int MCRecoveryPort;                                                                                                 /*Port used for recovery channel*/
    int MCBackupPort;                                                                                                   /*Port used for backup channel*/

    MulticastSocket MCControlSock;                                                                                      /*Socket for control channel communications*/
    MulticastSocket MCRecoverySock;
    MulticastSocket MCBackupSock;                                                                                       /*Socket for backup  channel communications*/

    Mutex MCRestoreSockMutex;

    Hashtable<String,ThreadBackupSend> BackupThreads;                                                                   /*Each position stores a thread for backup*/
    Hashtable<String,ThreadRestoreSend> RecoveryThreads;                                                                /*Each position stores a thread for recovery*/
    Hashtable<String,ThreadRestoreReceive> RecoveryReceiveThreads;
    Hashtable<String,Integer> StoreCounter;                                                                             /*Each position stores the number of STORED received for a "String" file_No*/

    Mutex storedCounterMutex;
    Mutex backupThreadMutex;                                                                                            /*regulates access to ""*/
    Mutex recoveryThreadMutex;
    Mutex commandQueueMutex;                                                                                            /*regulates access to "commands" queue*/

    ThreadMenu     MenuThread;                                                                                          /*Thread to make menu for user*/
    ThreadBackupReceive BackupReceive;
    Queue<String> sentQueue;                                                                                            /*Queue to store command sent from commandThread */


    Queue<String> commands;

    Vector<LogBackup> backupLog;

    Peer(String MCControlIP,int MCControlPort,String MCRecoveryIP,int MCRecoveryPort,String MCBackupIP,int MCBackupPort)throws UnknownHostException,IOException{



        this.MCBackupVIP   = InetAddress.getByName(MCBackupIP);                                             /*Network IP for Backup communications*/
        this.MCRecoveryVIP = InetAddress.getByName(MCRecoveryIP);                                           /*Network IP for Recovery communications */
        this.MCControlVIP  = InetAddress.getByName(MCControlIP);                                            /*Network IP for Control communications*/

        this.MCControlPort = MCControlPort;                                                                 /*Port used for Control Communications*/
        this.MCRecoveryPort= MCRecoveryPort;                                                                /*Port used for Recovery Communications*/
        this.MCBackupPort  = MCBackupPort;                                                                  /*Port used for Backup Communications*/

        this.MCControlSock = new MulticastSocket(MCControlPort);                                            /*Socket used for Control communications*/
        this.MCControlSock.joinGroup(MCControlVIP);                                                         /*Joining Control Network */

        this.MCBackupSock  = new MulticastSocket(MCBackupPort);
        this.MCBackupSock.joinGroup(MCBackupVIP);

        this.MCRecoverySock  = new MulticastSocket(MCRecoveryPort);
        this.MCRecoverySock.joinGroup(MCRecoveryVIP);

        this.MCRestoreSockMutex = new Mutex();

        this.commands = new LinkedList<String>();                                                           /*Queue for saving commands for later execution*/
        this.commandQueueMutex = new Mutex();                                                               /*Mutex for locking commands Queue*/
        this.backupThreadMutex = new Mutex();

        this.BackupThreads      = new Hashtable<String,ThreadBackupSend>();                                 /*HashTable to save Backup Threads*/
        this.StoreCounter = new Hashtable<String, Integer>();                                               /*HashTable to save STORED message information*/

        this.storedCounterMutex = new Mutex();                                                              /*Mutex to regulate access to StoredCounterHash♀*/

        this.RecoveryThreads = new Hashtable<String,ThreadRestoreSend>();                                       /*Vector to save Recovery Threads♀*/
        this.recoveryThreadMutex = new Mutex();

        this.sentQueue = new LinkedList<String>();                                                          /*Saves the last command sent to the network♀*/
        this.sentQueue.add("");                                                                             /*Making funny things since ♀ 1995-Malucos do Riso ♂*/

        this.backupLog = new Vector<LogBackup>();

        try{

            ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream("BackupLogs.mna")));

            backupLog = (Vector<LogBackup>) in.readObject();


        }
        catch(IOException e){}
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }

    }
    void run()throws SocketException{
        this.MenuThread = new ThreadMenu(this);
        this.BackupReceive = new ThreadBackupReceive(this);
        new Thread(MenuThread).start();
        new Thread(BackupReceive).start();

        byte[] buff= new byte[1024];                                                                        /*Buffer for control data receiving*/
        String data;                                                                                        /*String that holds received messages*/
        DatagramPacket packet = new DatagramPacket(buff,buff.length);                                       /*Packet to send data*/


        do{
            try{
                MCControlSock.setSoTimeout(100);
                MCControlSock.receive(packet);
                data = new String(packet.getData(), packet.getOffset(), packet.getLength());
                if (data.substring(0,data.lastIndexOf('\n')+1).equals(sentQueue.peek())){}
                else{
                    controlThreadHandler(data);
                }
            }
            catch(SocketTimeoutException e){}
            catch(IOException e){}
            checkForCommands();
        }while(!endProgram);

        try{

            OutputStream file  = new FileOutputStream("BackupLogs.mna");
            OutputStream buffer= new BufferedOutputStream(file);
            ObjectOutput output= new ObjectOutputStream(buffer);
            output.writeObject(backupLog);
            output.close();

        }
        catch(IOException e){}


    }

    void checkForCommands(){
        commandQueueMutex.lock();
        if(commands.peek() == null || commands.isEmpty()){
            commandQueueMutex.unlock();
            return;
        }
        String currentCommand = commands.poll();
        //System.out.println("CURRENT COMMAND: " + currentCommand);
        if(currentCommand.equals("BACKUP")){
            String filename = commands.poll();
            backupSendThreadLaunch(filename);
        }
        else if(currentCommand.equals("RESTORE")){
            // Nao vai servir pa nada :D:D:D:D - Bom e barato só no barata :D:D:D:D
            // AFINAL VAI :D:D:D:
            System.out.println("A Escutar canal de Restore...");
            String fileid = commands.poll();
            recoveryReceiveThreadLaunch(fileid);

        }
        else if(currentCommand.equals("STORED")){

            String fileID = commands.poll();
            String chunkNo = commands.poll();
            String data = "STORED " + version + " " + fileID + " " + chunkNo + " \n \n";
            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,MCControlPort);
            try{MCControlSock.send(packet);}
            catch(IOException e){ }
        }
        else if(currentCommand.equals("GETCHUNK")){
            String fileID = commands.poll();
            String chunkNo = commands.poll();

            String data = "GETCHUNK " + version + " " + fileID + " " + chunkNo + " \n \n";
            //System.out.println("Enviei : " + data);
            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,MCControlPort);
            try{
                MCControlSock.send(packet);
                //System.out.println("Enviei no MCControl: '" + data + "'");
            }
            catch(IOException e){ }
        }
        else if(currentCommand.equals("DELETE")){
            String fileID = commands.poll();
            String data = "DELETE "+ fileID +" " + '\n'+" " + '\n';
            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,MCControlPort);
            try{
                MCControlSock.send(packet);
                System.out.println("Enviei no MCControl: '" + data + "'");
            }
            catch(IOException e){ }
        }
        commandQueueMutex.unlock();
    }



    void backupSendThreadLaunch(String fileID){
        ChunkedFile file = null;
        try{
            file = new ChunkedFile(fileID,chunkSize,repDegree);
            backupLog.add(new LogBackup(file));
            backupThreadMutex.lock();
            BackupThreads.put(file.getHash(), new ThreadBackupSend(this, file));
            backupThreadMutex.unlock();
            new Thread(BackupThreads.get(file.getHash())).start();
        }
        catch(SocketException e){}
    }


    void controlThreadHandler(String message){
        if (message == null){return;}
        String[] controls = message.split(" ",2);

        if     (controls[0].equalsIgnoreCase("STORED")  ){
            String[] ctrls = message.split(" ",5);
            String id = ctrls[2];
            String chunkNo = ctrls[3];
            receivedStoreMessagesHandler(id, chunkNo);
        }
        else if(controls[0].equalsIgnoreCase("GETCHUNK")){
            String[] ctrls = message.split(" ", 5);

            //System.out.println("DETETEI GET CHUNK");
            String id = ctrls[2];
            String chunkNo = ctrls[3];
            receivedGetChunkMessagesHandler(id, chunkNo);
        }
        else if(controls[0].equalsIgnoreCase("DELETE"))  {deleteMessageHandler(controls[1]);}
        else if(controls[0].equalsIgnoreCase("REMOVED")) {System.out.println("REMOVED message detected by marisculino! Guga La");}
        else {System.out.println("Invalid");}

    }

    void deleteMessageHandler(String cmd){
        if (cmd == null){return;}
        String[] msg = cmd.split(" ",2);
        System.out.println(Arrays.toString(msg));
        deleteThreadLaunch(msg[0]);
    }
    void receivedStoreMessagesHandler(String fileID, String chunkNo){
        if(BackupThreads.get(fileID) != null){
            try{
                int[] aux = BackupThreads.get(fileID).chunksStored;
                int posChunk = Integer.parseInt(chunkNo);
                aux[posChunk]++;
            }catch(Exception e){ e.printStackTrace(); }
        }
        else{
            storedCounterMutex.lock();
            if(StoreCounter.get(fileID+chunkNo) == null){
                StoreCounter.put(fileID+chunkNo,1);
            }
            else{
                StoreCounter.put(fileID+chunkNo,StoreCounter.get(fileID+chunkNo)+1);
            }
            storedCounterMutex.unlock();
        }


    }
    void receivedGetChunkMessagesHandler(String fileID, String chunkNo){
        /*
            Verificicacao se pedido vem do proprio pc
         */

        String path = "./BackupFiles/"+ fileID+"_"+chunkNo+".mdr";
        File chunk = new File(path);

        if(!chunk.exists()){
            //System.out.println("Nao tem o chunk pedido");
        }
        else{
            // Enviar chunk pelo MCRestore - Criar thread para o chunk atual
            recoverySendThreadLaunch(fileID, chunkNo, path);

        }
    }

    void deleteThreadLaunch(String fileID){

        new Thread(new ThreadDelete(fileID)).start();
    }
    void recoverySendThreadLaunch(String filename, String chunkNo, String path){
        try{
            //  Mutex sobre hash das threads recovery
            recoveryThreadMutex.lock();
            RecoveryThreads.put(filename, new ThreadRestoreSend(this, filename, chunkNo, path) );
            recoveryThreadMutex.unlock();

            new Thread(RecoveryThreads.get(filename)).start();

            // Abre thread que lê entradas no canal Restore (chunks recebidos)


        }
        catch(IOException e){

        }
    }
    void recoveryReceiveThreadLaunch(String fileid){
        new Thread(new ThreadRestoreReceive(this, fileid)).start();
    }

}
