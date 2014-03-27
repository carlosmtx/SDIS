import sun.awt.Mutex;

import java.io.*;
import java.util.Queue;

/**
 * Created by Papa Formigas on 18-03-2014.
 */
public class ThreadBackupReceiveScheduleEntry {
    long time;
    ByteString no;
    ByteString fileID;
    ByteString chunk;
    ByteString replicationDeg;

    ThreadBackupReceiveScheduleEntry(int randomTime,ByteString fileID,ByteString no,ByteString chunk, ByteString repDeg){
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
            dir = new File("BackupFiles/"+(new String(fileID.getBytes())));
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File("BackupFiles/"+(new String(fileID.getBytes()))+"/"+(new String(no.getBytes()))+".mdr");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fw = new FileOutputStream(file);
            fw.write(chunk.getBytes());
            fw.close();
        }
        catch(IOException e){}
        catch(Exception e){System.out.println("[SCHEDULE] Escrita deu merda em " + (new String(no.getBytes())));}
    }

}
