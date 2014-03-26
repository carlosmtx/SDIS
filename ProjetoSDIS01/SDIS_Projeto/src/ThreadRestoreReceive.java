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

    ThreadRestoreReceive(Peer p, String fileid){
        this.MCRestoreSock = p.MCRecoverySock;
        this.MCRestoreAddress = p.MCRecoveryVIP;
        this.MCRestoreSockMutex = p.MCRestoreSockMutex;
        this.MCRestorePort = p.MCRecoveryPort;
        this.fileid = fileid;

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
        while(!Peer.endProgram){
            byte[] buff = new byte[100+Peer.chunkSize];

            DatagramPacket packet = new DatagramPacket(buff,buff.length);

            try{
                MCRestoreSock.setSoTimeout(50);
                MCRestoreSock.receive(packet);
                if(packetHandler(packet)){
                    System.out.println("[TRR] Ja recebeu todos os chunks");
                    break;
                }
                else{

                }
            }
            catch(SocketTimeoutException e){}
            catch(IOException e){}

        }

        File file = new File(fileName);
        try{
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i = 0; i < chunksStored.length; i++){
                bw.write(chunksStored[i]);
                System.out.println("[TRR] Passei po ficheiro " + i);
            }
            bw.close();
            System.out.println("[TRR] Ficheiro Restaurado com sucesso: '" + this.fileName + "'");

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
                System.out.println("[RESTORE_RECEIVE] RECEBEU CHUNK NO " + chunkNo + "***\n"+ content + "****");
                System.out.println("[RESTORE_RECEIVE] Ainda nao tinha este pedaco");
                chunksStored[no] = content;
                positionCheck.add(true);
            }
            else{
                System.out.println("[RESTORE_RECEIVE] Desculpa ja tinha oops");
            }
        }
        return isTransferComplete();
    }

    boolean isTransferComplete(){
        return this.positionCheck.size() == numberOfChunks;
    }
}