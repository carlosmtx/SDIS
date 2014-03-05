import sun.awt.Mutex;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Stack;

/**
 * Created by Papa Formigas on 05-03-2014.
 */
public class ThreadMenu implements Runnable{
    Mutex commandStackMutex;
    Stack<String> commands;
    Scanner read;
    ThreadMenu(Mutex mut,Stack<String> comm){
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
                    Runtime.getRuntime().exec("cls.exe").waitFor();
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

    }
    public void restoreMenu(){

    }
    public void fileDeletion(){

    }
    public void settingsMenu(){

    }
    public void run(){
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
