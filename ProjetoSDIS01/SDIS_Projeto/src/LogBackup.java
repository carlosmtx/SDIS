import java.io.Serializable;

/**
 * Created by Papa Formigas on 20-03-2014.
 */
public class LogBackup implements Serializable{
    String fileName;
    long lastModified;
    long size;
    long noChunks;
    long chunkSize;
    long backupDate;
    String hashName;

    LogBackup(ChunkedFile file){
        this.fileName    =file.getFilename();
        this.lastModified=file.getLastModified();
        this.size        =file.getSize();
        this.chunkSize   =file.getChunkSize();
        this.noChunks    =(long)Math.ceil(this.size/this.chunkSize);
        this.backupDate  =System.currentTimeMillis();
        this.hashName    =file.getHash();
    }
    int compareTo(Object o){
        LogBackup a = (LogBackup)o;
        if     (this.backupDate < a.backupDate){return -1;}
        else if(this.backupDate ==a.backupDate){return  0;}
        return 1;
    }
}
