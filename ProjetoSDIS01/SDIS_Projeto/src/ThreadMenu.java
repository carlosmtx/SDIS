import sun.awt.Mutex;

import java.io.File;
import java.util.*;

/**
 * Created by Papa Formigas on 05-03-2014.
 */
public class ThreadMenu implements Runnable{
    Mutex commandQueueMutex;
    Queue<String> commands;
    Scanner read;

    Vector<LogBackup> backupLog;

    Vector<String> egg;
    ThreadMenu(Peer p){
        this.commandQueueMutex = p.commandQueueMutex;
        this.commands = p.commands;
        this.backupLog = p.backupLog;
        read = new Scanner(System.in);

        /* egg */
        egg = new Vector<String>();
        egg.add("Cabecinha Pensadoooora         ");
        egg.add("Vai la vai, ate a barraca abana");
        egg.add("Oh Costa, a vida Costa         ");
        egg.add("Quimico, Natural ou Assim-Assim");
        egg.add("Directamente da Tailandia      ");
        egg.add("Isto e que vai aqui uma açorda ");
        egg.add("Bom e barato, só no Barata     ");

    }
    void clearConsole(){
        try
        {
            String os = System.getProperty("os.name");
            if (os.contains("Windows"))
            {
                Runtime.getRuntime().exec("call cls").waitFor();
            }
            else
            {
                Runtime.getRuntime().exec("clear");
            }
        }
        catch (Exception exception)
        {}
    }
    void mainMenu()throws Exception{
        clearConsole();
        int choice=0;
        Random l = new Random();
        int msg = l.nextInt(egg.size()-1);

        System.out.println("COMO TA O BACKUPLOG:");
        for(int i = 0; i < backupLog.size(); i++){
            System.out.println(backupLog.get(i).fileName + " ; " + backupLog.get(i).noChunks);
        }

        System.out.print(""+
                "\n  =========================================== "+
                "\n||   " + egg.get(msg)+ "  " + Peer.version +"   ||"+
                "\n||   rep.degree=" + Peer.repDegree + " chunkSize= " + Peer.chunkSize +"            ||" +
                "\n  =========================================== "+
                "\n" +

                "1-Backup de Ficheiro\n" +
                "2-Recuperar Ficheiro\n" +
                "3-Apagar Ficheiro\n" +
                "4-Definicoes\n" +
                "5-Sair\n");
        try{choice =read.nextInt();}
        catch(InputMismatchException exp){System.out.println("Invalid Input");}
        switch (choice){
            case 1:
                backupMenu();
                break;
            case 2:
                restoreMenu();
                break;
            case 3:
                fileDeletion();
                break;
            case 4:
                settingsMenu();
                break;
            case 5:
                throw new Exception();
        }
    }


    public void backupMenu(){
        int choice=0;
        do{
            clearConsole();
            System.out.print("" +
                    "1-Fazer Backup de ficheiro\n" +
                    "2-Voltar ao Menu Inicial\n"
            );
            try{choice =read.nextInt();}
            catch(InputMismatchException exp){read.reset();System.out.println("Invalid Input");}
        }
        while(choice >2 && choice <1);
        switch (choice){
            case 1:
                backupMenuGetFile();
                break;
            case 2:
                break;
        }
    }
    public void restoreMenu(){
        int choice=0;
        do{
            clearConsole();
            System.out.print("" +
                    "1-Recuperar Ficheiro\n" +
                    "2-Voltar ao Menu Inicial\n"
            );
            try{choice =read.nextInt();}
            catch(InputMismatchException exp){read.reset();System.out.println("Invalid Input");}
        }
        while(choice >2 && choice <1);
        switch (choice){
            case 1:
                restoreMenuGetFile();
                break;
            case 2:
                break;
        }
    }
    public void fileDeletion(){
        int choice=0;
        do{
            clearConsole();
            System.out.print("" +
                    "1-Apagar Ficheiro\n" +
                    "2-Voltar ao Menu Inicial\n"
            );
            try{choice =read.nextInt();}
            catch(InputMismatchException exp){read.reset();System.out.println("Invalid Input");}
        }
        while(choice >2 && choice <1);
        switch (choice){
            case 1:
                deleteMenuGetFile();
                break;
            case 2:
                break;
        }
    }
    public void settingsMenu(){
        int choice;
        System.out.print("" +
                "1-Alterar Grau de Replicacao\n" +
                "2-Voltar ao Menu Inicial\n"
        );
        try{choice =read.nextInt();}
        catch(InputMismatchException exp){System.out.println("Invalid Input");return;}
        if(choice == 1){
            System.out.print("Introduza o grau de replicacao:  ");
        }
        else if(choice == 2){
            return;
        }
        try{
            choice =read.nextInt();
        }
        catch(InputMismatchException exp){System.out.println("Invalid Input");return;}

        Peer.repDegree = choice;
    }

    public void backupMenuGetFile(){
        System.out.println("Insira o caminho do ficheiro:");
        read.nextLine();
        String path = read.nextLine();
        File f = new File(path);
        if(f.exists() && !f.isDirectory()) {addBackupCommand(path);}
        else{System.out.println("Path Invalido:Ficheiro nao existe ou e um directorio");}
    }
    public void restoreMenuGetFile(){
        System.out.println("Insira o nome do ficheiro a recuperar:");
        read.nextLine();
        String fileName = read.nextLine();
        boolean fileFound = false;

        for(int i = 0; i < backupLog.size(); i++){
            if(backupLog.get(i).fileName.equals(fileName)){
                fileFound = true;
                addRestoreCommand(backupLog.get(i).hashName);
                break;
            }
        }

        if(!fileFound){
            System.out.println("Ficheiro nao encontrado.");
        }
    }
    public void deleteMenuGetFile(){
        System.out.println("Insira o nome do ficheiro a apagar:");
        read.nextLine();
        String fileName = read.nextLine();
        boolean fileFound = false;

        for(int i = 0; i < backupLog.size(); i++){
            if(backupLog.get(i).fileName.equals(fileName)){
                fileFound = true;
                addDeleteCommand(backupLog.get(i).hashName);
                break;
            }
        }

        if(!fileFound){
            System.out.println("Ficheiro nao encontrado.");
        }

    }

    private void addBackupCommand(String path){
        commandQueueMutex.lock();
        commands.add("BACKUP");
        commands.add(path);
        commandQueueMutex.unlock();
        //System.out.println(Arrays.toString(commands.toArray()));
    }
    private void addRestoreCommand(String filename){
        String fileid = filename;

        LogBackup aux = null;
        for(int i = 0; i < backupLog.size(); i++){
            if(backupLog.get(i).hashName.equals(fileid)){
                aux = backupLog.get(i);
            }
        }

        commandQueueMutex.lock();
        commands.add("RESTORE"); // para abrir socket de rececao
        commands.add(fileid);
        commandQueueMutex.unlock();

        for(int i = 0; i < aux.noChunks; i++){
            commandQueueMutex.lock();
            commands.add("GETCHUNK");
            commands.add(fileid);
            commands.add(""+i);
            commandQueueMutex.unlock();
        }
    }
    private void addDeleteCommand(String fileid){
        commandQueueMutex.lock();
        commands.add("DELETE");
        commands.add(fileid);
        commandQueueMutex.unlock();
        //System.out.println(Arrays.toString(commands.toArray()));
    }


    public void run(){
        try{
            while(true){
                mainMenu();
            }
        }
        catch(Exception e){
            Peer.endProgram=true;
            System.out.println("Exiting...");
        }
    }
}
