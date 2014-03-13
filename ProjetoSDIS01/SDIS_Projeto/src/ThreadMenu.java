import sun.awt.Mutex;

import java.io.File;
import java.util.*;

/**
 * Created by Papa Formigas on 05-03-2014.
 */
public class ThreadMenu implements Runnable{
    Mutex commandStackMutex;
    Queue<String> commands;
    Scanner read;
    ThreadMenu(Mutex mut,Queue<String> comm){
        this.commandStackMutex =mut;
        this.commands = comm;
        read = new Scanner(System.in);
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
        boolean end=false;
        int choice=0;
        System.out.print("" +
                "1-File backup\n" +
                "2-File restore\n" +
                "3-File deletion\n" +
                "4-Settings\n" +
                "5-Exit\n");
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
                    "1-Fazer backup\n" +
                    "2-Voltar\n"
            );
            try{choice =read.nextInt();}
            catch(InputMismatchException exp){System.out.println("Invalid Input");}
        }
        while(choice >2 && choice <1);
        switch (choice){
            case 1:
                 backupMenuGetFile();
                break;
            case 2:
                restoreMenu();
                break;
        }
    }
    public void restoreMenu(){

    }
    public void fileDeletion(){

    }
    public void settingsMenu(){

    }

    public void backupMenuGetFile(){
        System.out.println("Insira o caminho do ficheiro:");
        read.nextLine();
        String path = read.nextLine();

        File f = new File(path);
        if(f.exists() && !f.isDirectory()) {addBackupCommand(path);}
        else{System.out.println("Path Invalido:Ficheiro nao existe ou e um directorio");}
    }

    private void addBackupCommand(String path){
        commandStackMutex.lock();
        commands.add("BACKUP");
        commands.add(path);
        commandStackMutex.unlock();
        System.out.println(Arrays.toString(commands.toArray()));
    }
    public void run(){
        System.out.println("Called Run(Thread Menu)");
        try{
            while(true){
                mainMenu();
            }
        }
        catch(Exception e){
            System.out.println("Exiting...");
        }
    }
}
