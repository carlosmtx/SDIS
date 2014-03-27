
import java.io.File;
import java.util.Vector;

/**
 * Created by Papa Formigas on 22-03-2014.
 */

public class ThreadDelete  implements Runnable{
    String fileID;
    Vector<LogBackup> backupLog;


    ThreadDelete(Peer peer,String fileID)
    {
     this.fileID = fileID;
    }

    public void run(){
        File dir = new File("BackupFiles/"+fileID+"/");
        if (!dir.exists()){System.out.println("Sou preguicoso, nao apanhei nada");return;}
        File[] files = dir.listFiles();

        for (int i = 0 ; i < files.length ; i++){
               files[i].delete();
        }
        dir.delete();
    }
}
