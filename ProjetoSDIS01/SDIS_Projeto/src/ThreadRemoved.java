import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by Papa Formigas on 29-03-2014.
 */
public class ThreadRemoved implements Runnable {
    boolean send;
    MulticastSocket MCBackupSock;
    String fileID;
    String chunkNo;
    Hashtable<String,ThreadRemoved> removedTable;
    ThreadRemoved(Peer peer,String fileID,String chunkNo){
        try{
        this.MCBackupSock = new MulticastSocket(peer.MCBackupPort);                                       /*Socket used for Control communications*/
        this.MCBackupSock.joinGroup(peer.MCBackupVIP);                                                    /*Joining Control Network */
        }
        catch (IOException e){
            e.printStackTrace();
        }
        send = false;
    }
    public void run(){
        byte[] buffer = new byte[200+Peer.chunkSize];
        int timeToWait = (new Random().nextInt(400)+1);
        long lastTime = System.currentTimeMillis();
        while(!Peer.endProgram && timeToWait > 0){
            try{

                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                MCBackupSock.setSoTimeout(timeToWait);
                MCBackupSock.receive(packet);
                timeToWait -=System.currentTimeMillis()-lastTime;
                packetHandler(packet);
            }
            catch (SocketException e){timeToWait -=System.currentTimeMillis()-lastTime;}
            catch (IOException e){timeToWait -=System.currentTimeMillis()-lastTime;}
        }

    }
    void getChunk(){

    }
    void packetHandler(DatagramPacket packet){

    }
}
