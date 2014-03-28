import sun.awt.Mutex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Hashtable;

/**
 * Created by Leonel Araujo on 28-03-2014.
 */
public class ThreadRestoreMonitor implements Runnable{
    DatagramSocket MCRestoreSock;                                                                                         /*Reference to the Multicast Backup Socket in Peer*/
    InetAddress MCRestoreAddress;                                                                                         /*Adress of the Mulsticast Backup Channel*/
    Mutex MCRestoreSockMutex;
    int MCRestorePort;

    Hashtable<String, Integer> ReceivedChunkCounter;
    Mutex receivedChunkCounterMutex;

    ThreadRestoreMonitor(Peer p){
        this.MCRestoreSock = p.RestoreChannelMonitor;
        this.MCRestoreAddress = p.MCRecoveryVIP;
        this.MCRestoreSockMutex = p.MCRestoreSockMutex;
        this.MCRestorePort = p.MCRecoveryPort;
        this.ReceivedChunkCounter = p.ReceivedChunkCounter;
        this.receivedChunkCounterMutex = p.receivedChunkCounterMutex;

    }

    public void run(){

        byte[] buff = new byte[200+Peer.chunkSize];
        DatagramPacket packet = new DatagramPacket(buff,buff.length);
        while(true){
            try{
                MCRestoreSock.receive(packet);
                refreshMonitorTable(packet);
            }
            catch(IOException e){}
        }
    }

    private void refreshMonitorTable(DatagramPacket packet){
        //CHUNK <Version> <FileId> <ChunkNo><CRLF> <CRLF> <Body>

        ByteString rec = new ByteString(packet.getData());
        rec = rec.substring(0,packet.getLength());

        ByteString[] receivedMessage = rec.split((byte)' ', 4);

        ByteString receivedID  = receivedMessage[2];
        ByteString rest = receivedMessage[3];
        ByteString[] aux = rest.split((byte)'\n', 3);

        ByteString chunkNo = aux[0];
        chunkNo = chunkNo.substring(0,chunkNo.length()-1);

        String id = new String(receivedID.getBytes());
        String no = new String(chunkNo.getBytes());
        String entry  = new String(id + "_" + no);


        receivedChunkCounterMutex.lock();
        if(ReceivedChunkCounter.get(entry) == null){
            ReceivedChunkCounter.put(entry,0);
        }
        ReceivedChunkCounter.put(entry, ReceivedChunkCounter.get(entry)+1);
        receivedChunkCounterMutex.unlock();

        //System.out.println("[TRM] Atualizei Hash com " + (new String (chunkNo.getBytes())));
    }
}
