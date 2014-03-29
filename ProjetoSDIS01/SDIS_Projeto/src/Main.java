import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

/**
 * Created by Papa Formigas on 26-02-2014.
 */
public class Main {
    public static void main(String[] args){

        Peer a = null;
        /*
        ChunkedFile file= new ChunkedFile("file.txt",1000,3);

        ObjectSender sender = new ObjectSender(file);
        new Thread(sender).start();
        try{Thread.sleep(200);}catch(Exception e){}

        ObjectReceiver receiver = new ObjectReceiver(sender.getIP(),sender.getPort());
        ChunkedFile file2=(ChunkedFile)receiver.getObject();

        */

        try{
            a=new Peer("225.4.5.6",2000,"225.4.5.6",2001,"225.4.5.6",2002);
            a.run();
        }
        catch(UnknownHostException exp){System.out.println("Wrong Network Adress");}
        catch(IOException exp){System.out.println("Could Not Create Socket");}
    }
}