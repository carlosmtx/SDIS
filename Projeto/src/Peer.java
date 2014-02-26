import java.io.IOException;
import java.net.*;
import java.util.Random;
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

    Peer(String MCControlIP,String MCRecoveryIP,String MCBackupIP)throws UnknownHostException,IOException{
        this.MCBackupVIP   = InetAddress.getByName(MCBackupIP);
        this.MCRecoveryVIP = InetAddress.getByName(MCRecoveryIP);
        this.MCControlVIP  = InetAddress.getByName(MCControlIP);

        MCControlSock = new MulticastSocket(5000);
        MCControlSock.joinGroup(MCControlVIP);

    }
    void activate()throws SocketException{
        byte[] buff = new String("Hello Everybody").getBytes();
        byte[] buff2= new byte[1024];

        DatagramPacket packet1 = new DatagramPacket(buff ,buff.length,MCControlVIP,4446);
        DatagramPacket packet2 = new DatagramPacket(buff2,buff2.length);

        System.out.println("Local Port:"+MCControlSock.getLocalPort());
        System.out.println("Loopback M:"+MCControlSock.getLoopbackMode());
        System.out.println("BroadCast :"+MCControlSock.getBroadcast());
        System.out.println(new String(buff));
        try{

            MCControlSock.send(packet1);
            MCControlSock.receive(packet2);
            System.out.println(packet2.getData());
        }
        catch(Exception e){
        }
    }
    void backupThreadLaunch(String fileID){

    }
    void recoveryThreadLaunch(String fileID){

    }



}
