import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by Papa Formigas on 26-02-2014.
 */
public class Main {
    public static void main(String[] args){
        Peer a = null;
        try{
            a=new Peer("225.4.5.6","225.4.5.6","225.4.5.6");
            a.activate();
        }
        catch(UnknownHostException exp){System.out.println("Wrong Network Adress");}
        catch(IOException exp){System.out.println("Could Not Create Socket");}
    }
}