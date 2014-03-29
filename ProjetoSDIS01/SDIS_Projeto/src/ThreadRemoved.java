import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by Papa Formigas on 29-03-2014.
 */
public class ThreadRemoved implements Runnable {
    boolean send;
    boolean receivedPutChunk;
    MulticastSocket MCBackupSock;
    InetAddress MCBackupIP;
    String fileID;
    String chunkNo;
    String repDegree;
    ByteString chunkContent;
    Hashtable<String,ThreadRemoved> removedTable;


    ThreadRemoved(Peer peer,String fileID,String chunkNo){
        try{
        this.MCBackupSock = new MulticastSocket(peer.MCBackupPort);                                       /*Socket used for Control communications*/
        this.MCBackupSock.joinGroup(peer.MCBackupVIP);                                                    /*Joining Control Network */
        }
        catch (IOException e){
            e.printStackTrace();
        }
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.send = true;
        this.receivedPutChunk = false;
        this.chunkContent = null;
        this.repDegree = "3";
        this.MCBackupIP = peer.MCBackupVIP;
    }
    public void run(){
        byte[] buffer = new byte[200+Peer.chunkSize];
        int timeToWait = (new Random().nextInt(400)+1);
        long lastTime = System.currentTimeMillis();
        while(!Peer.endProgram && timeToWait > 0 && !receivedPutChunk){
            try{
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                MCBackupSock.setSoTimeout(timeToWait);
                MCBackupSock.receive(packet);
                timeToWait -=System.currentTimeMillis()-lastTime;
                packetHandler(packet);
            }
            catch (SocketException e){timeToWait -=System.currentTimeMillis()-lastTime;}
            catch (IOException e){timeToWait -=System.currentTimeMillis()-lastTime;}
        }

        if(!receivedPutChunk){
            //System.out.println("Posso enviar PUTCHUNK de " + chunkNo + " e " + repDegree);
            sendPutChunk();
        }

    }

    void getChunk(){
        String filename = "BackupFiles/"+fileID+"/"+chunkNo + ".mdr";
        try{
            File chunk = new File(filename);

            if(!chunk.exists()){
                //System.out.println("Nao tenho ficheiro");
                send = false;
            }
            else{
                //System.out.println("Tenho ficheiro");
                byte[] bytes = new byte[(int)chunk.length()];
                DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
                dataInputStream.readFully(bytes);
                dataInputStream.close();
                this.chunkContent = new ByteString(bytes);
            }

        }catch(IOException e){
            //e.printStackTrace();
            System.out.println("Excecao");
        }

    }

    void sendPutChunk(){
        getChunk();

        if(send == true){
            String header = "PUTCHUNK"  +' '+
                    Peer.version        +' '+
                    fileID              +' '+
                    chunkNo             +' '+
                    repDegree           +
                    '\r'+'\n'           +
                    '\r'+'\n';

            ByteString data = new ByteString(header.getBytes());


            data.add(this.chunkContent);

            //System.out.println("VOU ENVIAR PUTCHUNK n" + chunkNo);

            DatagramPacket packet = new DatagramPacket(data.getBytes(),data.length(),MCBackupIP,2002);
            try{
                MCBackupSock.send(packet);
            }catch(IOException e){
                System.out.println("[THREAD_REMOVED] Excecao a enviar pacote");
            }

            System.out.println("Ficheiro replicado com sucesso");
        }
    }

    void packetHandler(DatagramPacket packet){


        ByteString packetReceived = new ByteString(packet.getData());
        packetReceived = packetReceived.substring(packet.getOffset(),packet.getLength());
        ByteString[] receivedMessage = packetReceived.split((byte)' ', 5);

        if(receivedMessage.length < 5){
            return;
        }

        ByteString received_fileid  = receivedMessage[2];
        ByteString received_chunkNo = receivedMessage[3];


        String received_id = new String(received_fileid.getBytes());
        String received_no = new String(received_chunkNo.getBytes());

        /*
        ByteString rest  = receivedMessage[4];
        ByteString[] aux = rest.split((byte)'\r', 3);
        ByteString received_repDeg = aux[0];
        String received_redDeg = new String(received_repDeg.getBytes());

        this.repDegree = received_redDeg;
        */
        if(this.fileID == received_id && this.chunkNo == received_no){
            this.receivedPutChunk = true;
        }


    }
}
