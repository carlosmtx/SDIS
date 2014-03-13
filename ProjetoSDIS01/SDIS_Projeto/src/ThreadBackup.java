import sun.awt.Mutex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Papa Formigas on 26-02-2014.
 */
public class ThreadBackup implements Runnable{
    DatagramSocket MCBackup;
    InetAddress    MCBackupAddress;
    String fileId;
    Mutex backupThreadMutex;
    public Vector<Integer> chunksStored;
    public ThreadBackup(DatagramSocket socket,InetAddress MCAddress,String fileID,Mutex backupThreadMutex)throws SocketException {
        this.MCBackupAddress = MCAddress;
        this.MCBackup = socket;
        this.fileId = fileID;
        this.backupThreadMutex = backupThreadMutex;
    }
    public void run(){
        System.out.println("Backup Thread Gerada");
        File file = new File(fileId, 128);
        ArrayList<String> aux = file.getChunks();
        chunksStored = new Vector<Integer>(aux.size());
        for(int i = 0; i < aux.size(); i++){

            String data = aux.get(i);
            System.out.println("Data:"+data);
            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCBackupAddress,2002);


            backupThreadMutex.lock();
            try{
                MCBackup.send(packet);
            }catch(IOException e){}
            backupThreadMutex.unlock();

        }

    }
}
