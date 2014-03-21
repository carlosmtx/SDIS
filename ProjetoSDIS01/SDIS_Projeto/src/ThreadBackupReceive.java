
import sun.awt.Mutex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.*;

/**
 * Created by Rui  on 15-03-2014.
 */
public class ThreadBackupReceive implements Runnable {
    DatagramSocket MCBackup;                                                                                         /*Reference to the Multicast Backup Socket in Peer*/
    InetAddress MCBackupAddress;                                                                                     /*Adress of the Mulsticast Backup Channel*/

    public int[] chunksStored;

    Queue<ThreadBackupReceiveScheduleEntry> entry;
    Mutex hashTableBackupMutex;

    Hashtable<String,ThreadBackupSend> BackupThreads;
    Hashtable<String,Integer> StoreCounter;
    Mutex storedCounterMutex;

    Queue<String> commands;
    Mutex commandQueueMutex;

    Hashtable<String,Integer> StoredCounter;
    ThreadBackupReceive(Peer peer){
        this.MCBackup = peer.MCBackupSock;
        this.MCBackupAddress = peer.MCBackupVIP;
        this.BackupThreads = peer.BackupThreads;
        this.hashTableBackupMutex = peer.backupThreadMutex;
        this.StoreCounter= peer.StoreCounter;
        this.storedCounterMutex = peer.storedCounterMutex;
        this.commands = peer.commands;
        this.commandQueueMutex = peer.commandQueueMutex;
        this.StoredCounter = peer.StoreCounter;
        this.entry = new LinkedList<ThreadBackupReceiveScheduleEntry>();
    }
    public void handlerQueue(){
        boolean continueHandler = true;
        while(continueHandler){
            ThreadBackupReceiveScheduleEntry currentEntry = entry.peek();
            if(!entry.isEmpty() && currentEntry.isReady()){
                storedCounterMutex.lock();
                String index = currentEntry.fileID + currentEntry.no;
                int repDegree = Integer.parseInt(currentEntry.replicationDeg);
                int value = StoreCounter.get(index) == null ? 0 : StoreCounter.get(index) ;
                storedCounterMutex.unlock();
                if( value < repDegree){
                    System.out.println(value +" "+repDegree);
                    entry.poll();
                    currentEntry.saveChunk();
                    commandQueueMutex.lock();
                    commands.add("STORED");
                    commands.add(currentEntry.fileID);                                                              /* Envia comando ao processo principal para enviar mensagem STORED*/
                    commands.add(currentEntry.no);
                    commandQueueMutex.unlock();
                }

            }
            else{
                continueHandler = false;
            }
        }
    }
    public void packetHandler(DatagramPacket packet){
        String rec = new String(packet.getData());
        String[] receivedMessage = rec.split(" ",8);
        String fileId  = receivedMessage[2];
        String chunkNo = receivedMessage[3];
        String repDeg  = receivedMessage[4];
        String content = receivedMessage[7];
        hashTableBackupMutex.lock();
        if (BackupThreads.get(fileId)!= null){
            hashTableBackupMutex.unlock();
            return;
        }
        hashTableBackupMutex.unlock();
        Random r = new Random();
        ThreadBackupReceiveScheduleEntry newEntry = new ThreadBackupReceiveScheduleEntry(r.nextInt(400), fileId, chunkNo, content, repDeg);
        entry.add(newEntry);
    }
    public void run(){
        while(!Peer.endProgram){
            byte[] buff = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buff,buff.length);
            try{
                MCBackup.setSoTimeout(50);
                MCBackup.receive(packet);
                packetHandler(packet);
            }
            catch(SocketTimeoutException e){}
            catch(IOException e){}

            handlerQueue();
        }
    }
}
