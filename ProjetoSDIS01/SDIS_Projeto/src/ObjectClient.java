import com.sun.corba.se.spi.activation.Server;

import java.io.*;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 * Created by Papa Formigas on 29-03-2014.
 */
public class ObjectClient implements Runnable{
    String port;
    String ip;
    Socket socket;
    Object obj;
    ObjectClient(String ip, String port){
        this.ip = ip;
        this.port = port;
    }
    ObjectClient(String ip, String port, Object obj){
        this.ip = ip;
        this.port = port;
        this.obj = obj;
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
        catch (IOException e){}
        catch (ClassNotFoundException e){e.printStackTrace();}
        return obj;

    }
    public void run(){
        try{
            Socket s = new Socket(ip,Integer.parseInt(port));

            OutputStream os = s.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(obj);
            oos.close();
            os.close();
            s.close();

        }
        catch(IOException e){
            //e.printStackTrace();
        }

    }
}
