import java.io.File;
import java.util.Vector;

/**
 * Created by Papa Formigas on 28-03-2014.
 */
public class DoomOfTheDamnedReclaimer {
    long size;
    String path;
    Vector<String> deleted;
    DoomOfTheDamnedReclaimer(String path,long size){
        this.size=size;
        this.path = path;
        this.deleted = new Vector<String>();
    }
    void reclaim(){
        reclaim(new File(path),0);
    }
    long reclaim(File file,long size){
        int erased=0;
        if ( size >= this.size)
        {return 0;}

        if (file.isDirectory()){
            File[] children = file.listFiles();
            for (int i = 0 ; i < children.length && erased+size <=this.size ; i++){
                erased += reclaim(children[i],size+erased);
            }
            if(file.listFiles().length==0){file.delete();}
        }
        else if (file.isFile()){
            erased+= file.length();
            file.delete();
            deleted.add(file.getParentFile().getName()+"/"+file.getName());
        }
        return erased;
    }

}
