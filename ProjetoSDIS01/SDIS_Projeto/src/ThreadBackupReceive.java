import sun.awt.Mutex;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Papa Formigas on 15-03-2014.
 */
public class ThreadBackupReceive implements Runnable {
    DatagramSocket MCBackup;                                                                                            /*Reference to the Multicast Backup Socket in Peer*/
    InetAddress MCBackupAddress;                                                                                     /*Adress of the Mulsticast Backup Channel*/
    ChunkedFile    file;                                                                                                /*The file to be sent over the network*/
    Mutex backupThreadMutex;                                                                                            /*Only one thread can send data at a given time,all ThreadSendBackup share this mutex*/

    ThreadBackupReceive(){

    }

    public void run(){

    }
}
