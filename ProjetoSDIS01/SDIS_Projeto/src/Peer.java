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
    static public int chunkSize=100;                                                  /*MaxSize of each chunk to be sent*/

    static public  int repDegree=3;
    static public boolean endProgram=false;
    InetAddress MCBackupVIP;                                                          /*Adress of multicast backUp channel*/
    InetAddress MCRecoveryVIP;                                                        /*Adress of multicast recovery channel*/
    InetAddress MCControlVIP;                                                         /*Adress of multicast control channel*/

    int MCControlPort;                                                                /*Port used for control channel*/
    int MCRecoveryPort;                                                               /*Port used for recovery channel*/
    int MCBackupPort;                                                                 /*Port used for backup channel*/

    MulticastSocket MCControlSock;                                                    /*Socket for control channel communications*/
    MulticastSocket MCRecoverySock;
    MulticastSocket MCBackupSock;                                                     /*Socket for backup  channel communications*/

    MulticastSocket RestoreChannelMonitor;

    Mutex MCRestoreSockMutex;

    Hashtable<String,ThreadBackupSend> BackupThreads;                                 /*Each position stores a thread for backup.*/
    Hashtable<String,ThreadRestoreSend> RecoveryThreads;                              /*Each position stores a thread for recovery*/
    Hashtable<String,Integer> StoreCounter;                                           /*Each position stores the number of STORED received for a "String" file_No*/

    Hashtable<String, Integer> ReceivedChunkCounter;
    Vector<String> DeleteRecords;

    Hashtable<String, Integer> ChunksControl;                                          /* Saves info regarding the PUTCHUNK controls received */

    Mutex storedCounterMutex;
    Mutex backupThreadMutex;                                                          /*Regulates access to */
    Mutex recoveryThreadMutex;
    Mutex commandQueueMutex;                                                          /*Regulates access to "commands" queue*/
    Mutex receivedChunkCounterMutex;
    Mutex deletedCounterMutex;
    Mutex chunksControlMutex;

    ThreadMenu     MenuThread;                                                        /*Thread to make menu for user*/
    ThreadBackupReceive BackupReceive;

    Queue<String> sentQueue;                                                          /*Queue to store command sent from commandThread */

    Queue<String> commands;                                                           /*Queue that stores all the commands from other threads*/

    Vector<LogBackup> backupLog;                                                      /*Keeps a registry of the backup logs*/

    Vector<Log> logs;                                                                 /*Keeps a record of all the messages received and actions taken by the program*/
    Mutex logMutex;                                                                   /*Regulates the access to logs*/

    Peer(String MCControlIP,int MCControlPort,String MCRecoveryIP,int MCRecoveryPort,String MCBackupIP,int MCBackupPort)throws UnknownHostException,IOException{



        this.MCBackupVIP   = InetAddress.getByName(MCBackupIP);                                             /*Network IP for Backup communications*/
        this.MCRecoveryVIP = InetAddress.getByName(MCRecoveryIP);                                           /*Network IP for Recovery communications */
        this.MCControlVIP  = InetAddress.getByName(MCControlIP);                                            /*Network IP for Control communications*/

        this.MCControlPort = MCControlPort;                                                                 /*Port used for Control Communications*/
        this.MCRecoveryPort= MCRecoveryPort;                                                                /*Port used for Recovery Communications*/
        this.MCBackupPort  = MCBackupPort;                                                                  /*Port used for Backup Communications*/

        this.MCControlSock = new MulticastSocket(MCControlPort);                                            /*Socket used for Control communications*/
        this.MCControlSock.joinGroup(MCControlVIP);                                                         /*Joining Control Network */

        this.MCBackupSock  = new MulticastSocket(MCBackupPort);                                             /*Socket for backup communications*/
        this.MCBackupSock.joinGroup(MCBackupVIP);                                                           /*Joining the multicast group*/

        this.MCRecoverySock  = new MulticastSocket(MCRecoveryPort);                                         /*Multicast Recovery socketServ */
        this.MCRecoverySock.joinGroup(MCRecoveryVIP);                                                       /*Joining recovery multicast group*/

        this.RestoreChannelMonitor  = new MulticastSocket(MCRecoveryPort);                                  /*Multicast Recovery socketServ */
        this.RestoreChannelMonitor.joinGroup(MCRecoveryVIP);                                                /*Joining recovery multicast group*/


        this.MCRestoreSockMutex = new Mutex();                                                              /*Initializing socketServ for restore socketServ access*/

        this.commands = new LinkedList<String>();                                                           /*Queue for saving commands for later execution*/
        this.commandQueueMutex = new Mutex();                                                               /*Mutex for locking commands Queue*/
        this.backupThreadMutex = new Mutex();

        this.BackupThreads      = new Hashtable<String,ThreadBackupSend>();                                 /*HashTable to save Backup Threads*/
        this.StoreCounter = new Hashtable<String, Integer>();                                               /*HashTable to save STORED message information*/
        this.ReceivedChunkCounter = new Hashtable<String, Integer>();                                       /*HashTable to save CHUNK message in Restore Channel information*/
        this.DeleteRecords = new Vector<String>();
        this.ChunksControl = new Hashtable<String, Integer>();

        this.storedCounterMutex = new Mutex();                                                              /*Mutex to regulate access to StoredCounterHash♀*/
        this.receivedChunkCounterMutex = new Mutex();                                                        /*Mutex to regulate access to ReceivedChunkCounter*/
        this.deletedCounterMutex = new Mutex();
        this.chunksControlMutex = new Mutex();

        this.RecoveryThreads = new Hashtable<String,ThreadRestoreSend>();                                    /*Vector to save Recovery Threads♀*/
        this.recoveryThreadMutex = new Mutex();

        this.sentQueue = new LinkedList<String>();                                                          /*Saves the last command sent to the network♀*/
        this.sentQueue.add("");                                                                             /*Making funny things since ♀ 1995-Malucos do Riso ♂*/

        this.backupLog = new Vector<LogBackup>();

        try{                                                                                                /*Importing logs */

            ObjectInputStream in = new ObjectInputStream(
            new BufferedInputStream(new FileInputStream("Data/BackupLogs.mna")));
            backupLog = (Vector<LogBackup>) in.readObject();

            in = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream("Data/Delete.mna")));
            DeleteRecords = (Vector<String>) in.readObject();
        }
        catch(IOException e){}
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }

        this.logMutex = new Mutex();
        this.logs = new Vector<Log>();

    }
    void run()throws SocketException{
        this.MenuThread = new ThreadMenu(this);
        this.BackupReceive = new ThreadBackupReceive(this);
        ThreadRestoreMonitor ts = new ThreadRestoreMonitor(this);

        new Thread(MenuThread).start();
        new Thread(BackupReceive).start();
        new Thread(ts).start();


        byte[] buff= new byte[1024];                                                                        /*Buffer for control data receiving*/
        String data;                                                                                        /*String that holds received messages*/
        DatagramPacket packet = new DatagramPacket(buff,buff.length);                                       /*Packet to send data*/


        do{
            try{
                MCControlSock.setSoTimeout(100);                                                            /*Every 100ms read is unblocked*/
                MCControlSock.receive(packet);                                                              /*Blocking and waiting for packets*/
                data = new String(packet.getData(), packet.getOffset(), packet.getLength());                /*handling the packet*/
                if (data.substring(0,data.lastIndexOf('\n')+1).equals(sentQueue.peek())){}
                else{
                    controlThreadHandler(data,packet);                                                      /*Interpretation of the packet is made by control...handler */
                }
            }
            catch(SocketTimeoutException e){}                                                               /*A timeout occurred.It's ok...you may continue running ;)*/
            catch(IOException e){}
            checkForCommands();                                                                             /*Better check if some thread needs some help*/
        }while(!endProgram);                                                                                /*Should we end the program??*/

                                                                                                            /*Saving everything ()*/

        try{

            OutputStream file  = new FileOutputStream("Data/BackupLogs.mna");
            OutputStream buffer= new BufferedOutputStream(file);
            ObjectOutput output= new ObjectOutputStream(buffer);
            output.writeObject(backupLog);
            output.close();

            file  = new FileOutputStream("Data/Delete.mna");
            buffer= new BufferedOutputStream(file);
            output= new ObjectOutputStream(buffer);
            output.writeObject(DeleteRecords);
            output.close();


        }
        catch(IOException e){}


    }

    void checkForCommands(){                                                                            /*Checks if any thread needs something to be sent*/
        commandQueueMutex.lock();                                                                       /*Locking the queue, we need exclusive access */
        if(commands.peek() == null || commands.isEmpty()){                                              /*There is nothing to do.*/
            commandQueueMutex.unlock();                                                                 /*Clean and leave*/
            return;
        }
        String currentCommand = commands.poll();                                                        /*Let's see the first command*/

        if(currentCommand.equals("BACKUP")){                                                            /*The user needs our help to backup a file(thanks menu thread for the warning)*/
            String filename = commands.poll();                                                          /*The user needs to backup the file:fileName*/
            backupSendThreadLaunch(filename);                                                           /*Better send someone. John... plz summon a backupThread*/
        }
        else if(currentCommand.equals("RESTORE")){                                                      /*The user!!! OH My God he needs to restore a file*/
            String fileid = commands.poll();                                                             /*What file does he want? Oh...Look...A fileID.  JOHN!!*/
            recoveryReceiveThreadLaunch(fileid);                                                         /*John plz summon a restore thread*/
            // Nao vai servir pa nada :D:D:D:D - Bom e barato só no barata :D:D:D:D
            // AFINAL VAI :D:D:D
        }
        else if(currentCommand.equals("STORED")){                                                       /*William someone needs us to inform that we stored something. This is great*/

            String fileID = commands.poll();                                                            /*Let's see what the fileID is*/
            String chunkNo = commands.poll();                                                           /*He probabily sent us the chunkNo*/
            String data = "STORED " + version + " " + fileID + " " + chunkNo + " \n \n";                /*Let's deliver the message*/
            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,MCControlPort);   /*Just putting it in the envelope*/
            try{MCControlSock.send(packet);}                                                            /*William watch how fast it goes! Perfect ^^ */
            catch(IOException e){ }
        }
        else if(currentCommand.equals("GETCHUNK")){                                                     /*Leonel look...someone needs a chunk... Let's help*/
            String fileID = commands.poll();                                                            /*Let's get the fileID*/
            String chunkNo = commands.poll();                                                           /*RestoreThreads are such visionary threads! They gave us the chunk id*/

            String data = "GETCHUNK " + version + " " + fileID + " " + chunkNo + "\r\n\r\n";              /*Construct the message*/
            ByteString d = new ByteString(data.getBytes());

            DatagramPacket packet = new DatagramPacket(d.getBytes(),d.length(),MCControlVIP,MCControlPort);/*In the envelope*/
            try{
                MCControlSock.send(packet);                                                             /*ULELELE... SEND*/
            }
            catch(IOException e){ }
        }
        else if(currentCommand.equals("DELETE")){                                                       /**/
            String fileID = commands.poll();
            String data = "DELETE "+ fileID + '\r' + '\n'+ '\r' + '\n';
            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,MCControlPort);
            try{
                MCControlSock.send(packet);
                System.out.println("Enviei no MCControl: '" + data + "'");
            }
            catch(IOException e){ }
        }
        else if(currentCommand.equals("RECLAIM")){
            long size = Integer.parseInt(commands.poll());
            new Thread(new ThreadReclaimer(this,size)).start();
        }
        else if(currentCommand.equals("REMOVED")){
           String fileID = commands.poll();
           String chunkNo=commands.poll();
           String data = "REMOVED "+"1.0 "+fileID+" "+chunkNo.replace(".mdr","")+"\r\n\r\n";
           DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,MCControlPort);
            try{
                MCControlSock.send(packet);
                System.out.println("Enviei no MCControl: '" + data + "");
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

    void controlThreadHandler(String message,DatagramPacket packet){
        if (message == null){return;}
        String[] controls = message.split(" ",2);
        if(controls.length < 2){return;}

        if     (controls[0].equalsIgnoreCase("STORED")  ){
            String[] ctrls = message.split(" ",5);
            if(ctrls.length < 4){return;}
            String id = ctrls[2];
            String chunkNo = ctrls[3];
            receivedStoreMessagesHandler(id, chunkNo);
            logMutex.lock();
            logs.add(new Log(Log.STORED,Log.IN,System.currentTimeMillis(),id,Integer.parseInt(chunkNo),packet.getAddress()));
            logMutex.unlock();
        }
        else if(controls[0].equalsIgnoreCase("GETCHUNK")){
            String[] ctrls = message.split(" ", 5);
            String id = ctrls[2];
            String chunkNo = ctrls[3];
            chunkNo = chunkNo.substring(0, chunkNo.length()-4);
            receivedGetChunkMessagesHandler(id, chunkNo);
            logMutex.lock();
            logs.add(new Log(Log.STORED,Log.IN,System.currentTimeMillis(),id,Integer.parseInt(chunkNo)));
            logMutex.unlock();
        }
        else if(controls[0].equalsIgnoreCase("DELETE"))  {
            deleteMessageHandler(controls[1]);
            logMutex.lock();
            logs.add(new Log(Log.DELETE,Log.IN,System.currentTimeMillis(),controls[1]));
            logMutex.unlock();
        }
        else if(controls[0].equalsIgnoreCase("REMOVED")) {
            removedMessageHandler(controls[1]);
        }
        else {System.out.println("Invalid");}

    }
    void removedMessageHandler(String cmd){
        String[] ctrl_a =  cmd.split(" ",3);
        if(ctrl_a.length < 3){return;}
        String[] ctrl_b = ctrl_a[2].split("\r",2);
        String fileID  = ctrl_a[1];
        String chunkNo= ctrl_b[0];

        System.out.println("FileID: "+ fileID);
        System.out.println("ChunkNo: "+chunkNo);
    }
    void deleteMessageHandler(String cmd){
        if (cmd == null){return;}
        String[] msg = cmd.split("\r",2);

        DeleteRecords.add(msg[0]);
        System.out.println(Arrays.toString(DeleteRecords.toArray()));
        deleteThreadLaunch(msg[0]);
        logMutex.lock();
        logs.add(new Log(Log.DELETE,Log.IN, System.currentTimeMillis(),msg[0]));
        logMutex.unlock();
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
        logMutex.lock();
        logs.add(new Log(Log.STORED,Log.IN,System.currentTimeMillis(),fileID,Integer.parseInt(chunkNo)));
        logMutex.unlock();


    }
    void receivedGetChunkMessagesHandler(String fileID, String chunkNo){
        /*
            Verificicacao se pedido vem do proprio pc
         */

        String path = "./BackupFiles/"+ fileID+ "/"+ chunkNo + ".mdr";
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

        new Thread(new ThreadDelete(this,fileID)).start();
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
