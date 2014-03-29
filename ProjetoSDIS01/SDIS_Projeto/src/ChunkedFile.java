import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ChunkedFile implements Serializable{

    private String filename;
    private int chunkSize;
    private ArrayList<byte[]> chunks = new ArrayList<byte[]>();
    private File file;
    private String hashName;
    private int repDegree;

    ChunkedFile(String filename, int chunkSize,int repDegree){
        this.filename = filename;
        this.chunkSize = chunkSize;
        file = new File(filename);
        this.repDegree = repDegree;
        MessageDigest sha256 = null;

        String food = new String(filename+getLastModified()+getSize());
        try{sha256 = MessageDigest.getInstance("SHA-256");}
        catch(NoSuchAlgorithmException e){}

        byte[] shaEncode = sha256.digest(food.getBytes());
        hashName = new String();
        for ( int i = 0 ; i< shaEncode.length; i++){
            hashName+= String.format("%02X",shaEncode[i] );
        }
    }
    public byte[] byteTrimmer(byte[] bytes,int startP,int no){
        byte[] res = new byte[no];
        for (int i = 0 ; i < no ;i++){
            res[i]=bytes[startP+i];
        }
        return res;
    }
    public ArrayList<byte[]> chunkFile(){
        try{
            byte[] bytes = new byte[(int)file.length()];
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
            dataInputStream.readFully(bytes);
            dataInputStream.close();
            for (int i = 0 ; i < bytes.length ; i+=chunkSize){
                if (i+chunkSize > bytes.length){
                    chunks.add(byteTrimmer(bytes,i,bytes.length-i) );
                }
                else {
                    chunks.add(byteTrimmer(bytes,i,chunkSize) );
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
        return chunks;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public int getChunkSize() {
        return chunkSize;
    }
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
    public ArrayList<byte[]> getChunks() {
        return chunks;
    }
    public void printChunks(){
        for(int i = 0; i < chunks.size(); i++){
            System.out.println("Chunk No: " + i);
            System.out.println(chunks.get(i));
        }
    }
    public String getHash(){
        return hashName;
    }
    public long getSize(){
        return file.length();
    }
    public long getLastModified(){
        return file.lastModified();
    }
    public int getRepDegree(){
        return repDegree;
    }


}
