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
        DatagramPacket packet1 = null;

        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        String data = null;
        try{
            while(true){
                System.out.println("Insira comando a enviar:");
                data=read.readLine();
                data+='\n';
                sentStack.push(data);
                packet1 = new DatagramPacket(data.getBytes(),data.length(),MCControlVIP,2000);
                MCControlSock.send(packet1);
            }
        }
        catch(IOException e){
        }
        System.out.println(data);
    }
}
