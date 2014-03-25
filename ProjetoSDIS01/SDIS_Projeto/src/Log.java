import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by Papa Formigas on 24-03-2014.
 */
public class Log implements Serializable{
    static final public byte DELETE     = 1;
    static final public byte BACKUPFILE = 2;
    static final public byte PUTCHUNK   = 3;
    static final public byte RESTORE    = 5;
    static final public byte REMOVED    = 6;
    static final public byte STORED     = 7;
    static final public byte IN         = 10;
    static final public byte OUT        = 11;

    String fileID;
    int chunkNo;
    int IO;

    int  type;
    long date;
    InetAddress adress;


    Log(int type,long date){
        this.type = type;
        this.date = date;
    }
    Log(int type,int IO,long date,String fileID,int chunkNo){
        this.type = type;
        this.date = date;
        this.fileID=fileID;
        this.chunkNo=chunkNo;
        this.IO = IO;
    }
    Log(int type,int IO,long date,String fileID){
        this.type = type;
        this.date = date;
        this.fileID=fileID;
        this.chunkNo = 0;
        this.IO = IO;
    }
    Log(int type,int IO,long date,String fileID,int chunkNo,InetAddress adress){
        this.type = type;
        this.date = date;
        this.fileID =fileID;
        this.chunkNo=chunkNo;
        this.adress=adress;
        this.IO = IO;
    }

}
