package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Vector;

import Server.Servidor.*;
import Server.Matricula.Matricula;

public class ServMain {
	static Vector<Matricula> 	matriculas;
	static Server serv 	= null;
	static Vector<String>		nomes;
    static int iters = 0;

	public static void main(String[] args) {
		matriculas 	= new Vector<Matricula>();
		nomes		= new Vector<String>();

        byte[] s = new byte[1024];
        DatagramPacket message = new DatagramPacket(s,s.length);


		boolean JavaEUmaTreta = true;
		try { serv = new Server(4455);} catch (IOException e) {e.printStackTrace();}
		//try { serv.receive(message);} 	catch (IOException e) {e.printStackTrace();}
        System.out.println("Inicio.");


		while(JavaEUmaTreta){
			try {
                    System.out.println("Iter no " + iters);
                    serv.receive(message);
                    System.out.println("[" + iters + "] Address do recebido: " + message.getAddress());
					takeAction(message);
					System.out.println("[" + iters + "] Tamanho Estrutura: " + matriculas.size());
			}	
			catch (IOException e) {e.printStackTrace();}

            iters++;
		}
		serv.close();
	}

	static void takeAction(DatagramPacket packet){
        String message = new String(packet.getData());
		String[] commands = message.split("\\s");

		if		(commands[0].equals("REGISTER")){addRecord(packet);}
		else if (commands[0].equals("LOOKUP"))	{getName(packet);}
	}
	static void getName(DatagramPacket packet){
        String message_ = new String(packet.getData());
        String[] message = message_.split("\\s");
		Matricula compare = new Matricula(message[1]);
		for (int i = 0 ; i < matriculas.size() ; i++){
			if ( matriculas.elementAt(i).equals(compare)){
				try {serv.send(nomes.elementAt(i),packet.getAddress());} catch (IOException e) {e.printStackTrace();}
			}
		}
	}
	static void addRecord(DatagramPacket packet){
        String message_ = new String(packet.getData());
        String[] commands = message_.split("\\s");

		Matricula 	compare = new Matricula(commands[1]);
		String 		name 	= commands[2];
		matriculas.add(compare);
		nomes.add(name);

        System.out.println("[" + iters + "] " + commands[1]);

		try {
                //System.out.println(packet.getAddress());
                serv.send("Success",packet.getAddress());
        } catch (IOException e) {e.printStackTrace();}
	}
}
