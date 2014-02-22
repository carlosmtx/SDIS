import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Vector;

/**
 * Created by Papa Formigas on 22-02-2014.
 */

public class Server {
    Vector<String> plates;
    Vector<String> owners;

    DatagramSocket socket;
    int myPort;

    Server(int portSource)throws SocketException{
        this.myPort =portSource;                                                                /*Port for data receiving*/

        plates = new Vector<String>();                                                          /*Database(plates)*/
        owners = new Vector<String>();                                                          /*Database(owners)*/

        socket      = new DatagramSocket(myPort);                                               /*Builing socket*/
    }
    public void sendAnswer(DatagramPacket packet, String answer)throws IOException{

        byte[] sendBuff     = answer.getBytes();                                                /*Buffer where data to be sent will be stored*/
        int port            = packet.getPort();                                                 /*Port for data sending*/
        InetAddress address = packet.getAddress();                                              /*Obtaining dest adress*/
        System.out.println("      SendingAnswer:" + new String(sendBuff));
        DatagramPacket sendPacket = new DatagramPacket(sendBuff,sendBuff.length,address,port);  /*Building send packet*/
        socket.send(sendPacket);                                                                /*Sending packet*/
    }
    public void receiveRequest()throws IOException{
        byte[] buff = new byte[1024];                                                           /*Buffer for receiving data*/
        DatagramPacket recPacket = new DatagramPacket(buff,buff.length);                        /*Packet where received data is to be stored*/
        socket.receive(recPacket);                                                              /*Waiting for a request*/

        String request = new String(recPacket.getData());                                       /*Obtaining request string from packet*/

        String[] commands = request.split("\\s");                                               /*Splitting string in the different commands*/
        if      (commands[0].equals("REGISTER")){                                               /*In case of a register request*/
            register(recPacket, commands);
        }
        else if (commands[0].equals("LOOKUP"))  {                                               /*In case of a lookup request*/
            lookup(recPacket, commands);
        }
    }
    public void register(DatagramPacket packet,String[] commands){
       System.out.println("      Pedido de register");
       System.out.println("      Tipo:"+commands[0]);
       System.out.println("      Matr:"+commands[1]);
       System.out.println("      Nome:"+commands[2]);

       plates.add(commands[1]);                                                                 /*Adding plate to (the)? database*/
       owners.add(commands[2]);                                                                 /*Adding owner "                "*/

        System.out.println("     Adicionei matricula e owner");
    }
    public void lookup(DatagramPacket packet,String[] commands)throws IOException{
        System.out.println("      Pedido de lookup");
        System.out.println("      Tipo:"+commands[0]);
        System.out.println("      Matr:"+commands[1]);
        int index = plates.indexOf(commands[1]);                                              /*looking up the answer*/
        sendAnswer(packet, owners.elementAt(index));                                          /*Sending the answer for lookup(Note: no negative answer exists)*/
    }                                                                                         /*Eh voila*/


}
