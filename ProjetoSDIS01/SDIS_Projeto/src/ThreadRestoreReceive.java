import sun.awt.Mutex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Leonel da Purificacao on 21-03-1995.
 */
public class ThreadRestoreReceive implements Runnable {
    DatagramSocket MCRestoreSock;                                                                                         /*Reference to the Multicast Backup Socket in Peer*/
    InetAddress MCRestoreAddress;                                                                                         /*Adress of the Mulsticast Backup Channel*/
    Mutex MCRestoreSockMutex;
    int MCRestorePort;

    int packetReceivedSize = 1024;

    ArrayList<Boolean> positionCheck;
    String[] chunksStored;

    String fileid;
    long numberOfChunks;

    ThreadRestoreReceive(Peer p, String fileid){
        this.MCRestoreSock = p.MCRecoverySock;
        this.MCRestoreAddress = p.MCRecoveryVIP;
        this.MCRestoreSockMutex = p.MCRestoreSockMutex;
        this.MCRestorePort = p.MCRecoveryPort;
        this.fileid = fileid;

        for(int i = 0; i < p.backupLog.size(); i++){
            if(p.backupLog.get(i).hashName == fileid){
                this.numberOfChunks = p.backupLog.get(i).noChunks;
            }
        }

        positionCheck = new ArrayList<Boolean>();
        chunksStored = new String[(int)numberOfChunks];

    }

    public void run(){

        while(!Peer.endProgram){
            byte[] buff = new byte[packetReceivedSize];
            DatagramPacket packet = new DatagramPacket(buff,buff.length);
            try{
                MCRestoreSock.setSoTimeout(50);
                MCRestoreSock.receive(packet);
                if(packetHandler(packet)){
                    System.out.println("Ja recebeu todos os chunks");
                    break;
                }
                else{
                    System.out.println("Ainda nao acabou por isso vou continuar a escutar");
                }
            }
            catch(SocketTimeoutException e){}
            catch(IOException e){}

        }

        System.out.println("Terminei escuta - passar po ficheiro");
        System.out.println(Arrays.toString(chunksStored));



    }

    boolean packetHandler(DatagramPacket packet){
        String rec = new String(packet.getData());
        String[] receivedMessage = rec.split(" ",5);
        String receivedID  = receivedMessage[2];
        String chunkNo = receivedMessage[3];
        String content = receivedMessage[4];

        System.out.println("Recebi chunk: " + chunkNo);
        if(receivedID.equals(this.fileid)){
            // Ficheiro da Thread criada
            int no = Integer.parseInt(chunkNo);

            if(chunksStored[no] == null){
                System.out.println("Ainda nao tinha este cromo");
                chunksStored[no] = content;
                positionCheck.add(true);
            }
            else{
                System.out.println("Desculpa ja tinha oops");
            }

        }

        return isTransferComplete();
    }

    boolean isTransferComplete(){
        return this.positionCheck.size() == numberOfChunks;
    }
}