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
                        repDegree           +' '+
                        '\r'+'\n'           +
                        '\r'+'\n';
        return header;
    }
    private void sendPacket(int i,int repDegree,ArrayList fileChunks){
        String data = getHeader(i,repDegree)+ fileChunks.get(i);

        // Problema nao e aqui. Envia direito.

        DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCBackupAddress,2002);

        System.out.println("[TBS] PACKET ENVIADO ****" + data + "****");


        backupThreadMutex.lock();                                                                                   /*Trying to lock resource*/
        try{
            MCBackup.send(packet);                                                                                  /*Sending data*/
        }catch(IOException e){}
        backupThreadMutex.unlock();                                                                                 /*Unlocking resource*/
    }
    public void run(){
        System.out.println("Backup Send Thread Gerada");
        ArrayList<String> aux = file.chunkFile();                                                                   /*Obtaining the file divided in chunks*/
        for(int i = 0; i < aux.size(); i++){                                                                        /*Sending all the chunks*/
            sendPacket(i,file.getRepDegree(),aux);
        }
        for ( int i = 0 ; i < chunksStored.length ; i++){
            if (chunksStored[i] < file.getRepDegree()){
            //    sendPacket(i,file.getRepDegree()-chunksStored[i],aux);
            }
        }
    }
}
