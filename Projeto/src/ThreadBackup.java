import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Papa Formigas on 26-02-2014.
 */
public class ThreadBackup implements Runnable{
    DatagramSocket MCBackup;
    InetAddress    MCBackupAddress;
    public ThreadBackup(InetAddress MCAddress,String fileID)throws SocketException {
        this.MCBackupAddress = MCAddress;
        this.MCBackup = new DatagramSocket(0);
    }
    public void run(){

    }
}
