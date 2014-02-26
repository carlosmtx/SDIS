import java.io.*;
import java.util.ArrayList;

public class Ficheiro {

    private String filename;
    private int chunkSize;
    private ArrayList<String> chunks = new ArrayList<String>();

    Ficheiro(String filename, int chunkSize){
        this.filename = filename;
        this.chunkSize = chunkSize;

        chunkFile();
    }

    // Corta Ficheiro para "chunks"
    private void chunkFile(){
        try{
            /* Scanner
                Scanner sc = new Scanner(new File(filename));
                String data = "";
                while(sc.hasNext()){
                    data += sc.next();
                }
             */


            File file = new File(filename);
            byte[] bytes = new byte[(int)file.length()];
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
            dataInputStream.readFully(bytes);
            dataInputStream.close();

            String data = new String(bytes);
            //System.out.print(res);


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
    }



    // Gets Sets
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
}
