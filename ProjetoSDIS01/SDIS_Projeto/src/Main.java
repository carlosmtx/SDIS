import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

/**
 * Created by Papa Formigas on 26-02-2014.
 */
public class Main {
    public static void main(String[] args){

        Peer a = null;

        String mcControl = "225.4.5.6";
        String mcRecovery = "225.4.5.6";
        String mcBackup = "225.4.5.6";

        int portControl = 2000;
        int portRecovery = 2001;
        int portBackup = 2002;

        if(args.length == 6){
            mcControl = args[0];
            portControl = Integer.parseInt(args[1]);
            mcBackup = args[2];
            portBackup = Integer.parseInt(args[3]);
            mcRecovery = args[4];
            portRecovery = Integer.parseInt(args[5]);

        }

        try{
            a=new Peer(mcControl,portControl,mcRecovery,portRecovery,mcBackup,portBackup);
            a.run();
        }
        catch(UnknownHostException exp){System.out.println("Wrong Network Adress");}
        catch(IOException exp){System.out.println("Could Not Create Socket");}
    }
}