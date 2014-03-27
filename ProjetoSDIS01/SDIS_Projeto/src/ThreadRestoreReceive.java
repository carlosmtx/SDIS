import sun.awt.Mutex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.regex.Pattern;

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
    String fileName;

    Mutex commandQueueMutex;
    Queue<String> commands;
    long chunkNo;

    ThreadRestoreReceive(Peer p, String fileid){
        this.MCRestoreSock = p.MCRecoverySock;
        this.MCRestoreAddress = p.MCRecoveryVIP;
        this.MCRestoreSockMutex = p.MCRestoreSockMutex;
        this.MCRestorePort = p.MCRecoveryPort;
        this.fileid = fileid;
        this.commandQueueMutex = p.commandQueueMutex;
        this.commands = p.commands;

        LogBackup aux = null;
        for(int i = 0; i < p.backupLog.size(); i++){
            if(p.backupLog.get(i).hashName.equals(fileid)){
                aux = p.backupLog.get(i);
            }
        }
        chunkNo = aux.noChunks +1;

        for(int i = 0; i < p.backupLog.size(); i++){
            if(p.backupLog.get(i).hashName == fileid){
                this.numberOfChunks = p.backupLog.get(i).noChunks+1;

                this.fileName = p.backupLog.get(i).fileName;
            }
        }

        positionCheck = new ArrayList<Boolean>();
        chunksStored = new String[(int)numberOfChunks];

        this.fileName.replace("/", Pattern.quote(File.separator));
        String[] parts = this.fileName.split(Pattern.quote(File.separator));
        String name = parts[parts.length-1];
        this.fileName = name; /*"RestoredFiles/"+name; */

    }

    public void run(){
        boolean resend = true;
        byte[] buff = new byte[100+Peer.chunkSize];
        int attemptsNumber=0;
        DatagramPacket packet = new DatagramPacket(buff,buff.length);
        for(int i = 0; i < chunkNo && !Peer.endProgram && attemptsNumber < 5; i++){
            try{
                if(resend){
                System.out.println("Vou tentar receber o :"+i+"pacote");
                commandQueueMutex.lock();
                commands.add("GETCHUNK");
                commands.add(fileid);
                commands.add(""+i);
                commandQueueMutex.unlock();
                }
                MCRestoreSock.setSoTimeout(100);
                MCRestoreSock.receive(packet);
                attemptsNumber =0;
                if(!packetHandler(packet))
                {i--;resend=false;}

            }
            catch(SocketTimeoutException e){i--;attemptsNumber++;resend=true;}
            catch(IOException e){}
        }
        if(attemptsNumber == 5){
            System.out.println("Nao deu Devia mandar uma excepcao .... ThreadRestoreReceive linha 96");
            return;
        }

        File file = new File(fileName);
        try{
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i = 0; i < chunkNo; i++){
                bw.write(chunksStored[i]);
            }
            bw.close();


        }
        catch(IOException e){

        }
    }

    boolean packetHandler(DatagramPacket packet){


        String rec = new String(packet.getData());
        rec = rec.substring(0,packet.getLength());

        String[] receivedMessage = rec.split(" ",5);
        String receivedID  = receivedMessage[2];
        String chunkNo = receivedMessage[3];
        String content = receivedMessage[4];
        content = content.substring(4,content.length());

        if(receivedID.equals(this.fileid)){
            // Ficheiro da Thread criada
            int no = Integer.parseInt(chunkNo);

            if(chunksStored[no] == null){

                chunksStored[no] = content;
                positionCheck.add(true);
            }
            else{
                return false;
            }
        }
        return true;
    }

    boolean isTransferComplete(){
        return this.positionCheck.size() == numberOfChunks;
    }
}