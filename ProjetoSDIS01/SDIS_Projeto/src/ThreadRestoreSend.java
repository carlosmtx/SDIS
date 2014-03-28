import sun.awt.Mutex;


import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Random;

/**
 * Created by Leonel Araujo on 20-03-2014.
 *
 */

public class ThreadRestoreSend implements Runnable{

    DatagramSocket MCRestore;                                                                                            /*Reference to the Multicast Backup Socket in Peer*/
    InetAddress MCRestoreAddress;
    Mutex MCRestoreSockMutex;
    int MCRestorePort;

    String fileID;
    String pathChunk;
    String chunkNo;
    ByteString dataToSend;

    Hashtable<String, Integer> ReceivedChunkCounter;

    public ThreadRestoreSend(Peer p, String fileHash, String chunkNo, String path)throws SocketException {

        this.MCRestore = p.MCRecoverySock;
        this.MCRestoreAddress = p.MCRecoveryVIP;
        this.MCRestoreSockMutex = p.MCRestoreSockMutex;
        this.MCRestorePort = p.MCRecoveryPort;

        this.fileID = fileHash;
        this.chunkNo = chunkNo;
        this.pathChunk = path;

        this.ReceivedChunkCounter = p.ReceivedChunkCounter;

    }
    public void run(){

        String checkIfSent = new String(fileID + "_" + chunkNo);

        if(ReceivedChunkCounter.get(checkIfSent) != null){
            System.out.println("[TRS] " + chunkNo + " nao e preciso enviar pois ja foi enviada");
        }
        else{

            Random r = new Random();
            try{
                int i = r.nextInt(399)+1;
                System.out.println("[TRS] Vou ter de enviar " + chunkNo + " daqui a " + i + "ms");
                Thread.sleep(r.nextInt(i));

                dataToSend = getInfoFromFile(this.pathChunk);
                sendPacket(dataToSend);

            }
            //catch(InterruptedException e){ e.printStackTrace();};
            catch(Exception e){ e.printStackTrace();};

        }
    }

    private void sendPacket(ByteString dataToSend) {

        String data = "CHUNK"       +' '+
                Peer.version        +' '+
                fileID              +' '+
                Integer.parseInt(chunkNo)  +
                '\r'+'\n'           +
                '\r'+'\n';

        ByteString toSend = new ByteString(data.getBytes());
        toSend.add(dataToSend);
        DatagramPacket packet = new DatagramPacket(toSend.getBytes(),toSend.length(),MCRestoreAddress,MCRestorePort);
        MCRestoreSockMutex.lock();                                                                                   /*Trying to lock resource*/
        try{
            MCRestore.send(packet);                                                                                  /*Sending data*/
        }catch(IOException e){}
        MCRestoreSockMutex.unlock();

        //System.out.println("[RESTORE_SEND] ENVIOU CHUNK NO " + chunkNo);
    }

    private ByteString getInfoFromFile(String pathChunk){
        ByteString res = null;
        try{
            File chunk = new File(pathChunk);
            byte[] bytes = new byte[(int)chunk.length()];
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(pathChunk)));
            dataInputStream.readFully(bytes);
            dataInputStream.close();
            res = new ByteString(bytes);

        }catch(IOException e){
            e.printStackTrace();
        }

        return res;
    }

}
