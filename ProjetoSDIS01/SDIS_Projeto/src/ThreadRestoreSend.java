import sun.awt.Mutex;


import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Queue;

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
    String dataToSend;

    public ThreadRestoreSend(Peer p, String fileHash, String chunkNo, String path)throws SocketException {

        this.MCRestore = p.MCRecoverySock;
        this.MCRestoreAddress = p.MCRecoveryVIP;
        this.MCRestoreSockMutex = p.MCRestoreSockMutex;
        this.MCRestorePort = p.MCRecoveryPort;

        this.fileID = fileHash;
        this.chunkNo = chunkNo;
        this.pathChunk = path;

    }
    public void run(){
        dataToSend = getInfoFromFile(pathChunk);
        sendPacket(dataToSend);
    }

    private void sendPacket(String dataToSend) {

        String data = "CHUNK"       +' '+
                Peer.version        +' '+
                fileID              +' '+
                chunkNo             +' '+
                '\n'                +' '+
                '\n'                +' '+
                dataToSend;

        //System.out.println("A enviar: \n****************\n'" + data + "\n****************");
        DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCRestoreAddress,MCRestorePort);
        System.out.println("Restore enviou packet com tamanhao: " + data.getBytes().length);
        MCRestoreSockMutex.lock();                                                                                   /*Trying to lock resource*/
        try{
            MCRestore.send(packet);                                                                                  /*Sending data*/
        }catch(IOException e){}
        MCRestoreSockMutex.unlock();
    }

    private String getInfoFromFile(String pathChunk){
        String data = "";
        try{
            File chunk = new File(pathChunk);
            byte[] bytes = new byte[(int)chunk.length()];
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(pathChunk)));
            dataInputStream.readFully(bytes);
            dataInputStream.close();

            data = new String(bytes);

            //System.out.println("****************** Data Lida do Ficheiro ******************\n" + data + "\n********************************************\n");
            System.out.println("Enviou Packet com ChunkNo: " + chunkNo);
        }catch(IOException e){
            e.printStackTrace();
        }

        return data;
    }

}
