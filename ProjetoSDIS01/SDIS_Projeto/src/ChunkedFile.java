import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ChunkedFile {

    private String filename;
    private int chunkSize;
    private ArrayList<String> chunks = new ArrayList<String>();
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
    public ArrayList<String> chunkFile(){
        try{
            byte[] bytes = new byte[(int)file.length()];
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
            dataInputStream.readFully(bytes);
            dataInputStream.close();

            String data = new String(bytes);

            int i = 0;
            while(i < data.length()){
                if( i + chunkSize > data.length()){ // Ultimo chunk
                    chunks.add(data.substring(i));
                    i+= chunkSize;
                }
                else{
                    chunks.add(data.substring(i, i + chunkSize));
                    i+= chunkSize;
                }
            }

        }catch(Exception e){
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
    public ArrayList<String> getChunks() {
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
