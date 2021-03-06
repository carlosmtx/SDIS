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

    Peer temp;
    Vector<LogBackup> backupLog;
    int msg;

    Vector<String> egg;
    ThreadMenu(Peer p){
        this.commandQueueMutex = p.commandQueueMutex;
        this.commands = p.commands;
        this.backupLog = p.backupLog;
        read = new Scanner(System.in);

        egg = new Vector<String>();
        egg.add("Backup Ficheiros SDIS 2013-2014");
        egg.add("Cabecinha Pensadoooora         ");
        egg.add("Vai la vai  ate a barraca abana");
        egg.add("Oh Costa  a vida Costa         ");
        egg.add("Quimico  Natural ou Assim Assim");
        egg.add("Directamente da Tailandia      ");
        egg.add("Isto e que vai aqui uma acorda ");
        egg.add("Bom e barato  so no Barata     ");

        this.temp = p;
        msg = 0;

    }
    void clearConsole(){

    }
    void mainMenu()throws Exception{
        clearConsole();
        int choice=0;
        Random l = new Random();
        System.out.print(""+
                "\n  =========================================== "+
                "\n||   " + egg.get(msg)+ "  " + Peer.version +"   ||"+
                "\n||   rep.degree=" + Peer.repDegree + " chunkSize= " + Peer.chunkSize +"            ||" +
                "\n  =========================================== "+
                "\n" +

                "1-Backup de Ficheiro\n" +
                "2-Recuperar Ficheiro\n" +
                "3-Apagar Ficheiro\n" +
                "4-Backup de BigFiles\n" +
                "5-Reclamar Espaco\n"+
                "6-Actualizar Deletes\n"+
                "7-Definicoes\n"+
                "8-Sair\n");
        try{
            choice =read.nextInt();
        }
        catch(InputMismatchException exp){System.out.println("Invalid Input");read.nextLine();}
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
            case 7:
                settingsMenu();
                break;
            case 5:
                spaceReclaimMenu();
                break;
            case 6:
                refreshDeleteData();
                break;
            case 4:
                bigfileMenu();
                break;
            case 8:
                throw new Exception();
            case 420:
                msg = l.nextInt(egg.size()-1);
                System.out.println("EasterEgg It");
                break;
            default:
                break;
        }
    }

    void printDeleteInfo(){
        System.out.println("Deletes:");
        for(int i = 0; i < temp.DeleteRecords.size(); i++){
            System.out.println(i + "- " + temp.DeleteRecords.get(i));
        }
    }

    public void bigfileMenu(){
        int choice=0;
        do{
            clearConsole();
            System.out.print("" +
                    "1-Seleccionar Ficheiro\n" +
                    "2-Voltar ao Menu Inicial\n"
            );
            try{choice =read.nextInt();}
            catch(InputMismatchException exp){read.reset();System.out.println("Invalid Input");}
        }
        while(choice >2 && choice <1);
        switch (choice){
            case 1:
                bigfileGetFileMenu();
                break;
            case 2:
                break;
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
            catch(InputMismatchException exp){System.out.println("Invalid Input") ; read.nextLine();}
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
    public void spaceReclaimMenu(){
        int size=0;
        do{
            clearConsole();
            System.out.print("" +
                    "Insira o espaco a libertar(KB):\n"
            );
            try{size =read.nextInt();}
            catch(InputMismatchException exp){read.reset();System.out.println("Invalid Input"); read.nextLine();}
        }
        while(size < 0);

        commandQueueMutex.lock();
        commands.add("RECLAIM");
        commands.add(""+size);
        commandQueueMutex.unlock();
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
            catch(InputMismatchException exp){read.reset();System.out.println("Invalid Input"); read.nextLine();}
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
            catch(InputMismatchException exp){read.reset();System.out.println("Invalid Input"); read.nextLine();}
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
                "2-Alterar Tamanho de Chunk\n" +
                "3-Voltar ao Menu Inicial\n"
        );
        try{choice =read.nextInt();}
        catch(InputMismatchException exp){System.out.println("Invalid Input");read.nextLine();return;}
        if(choice == 1){
            System.out.print("Introduza o grau de replicacao:  ");
        }
        else if(choice == 2){
            System.out.print("Introduza o tamanho de chunk:  ");
        }
        else if(choice == 3){
            return;
        }
        int newValue;
        try{
            newValue =read.nextInt();
        }
        catch(InputMismatchException exp){System.out.println("Invalid Input");read.nextLine();return;}

        if(choice == 1) Peer.repDegree = newValue;
        if(choice == 2) Peer.chunkSize = newValue;
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

    public void bigfileGetFileMenu(){
        System.out.println("Insira o caminho do ficheiro:");
        read.nextLine();
        String path = read.nextLine();
        File f = new File(path);
        if(f.exists() && !f.isDirectory()) {addBigFileCommand(path);}
        else{System.out.println("Path Invalido:Ficheiro nao existe ou e um directorio");}
    }

    public void deleteMenuGetFile(){
        System.out.println("Insira o nome do ficheiro a apagar:");
        read.nextLine();
        String fileName = read.nextLine();
        addDeleteCommand(fileName);
    }
    private void addBackupCommand(String path){
        commandQueueMutex.lock();
        commands.add("BACKUP");
        commands.add(path);
        commandQueueMutex.unlock();
    }

    private void addBigFileCommand(String path){
        commandQueueMutex.lock();
        commands.add("BIGFILE");
        commands.add(path);
        commandQueueMutex.unlock();
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
        commands.add("RESTORE"); // para abrir socketServ de rececao
        commands.add(fileid);
        commandQueueMutex.unlock();

    }
    private void addDeleteCommand(String filename){
        boolean fileFound = false;
        for(int i = 0; i < backupLog.size(); i++){
            if(backupLog.get(i).fileName.equals(filename)){
                fileFound = true;
                filename=backupLog.get(i).hashName;
                break;
            }
        }
        commandQueueMutex.lock();
        commands.add("DELETE");
        commands.add(filename);
        commandQueueMutex.unlock();
        //System.out.println(Arrays.toString(commands.toArray()));
    }

    public void refreshDeleteData(){
        commandQueueMutex.lock();
        commands.add("GETDELETE");
        commandQueueMutex.unlock();
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
