import sun.awt.Mutex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Papa Formigas on 26-02-2014.
 */
public class ThreadBackupSend implements Runnable{
    DatagramSocket MCBackup;                                                                                            /*Reference to the Multicast Backup Socket in Peer*/
    InetAddress    MCBackupAddress;                                                                                     /*Adress of the Mulsticast Backup Channel*/
    ChunkedFile    file;                                                                                                /*The file to be sent over the network*/
    Mutex backupThreadMutex;                                                                                            /*Only one thread can send data at a given time,all ThreadSendBackup share this mutex*/

    public int[] chunksStored;                                                                                          /*Each position represents a chunk that was stored in a remote computer*/

    public ThreadBackupSend(Peer peer,ChunkedFile file)throws SocketException {
        this.MCBackupAddress = peer.MCBackupVIP;
        this.MCBackup = peer.MCBackupSock;
        this.file = file;
        this.backupThreadMutex = peer.backupThreadMutex;
        this.chunksStored = new int[(int)file.getSize()/file.getChunkSize()+1];
    }
    private String getHeader(int i,int repDegree){
        String header = "PUTCHUNK"          +' '+
                        Peer.version        +' '+
                        file.getHash()      +' '+
                        i                   +' '+
                        repDegree           +
                        '\r'+'\n'           +
                        '\r'+'\n';
        return header;
    }
    private void sendPacket(int i,int repDegree,ArrayList<byte[]> fileChunks){
        //String data = getHeader(i,repDegree)+ fileChunks.get(i);
        try{
            ByteString data = new ByteString(getHeader(i,repDegree).getBytes());
            data.add(fileChunks.get(i));

            System.out.println("[TBS] A enviar pacote " + i + " com " + data.length() + " bytes");
            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCBackupAddress,2002);

            backupThreadMutex.lock();                                                                                   /*Trying to lock resource*/

            MCBackup.send(packet);                                                                                  /*Sending data*/
        }catch(Exception e){
            System.out.println("[TBR] Excecao a enviar pacote " + i);
        }
        backupThreadMutex.unlock();                                                                                 /*Unlocking resource*/
    }
    public void run(){
        int time = 1000;
        ArrayList<byte[]> aux = file.chunkFile();                                                                   /*Obtaining the file divided in chunks*/
        //System.out.println("Vou enviar chunks: " +aux.size());
        Boolean end = false;
        for(int j = 0; j < 5 && !Peer.endProgram && !end; j++){
            for(int i = 0; i < aux.size(); i++){                                                                        /*Sending all the chunks*/
                if(chunksStored[i] < Peer.repDegree){
                    sendPacket(i,file.getRepDegree(),aux);
                }
            }
            end = true;
            for(int i = 0; i < chunksStored.length; i++){
                if(chunksStored[i] < Peer.repDegree){
                    end = false;
                }
            }
            try{Thread.sleep(time); time = time*2;}catch (InterruptedException e){};
        }
    }
}
