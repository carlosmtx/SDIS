import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by Papa Formigas on 26-02-2014.
 * Class Peer
 */
public class Peer {


    InetAddress MCBackupVIP;
    InetAddress MCRecoveryVIP;
    InetAddress MCControlVIP;

    MulticastSocket MCControlSock;

    Vector<Thread> BackupThreads;
    Vector<Thread> RecoveryThreads;

    Stack<String> sentStack;

    Peer(String MCControlIP,String MCRecoveryIP,String MCBackupIP)throws UnknownHostException,IOException{
        int port = 2000;
        this.MCBackupVIP   = InetAddress.getByName(MCBackupIP);
        this.MCRecoveryVIP = InetAddress.getByName(MCRecoveryIP);
        this.MCControlVIP  = InetAddress.getByName(MCControlIP);

        MCControlSock = new MulticastSocket(port);
        MCControlSock.joinGroup(MCControlVIP);

        sentStack = new Stack<String>();

    }
    void activate()throws SocketException{
        byte[] buff = new String("Hello Everybody").getBytes();
        byte[] buff2= new byte[1024];

        DatagramPacket packet1 = null;
        DatagramPacket packet2 = new DatagramPacket(buff2,buff2.length);

        System.out.println("Local Port:"+MCControlSock.getLocalPort());
        System.out.println("Loopback M:"+MCControlSock.getLoopbackMode());
        System.out.println("BroadCast :"+MCControlSock.getBroadcast());
        System.out.println(new String(buff));
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        String data = null;
        try{
            data=read.readLine();
            data+='\n';
            sentStack.push(data);
            packet1 = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,2000);
            MCControlSock.send(packet1);
            do{
                MCControlSock.receive(packet2);
                data = new String(packet2.getData());
               if (data.substring(0,data.lastIndexOf('\n')+1).equals(sentStack.peek())){}
               else{}
            }while(true);
        }
        catch(IOException e){
        }
        System.out.println(data);
    }
    void backupThreadLaunch(String fileID){

    }
    void recoveryThreadLaunch(String fileID){

    }
    void controlThreadHandler(String message){
     String[] controls = message.split("\\s");
    }


}
