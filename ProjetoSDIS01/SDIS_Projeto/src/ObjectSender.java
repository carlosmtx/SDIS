import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Papa Formigas on 29-03-2014.
 */
public class ObjectSender implements Runnable{
    ServerSocket socketServ;
    Socket socket;
    Object obj;

    ObjectSender(Object obj){
        try{
            socketServ = new ServerSocket(0);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        this.obj = obj;

    }
    public String getPort(){
       return ""+socketServ.getLocalPort();
    }
    public String getIP(){
        try{
            return new String(Inet4Address.getLocalHost().getHostAddress());
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
    public void run(){
        try{
            //socketServ.setSoTimeout(2000);
            socket = socketServ.accept();
            socketServ.close();
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(obj);
            oos.close();
            os.close();
            socket.close();


        }
        catch(IOException e){
            e.printStackTrace();
        }


    }
}
