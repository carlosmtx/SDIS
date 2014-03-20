import sun.awt.Mutex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;

/**
 * Created by Papa Formigas on 18-03-2014.
 */
public class ThreadBackupReceiveScheduleEntry {
    long time;
    String no;
    String fileID;
    String chunk;
    String replicationDeg;

    ThreadBackupReceiveScheduleEntry(int randomTime,String fileID,String no,String chunk, String repDeg){
        time = System.currentTimeMillis()+(long)randomTime;
        this.fileID = fileID;
        this.no = no;
        this.chunk = chunk;
        this.replicationDeg = repDeg;
    }
    boolean isReady(){
        return time < System.currentTimeMillis() ;
    }
    void saveChunk(){
        try{
            File dir = new File("BackupFiles");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File("BackupFiles/"+fileID+"_"+no+".mdr");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(chunk);
            bw.close();
        }
        catch(IOException e){}
    }

}
