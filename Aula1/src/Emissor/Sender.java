package Emissor;

import Emissor.Input.Input;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;



public class Sender {

	public static void main(String[] args) throws IOException{
		
		if (args.length < 3) {
			//System.out.println("Usage: java Client <host_name> <port_number> <oper> <opnd>*");
			//return;
		}

        System.out.println("Using hard coded inputs:");

        ArrayList<Input> inputs = new ArrayList();
        inputs.add(new Input("127.0.0.1", "4455", "REGISTER", "44-55-22", "Leonel"));
        inputs.add(new Input("127.0.0.1", "4455", "REGISTER", "33-22-11", "Carlos"));
        inputs.add(new Input("127.0.0.1", "4455", "REGISTER", "11-33-32", "Leonel"));
        inputs.add(new Input("127.0.0.1", "4455", "REGISTER", "44-55-22"));

		// send request
		DatagramSocket socket = new DatagramSocket();


        for(int i = 0; i < inputs.size(); i++){

            // Input
            //System.out.println("Recebido na consola:");
            //System.out.println(inputs.get(i).toString());
            Input current = new Input(inputs.get(i));

            // Porta
            int port = Integer.parseInt(current.port_number);

            //Operacao
            String message = "";
            if (current.operacao.toLowerCase().equals("register")) {
                // INSERT
                message = "REGISTER " + current.matricula + " " + current.proprietario + "\n";
            }
            else if (current.operacao.toLowerCase().equals("lookup")) {
                // LOOKUP
                message = "LOOKUP " + current.matricula + "\n";
            }

            System.out.print("[" + i + "] Mensagem a Enviar : ");
            System.out.print(message);
            byte[] sbuf = message.getBytes();

            // Address
            InetAddress address = InetAddress.getByName(current.host);

            DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);


            socket.send(packet);
            System.out.println("[" + i + "] Mensagem enviada ");
            System.out.println("[" + i + "] A espera de resposta...");
            byte[] rbuf = new byte[sbuf.length];
            DatagramPacket packet_r = new DatagramPacket(rbuf, rbuf.length);
            socket.receive(packet_r);
            System.out.println("Lel");
            String received = new String(packet_r.getData());
            System.out.println("[" + i + "] Resposta: " + received);
        }

        socket.close();
	}
}
