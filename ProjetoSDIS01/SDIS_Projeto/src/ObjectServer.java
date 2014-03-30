import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Papa Formigas on 29-03-2014.
 */
public class ObjectServer implements Runnable{
    ServerSocket socketServ;
    Socket socket;
    Object obj;
    ObjectServer(){
        try{
            socketServ = new ServerSocket(0);
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }
    ObjectServer(Object obj){
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

    public Object getObject(){
        Object obj = null;
        try{
            socket = socketServ.accept();
            socketServ.close();
            InputStream os = socket.getInputStream();
            ObjectInputStream oos = new ObjectInputStream(os);
            obj = oos.readObject();
            oos.close();
            os.close();
            socket.close();
        }
        catch(UnknownHostException e){e.printStackTrace();}
        catch (IOException e){e.printStackTrace();}
        catch (ClassNotFoundException e){e.printStackTrace();}
        return obj;


    }
}
