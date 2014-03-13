import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Papa Formigas on 26-02-2014.
 */
public class ThreadRestore implements Runnable{
    DatagramSocket MCRestore;
    InetAddress    MCRestoreAddress;
    public ThreadRestore(InetAddress MCAddress,String fileID)throws SocketException{
        this.MCRestoreAddress = MCAddress;
        this.MCRestore        = new DatagramSocket(0);
    }
    public void run(){

    }
}
