
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

    Hashtable<String, Integer> ChunksControl;
    Mutex chunksControlMutex;

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

        this.ChunksControl = new Hashtable<String, Integer>();
        this.chunksControlMutex = new Mutex();

    }
    public void handlerQueue(){
        boolean continueHandler = true;
        while(continueHandler){
            ThreadBackupReceiveScheduleEntry currentEntry = entry.peek();
            if(!entry.isEmpty() && currentEntry.isReady()){
                storedCounterMutex.lock();
                String index = (new String(currentEntry.fileID.getBytes())) + (new String(currentEntry.no.getBytes()));
                int repDegree = Integer.parseInt(new String(currentEntry.replicationDeg.getBytes()));
                int value = StoreCounter.get(index) == null ? 0 : StoreCounter.get(index) ;
                storedCounterMutex.unlock();
                if( value < repDegree){
                    System.out.println(value +" "+repDegree);
                    entry.poll();
                    currentEntry.saveChunk();
                    commandQueueMutex.lock();
                    commands.add("STORED");
                    commands.add(new String(currentEntry.fileID.getBytes()));                                                              /* Envia comando ao processo principal para enviar mensagem STORED*/
                    commands.add(new String(currentEntry.no.getBytes()));
                    commandQueueMutex.unlock();
                }

            }
            else{
                continueHandler = false;
            }
        }
    }
    public void packetHandler(DatagramPacket packet){
        // RECEBE PUTCHUNK

        ByteString packetReceived = new ByteString(packet.getData());
        packetReceived = packetReceived.substring(packet.getOffset(),packet.getLength());
        int i = packet.getLength();

        ByteString[] receivedMessage = packetReceived.split((byte)' ', 5);

        if(receivedMessage.length < 5){
            return;
        }

        ByteString fileId  = receivedMessage[2];
        ByteString chunkNo = receivedMessage[3];
        ByteString rest  = receivedMessage[4];

        ByteString[] aux = rest.split((byte)'\n', 3);

        ByteString repDeg = aux[0];
        repDeg = repDeg.substring(0,repDeg.length()-1);
        ByteString content = aux[2];

        hashTableBackupMutex.lock();
        if (BackupThreads.get(new String(fileId.getBytes()))!= null){
            hashTableBackupMutex.unlock();
            return;
        }
        hashTableBackupMutex.unlock();

        chunksControlMutex.lock();
        String id = new String(fileId.getBytes());
        String no = new String(chunkNo.getBytes());
        String hashtable_fileid = new String(id + no);
        String rep = new String(repDeg.getBytes());
        int hashtable_repDeg = Integer.parseInt(rep);
        ChunksControl.put(hashtable_fileid, hashtable_repDeg);
        chunksControlMutex.unlock();

        //String s = new String(content.getBytes());
        //System.out.println("[TBR] Recebi pacote " + (new String(chunkNo.getBytes())) + " com " + packet.getLength() +"\n[TBR] Com Conteudo " +
        //        "***\n" + s + "***");

        Random r = new Random();
        ThreadBackupReceiveScheduleEntry newEntry = new ThreadBackupReceiveScheduleEntry(r.nextInt(399)+1, fileId, chunkNo, content, repDeg);
        entry.add(newEntry);

    }
    public void run(){
        while(!Peer.endProgram){
            byte[] buff = new byte[200+Peer.chunkSize];
            DatagramPacket packet = new DatagramPacket(buff,buff.length);

            try{
                MCBackup.setSoTimeout(50);
                MCBackup.receive(packet);
                packetHandler(packet);
            }
            catch(SocketTimeoutException e){}
            catch(IOException e){}
            catch(Exception e){e.printStackTrace();}

            handlerQueue();
        }
    }
}
