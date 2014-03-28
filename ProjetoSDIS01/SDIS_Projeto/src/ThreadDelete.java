
import sun.awt.Mutex;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by Papa Formigas on 22-03-2014.
 */

public class ThreadDelete  implements Runnable{
    String fileID;
    Vector<LogBackup> backupLog;

    Hashtable<String, Integer> DeletedCounter;
    Mutex chunksControlMutex;

    ThreadDelete(Peer peer,String fileID)
    {
        this.fileID = fileID;
        this.DeletedCounter = peer.DeletedCounter ;
        this.chunksControlMutex = peer.chunksControlMutex;

    }

    public void run(){
        File dir = new File("BackupFiles/"+fileID+"/");
        if (!dir.exists()){/*System.out.println("Sou preguicoso, nao apanhei nada");*/return;}
        File[] files = dir.listFiles();

        int i;
        for (i = 0 ; i < files.length ; i++){
               files[i].delete();
        }
        dir.delete();

        for (int j = 0; j < i; j++){
            chunksControlMutex.lock();
            DeletedCounter.put(fileID,j);
            chunksControlMutex.unlock();
        }


    }
}
