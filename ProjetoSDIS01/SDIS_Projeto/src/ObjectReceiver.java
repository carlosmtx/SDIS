import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Papa Formigas on 29-03-2014.
 */
public class ObjectReceiver{
    String port;
    String ip;
    Socket socket;
    ObjectReceiver(String ip , String port){
        this.ip = ip;
        this.port = port;
    }
    Object getObject(){
        Object obj = null;
        try{
            socket = new Socket(ip,Integer.parseInt(port));
            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            obj = ois.readObject();
        }
        catch(UnknownHostException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}
        catch (ClassNotFoundException e){e.printStackTrace();}
        return obj;

    }
}
