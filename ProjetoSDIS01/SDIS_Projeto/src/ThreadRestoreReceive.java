import sun.awt.Mutex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
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

    int packetReceivedSize = 512;

    ArrayList<Boolean> positionCheck;
    String[] chunksStored;

    String fileid;
    long numberOfChunks;

    String fileName;

    ThreadRestoreReceive(Peer p, String fileid){
        /*
        try{
            this.MCRestoreSock = new DatagramSocket(0);
        }
        catch(SocketException e){}
        */
        this.MCRestoreSock = p.MCRecoverySock;

        this.MCRestoreAddress = p.MCRecoveryVIP;
        this.MCRestoreSockMutex = p.MCRestoreSockMutex;
        this.MCRestorePort = p.MCRecoveryPort;
        this.fileid = fileid;


        for(int i = 0; i < p.backupLog.size(); i++){
            if(p.backupLog.get(i).hashName == fileid){
                this.fileName = p.backupLog.get(i).fileName;
                this.numberOfChunks = p.backupLog.get(i).noChunks;
            }
        }

        this.fileName.replace("/", Pattern.quote(File.separator));
        String[] parts = this.fileName.split(Pattern.quote(File.separator));
        String name = parts[parts.length-1];
        //this.fileName = "/RestoredFiles/"+name;
        this.fileName = name;


        positionCheck = new ArrayList<Boolean>();
        chunksStored = new String[(int)numberOfChunks];

    }

    public void run(){

        while(!Peer.endProgram){
            byte[] buff = new byte[packetReceivedSize];
            DatagramPacket packet = new DatagramPacket(buff,buff.length);
            try{
                MCRestoreSock.setSoTimeout(50);
                MCRestoreSock.receive(packet);
                if(packetHandler(packet)){
                    System.out.println("Ja recebeu todos os chunks");
                    break;
                }
                else{
                    //System.out.println("Ainda nao acabou por isso vou continuar a escutar");
                }
            }
            catch(SocketTimeoutException e){}
            catch(IOException e){}

        }

        //System.out.println("Terminei escuta - passar po ficheiro");
        //System.out.println(Arrays.toString(chunksStored));

        File file = new File(fileName);
        //System.out.println("Ficheiro vai-se chamar " +fileName);

        // if file doesnt exists, then create it
        try{
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(int i = 0; i < chunksStored.length; i++){
                bw.write(chunksStored[i]);
                //System.out.println("Passei po ficheiro " + i);
            }
            bw.close();
            System.out.println("Ficheiro Restaurado com sucesso: " + this.fileName);

        }
        catch(IOException e){

        }

    }

    boolean packetHandler(DatagramPacket packet){
        String rec = new String(packet.getData());
        String[] receivedMessage = rec.split(" ",6);
        String receivedID  = receivedMessage[2];
        String chunkNo = receivedMessage[3];
        String content = receivedMessage[4];

        System.out.println("Recebi chunk: " + chunkNo);
        if(receivedID.equals(this.fileid)){
            // Ficheiro da Thread criada
            int no = Integer.parseInt(chunkNo);

            if(chunksStored[no] == null){
                System.out.println("Ainda nao tinha este cromo");
                chunksStored[no] = content;
                positionCheck.add(true);
            }
            else{
                System.out.println("Desculpa ja tinha oops");
            }

        }

        return isTransferComplete();
    }

    boolean isTransferComplete(){
        return this.positionCheck.size() == numberOfChunks;
    }
}