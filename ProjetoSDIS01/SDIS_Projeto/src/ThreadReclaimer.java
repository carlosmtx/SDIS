import sun.awt.Mutex;

import java.util.Arrays;
import java.util.Queue;

/**
 * Created by Papa Formigas on 28-03-2014.
 */
public class ThreadReclaimer implements Runnable{
    Queue<String> commands;
    Mutex commandsMutex;
    long size;
    ThreadReclaimer(Peer peer,long size){
        this.commands = peer.commands;
        this.commandsMutex = peer.commandQueueMutex;
        this.size=size;
    }
    public void run(){
        //System.out.println("Reclaimer was unleashed");
        DoomOfTheDamnedReclaimer reclaimer=new DoomOfTheDamnedReclaimer("BackupFiles",size);
        reclaimer.reclaim();
        commandsMutex.lock();
        //System.out.println("ReclaimerLevel:"+reclaimer.deleted.size());
        for (int i = 0 ; i < reclaimer.deleted.size();i++){
            String[] buff= reclaimer.deleted.elementAt(i).split("/",2);
            String fileHash = buff[0];
            String chunkNo  = buff[1];
            //System.out.println(buff[0] + "*****" + buff[1]);
            commands.add("REMOVED");
            commands.add(buff[0]);
            commands.add(buff[1]);
        }
        //System.out.println(Arrays.toString(commands.toArray()));
        commandsMutex.unlock();

    }
}
