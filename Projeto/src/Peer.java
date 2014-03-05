import sun.awt.Mutex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by Sr. Lelo da Purificacao(Malucos do Riso desde 1995) on 26-02-2014.
 * Class Peer
 */
public class Peer {


    InetAddress MCBackupVIP;
    InetAddress MCRecoveryVIP;
    InetAddress MCControlVIP;

    MulticastSocket MCControlSock;

    Vector<Thread> BackupThreads;
    Vector<Thread> RecoveryThreads;
    ThreadMenu     MenuThread;
    Stack<String> sentStack;

    Mutex commandStackMutex;
    Stack<String> commands;

    Peer(String MCControlIP,String MCRecoveryIP,String MCBackupIP)throws UnknownHostException,IOException{
        int port = 2000;
        this.MCBackupVIP   = InetAddress.getByName(MCBackupIP);                                             /*Network IP for Backup communications*/
        this.MCRecoveryVIP = InetAddress.getByName(MCRecoveryIP);                                           /*Network IP for Recovery communications */
        this.MCControlVIP  = InetAddress.getByName(MCControlIP);                                            /*Network IP for Control communications*/

        this.MCControlSock = new MulticastSocket(port);                                                     /*Socket used for Control communications*/
        this.MCControlSock.joinGroup(MCControlVIP);                                                         /*Joining Control Network */

        this.commands = new Stack<String>();                                                                /*Stack for saving commands for later execution*/
        this.commandStackMutex = new Mutex();                                                               /*Mutex for locking commands Stack*/

        this.BackupThreads   = new Vector<Thread>();                                                        /*Vector to save Backup Threads*/
        this.RecoveryThreads = new Vector<Thread>();                                                        /*Vector to save Recovery Threads*/

        this.sentStack = new Stack<String>();                                                               /*Saves the last command sent to the network*/
        this.sentStack.push("");                                                                            /*Making funny things since 1995-Malucos do Riso*/
        this.MenuThread = new ThreadMenu(commandStackMutex,commands);
        this.MenuThread.run();
    }
    void run()throws SocketException{
        byte[] buff= new byte[1024];                                                                        /*Buffer for control data receiving*/
        String data;                                                                                        /*String that holds received messages*/
        DatagramPacket packet = new DatagramPacket(buff,buff.length);                                       /*Packet to send data*/
        try{
            do{
                //MCControlSock.setSoTimeout(1000);
                MCControlSock.receive(packet);
                data = new String(packet.getData(), packet.getOffset(), packet.getLength());
                if (data.substring(0,data.lastIndexOf('\n')+1).equals(sentStack.peek())){}
                else{controlThreadHandler(data);}
            }while(true);
        }
        catch(IOException e){

        }
    }
    void backupThreadLaunch(String fileID){

    }
    void recoveryThreadLaunch(String fileID){

    }
    void controlThreadHandler(String message){
     String[] controls = message.split("\\s");
     System.out.println(Arrays.toString(controls));
     if     (controls[0].equalsIgnoreCase("STORED")  ){System.out.println("STORED message detected by me! Uarray!");}
     else if(controls[0].equalsIgnoreCase("GETCHUNK")){System.out.println("GETCHUNK message detected by me! Uarray!");}
     else if(controls[0].equalsIgnoreCase("DELETE"))  {System.out.println("DELETE message detected by me! Nhe fraquinho!");}
     else if(controls[0].equalsIgnoreCase("REMOVED")) {System.out.println("REMOVED message detected by marisculino! Guga La");}
     else {System.out.println("Invalid");}

    }


}
