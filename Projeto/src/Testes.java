/**
 * Created by Leonel Araujo on 26-02-2014.
 */
public class Testes {

    public static void main (String[] args){
        try{
            Ficheiro f1 = new Ficheiro("c://testfile.txt", 256);
            Ficheiro f2 = new Ficheiro("c://testfile.txt", 10);
            Ficheiro f3 = new Ficheiro("c://testimg.png", 526);


            System.out.println("Curtou o ficheiro em " + f1.getChunks().size() + " chunks.");
            System.out.println("Curtou o ficheiro em " + f2.getChunks().size() + " chunks.");
            System.out.println("Curtou o ficheiro em " + f3.getChunks().size() + " chunks.");


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
