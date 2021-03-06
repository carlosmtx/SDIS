import java.io.IOException;
import java.net.*;

/**
 * Created by Papa Formigas on 22-02-2014.
 * Notes: Abrir com editor que nao ponha limite no tamanho horizontal das linhas caso contrario fica Feiooooo
 */
public class Client {
    int port;
    int myPort;
    InetAddress ipAddress;
    DatagramSocket socket;

    Client(String IpAddress,int portSource,int portDest)throws SocketException,UnknownHostException{
        this.port  =portDest;                                                                        /*Port for sending*/
        this.myPort =portSource;
        this.ipAddress = InetAddress.getByName(IpAddress);                                           /*Building ipAddressObject;Note: throws exception on invalid Ip*/
        socket = new DatagramSocket(myPort);                                                         /*Building Socket used by object to send and receive data*/
    }
    public void registerPlate(String plate,String clientName)throws IOException{
        String message = "REGISTER " + plate+' '+ clientName+'\n';                                  /*Building send message. REGISTER <plate> <client>*/
        byte[] sendBuff= message.getBytes();                                                        /*Converting message to bytes(Hint: needed by DatagramPacket)*/

        DatagramPacket packet = new DatagramPacket(sendBuff,sendBuff.length,ipAddress,port);        /*Building send packet with message/ipAddress/port*/
        socket.send(packet);                                                                        /*Sending packet*/
    }
    public String getOwnerAssociated(String plate)throws IOException{
        String message = "LOOKUP "+plate+'\n';                                                       /*Building message. REGISTER <plate>*/
        byte[] sendBuff=message.getBytes();                                                          /*Converting message to bytes(Hint: needed by DatagramPacket)*/
        DatagramPacket sendPacket = new DatagramPacket(sendBuff,sendBuff.length,ipAddress,port);     /*Building send packet with message/ipAddress...*/
        socket.send(sendPacket);                                                                     /*Sending packet*/


        byte[] receiveBuff = new byte[1024];
        DatagramPacket recPacket = new DatagramPacket(receiveBuff,receiveBuff.length);
        socket.receive(recPacket);                                                                   /*Waiting for response*/
        String receivedMessage = new String(recPacket.getData());                                    /*Obtaining Data*/
        return receivedMessage;                                                                      /*Eh voila*/
    }
}
