package Server.Servidor;

import java.io.IOException;
import java.net.*;
public class Server extends DatagramSocket{
	public DatagramPacket 		packet;
	public int					maxSize;
	public Server(int port) throws IOException{
		super(port);
		maxSize = 1024;
	}

	public void send(String send, InetAddress address) throws IOException{
        //System.out.println("Vou enviar '" + send + "'");
        //System.out.println("Para '" + address + "'");
		packet = new DatagramPacket((send+"\n").getBytes(), send.length());
        super.send(packet);
        System.out.println("Enviou");

	}
}
