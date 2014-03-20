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
    MulticastSocket MCBackupSock;                                                                                       /*Socket for backup  channel communications*/

    Hashtable<String,ThreadBackupSend> BackupThreads;                                                                   /*Each position stores a thread for backup*/
    Hashtable<String,Thread> RecoveryThreads;                                                                           /*Each position stores a thread for recovery*/
    Hashtable<String,Integer> StoreCounter;                                                                             /*Each position stores the number of STORED received for a "String" file_No*/

    Mutex storedCounterMutex;
    Mutex backupThreadMutex;                                                                                            /*regulates access to ""*/
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
        this.MCBackupSock.joinGroup(MCControlVIP);

        this.commands = new LinkedList<String>();                                                           /*Queue for saving commands for later execution*/
        this.commandQueueMutex = new Mutex();                                                               /*Mutex for locking commands Queue*/
        this.backupThreadMutex = new Mutex();

        this.BackupThreads      = new Hashtable<String,ThreadBackupSend>();                                 /*HashTable to save Backup Threads*/
        this.StoreCounter = new Hashtable<String, Integer>();                                               /*HashTable to save STORED message information*/

        this.storedCounterMutex = new Mutex();                                                              /*Mutex to regulate access to StoredCounterHash♀*/

        this.RecoveryThreads = new Hashtable<String,Thread>();                                              /*Vector to save Recovery Threads♀*/

        this.sentQueue = new LinkedList<String>();                                                          /*Saves the last command sent to the network♀*/
        this.sentQueue.add("");                                                                             /*Making funny things since ♀ 1995-Malucos do Riso ♂*/

        this.backupLog = new Vector<LogBackup>();

        try{
            InputStream file = new FileInputStream("BackupLogs.mna");
            InputStream buffer=new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            backupLog = (Vector<LogBackup>)input.readObject();
            input.close();
        }
        catch(IOException e){}
        catch(ClassNotFoundException e){}

    }
    void run()throws SocketException{
        this.MenuThread = new ThreadMenu(commandQueueMutex,commands);
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
        String[] controls = message.split(" ",2);

        if     (controls[0].equalsIgnoreCase("STORED")  ){
            String[] ctrls = message.split(" ",5);
            String id = ctrls[2];
            String chunkNo = ctrls[3];
            receivedStoreMessagesHandler(id, chunkNo);
        }
        else if(controls[0].equalsIgnoreCase("GETCHUNK")){System.out.println("GETCHUNK message detected by me! Uarray!");}
        else if(controls[0].equalsIgnoreCase("DELETE"))  {System.out.println("DELETE message detected by me! Nhe fraquinho!");}
        else if(controls[0].equalsIgnoreCase("REMOVED")) {System.out.println("REMOVED message detected by marisculino! Guga La");}
        else {System.out.println("Invalid");}

    }
    void backupReceiveThreadLaunch(){

    }

    void recoveryReceiveThreadLaunch(String fileID){
        /**/
    }
    void recoverySendThreadLaunch(String fileID){

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
    void checkForCommands(){
        commandQueueMutex.lock();
        if(commands.peek() == null || commands.isEmpty()){
            commandQueueMutex.unlock();
            return;
        }
        String currentCommand = commands.poll();

        if(currentCommand.equals("BACKUP")){
            String filename = commands.poll();
            backupSendThreadLaunch(filename);
        }
        else if(currentCommand.equals("RESTORE")){
            // Gera thread Restore
        }
        else if(currentCommand.equals("STORED")){

            String fileID = commands.poll();
            String chunkNo = commands.poll();
            String data = "STORED " + version + " " + fileID + " " + chunkNo + " \n \n";
            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,MCControlPort);
            try{MCControlSock.send(packet);}
            catch(IOException e){ }
        }
        commandQueueMutex.unlock();
    }

}
