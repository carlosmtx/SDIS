/**
 * Created by Papa Formigas on 22-02-2014.
 */
public class Main {
    public static void main (String[] args){
        Server server = null;
        System.out.println("-Criando socket");

        try{server = new Server(2005);} catch(Exception e){e.printStackTrace();return;}

        System.out.println("-Socket Criado");
        System.out.println("-Bloqueando p/ receber request ");

        try{server.receiveRequest();}   catch(Exception e){e.printStackTrace();return;}

        System.out.println("-Recebi e tratei um request");
        System.out.println("    Vector Plates:"+server.plates);
        System.out.println("    Vector Owners:"+server.owners);
        System.out.println("-Bloqueando p/ receber request ");

        try{server.receiveRequest();}   catch(Exception e){e.printStackTrace();return;}
        System.out.println("-Recebi um request");
        System.out.println("-Bloqueando para receber um request");
        try{server.receiveRequest();}   catch(Exception e){e.printStackTrace();return;}
        System.out.println("-Recebi e tratei um request");
        System.out.println("    Vector Plates:"+server.plates);
        System.out.println("    Vector Owners:"+server.owners);

    }
}
