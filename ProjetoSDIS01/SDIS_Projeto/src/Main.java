import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by Papa Formigas on 26-02-2014.
 */
public class Main {
    public static void main(String[] args){
        System.setProperty("file.encoding","UTF-8");
        Peer a = null;
        try{
            new Thread(new ThreadDelete("")).start();
            a=new Peer("225.4.5.6",2000,"225.4.5.6",2001,"225.4.5.6",2002);
            a.run();
        }
        catch(UnknownHostException exp){System.out.println("Wrong Network Adress");}
        catch(IOException exp){System.out.println("Could Not Create Socket");}
    }
}