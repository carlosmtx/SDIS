import sun.awt.Mutex;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * Created by Sr. Lelo da Purificacao(Malucos do Riso desde 1995) on 26-02-2014.
 * Class Peer
 */
public class Peer {


    InetAddress MCBackupVIP;
    InetAddress MCRecoveryVIP;
    InetAddress MCControlVIP;

    int MCControlPort;
    int MCRecoveryPort;
    int MCBackupPort;

    MulticastSocket MCControlSock;
    MulticastSocket MCBackupSock;

    Hashtable<String,Thread> BackupThreads;
    Vector<Thread> RecoveryThreads;
    ThreadMenu     MenuThread;
    Queue<String> sentQueue;

    Mutex commandQueueMutex;
    Mutex backupThreadMutex;

    Queue<String> commands;

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
        this.commandQueueMutex = new Mutex();                                                              /*Mutex for locking commands Queue*/
        this.backupThreadMutex = new Mutex();

        this.BackupThreads   = new Vector<Thread>();                                                        /*Vector to save Backup Threads*/
        this.RecoveryThreads = new Vector<Thread>();                                                        /*Vector to save Recovery Threads*/

        this.sentQueue = new LinkedList<String>();                                                         /*Saves the last command sent to the network*/
        this.sentQueue.add("");                                                                            /*Making funny things since 1995-Malucos do Riso*/

    }
    void run()throws SocketException{
        byte[] buff= new byte[1024];                                                                        /*Buffer for control data receiving*/
        String data;                                                                                        /*String that holds received messages*/
        DatagramPacket packet = new DatagramPacket(buff,buff.length);                                       /*Packet to send data*/
        this.MenuThread = new ThreadMenu(commandQueueMutex,commands);
        new Thread(MenuThread).start();
        do{
            try{

                MCControlSock.setSoTimeout(100);
                MCControlSock.receive(packet);
                data = new String(packet.getData(), packet.getOffset(), packet.getLength());
                if (data.substring(0,data.lastIndexOf('\n')+1).equals(sentQueue.peek())){}
                else{controlThreadHandler(data);}
            }
            catch(SocketTimeoutException e){
                checkForCommands();
            }
            catch(IOException e){}
        }while(true);
    }
    void backupThreadLaunch(String fileID){
        try{
            BackupThreads.put("",new Thread(new ThreadBackup(MCBackupSock,MCBackupVIP,fileID,backupThreadMutex)));
            BackupThreads.get("").start();
        }
        catch(SocketException e){}
    }
    void recoveryThreadLaunch(String fileID){

    }
    void controlThreadHandler(String message){
        String[] controls = message.split(" ",2);
        System.out.println(Arrays.toString(controls));
        if     (controls[0].equalsIgnoreCase("STORED")  ){System.out.println("STORED message detected by me! Uarray!");}
        else if(controls[0].equalsIgnoreCase("GETCHUNK")){System.out.println("GETCHUNK message detected by me! Uarray!");}
        else if(controls[0].equalsIgnoreCase("DELETE"))  {System.out.println("DELETE message detected by me! Nhe fraquinho!");}
        else if(controls[0].equalsIgnoreCase("REMOVED")) {System.out.println("REMOVED message detected by marisculino! Guga La");}
        else {System.out.println("Invalid");}

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
            System.out.println("Vou fazer backup do ficheiro " + filename);

            backupThreadLaunch(filename);
        }
        else if(currentCommand.equals("RESTORE")){
            // Gera thread Restore
        }
        commandQueueMutex.unlock();
    }

}
