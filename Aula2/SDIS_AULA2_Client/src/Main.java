/**
 * Created by Papa Formigas on 22-02-2014.
 */

public class Main {
    public static void main (String[] args){
        Client client = null;
        String get;
        System.out.println("-Creating Socket");

        try{client = new Client("225.1.1.1",2048);}            catch(Exception e){e.printStackTrace();return;}

        while(true){
            try{ client.receive(); }catch(Exception e){e.printStackTrace();}
        }

        /*
        System.out.println("-Socket Created");

        try{client.registerPlate("04-06-BC" ,"LeonelRochinha");}    catch(Exception e){e.printStackTrace();return;}
        System.out.println("-Registry Request sent");
        System.out.println("-Sending registry request");

        try{client.registerPlate("05-08-CD" ,"LeaoRocha");}         catch(Exception e){e.printStackTrace();return;}
        System.out.println("-Registry Request sent");

        System.out.println("-Sending Lookup request");
        try{get=client.getOwnerAssociated("05-08-CD");}            catch(Exception e){e.printStackTrace();return;}
        System.out.println("-Registry Request sent");
        System.out.println("-Response:"+get);
        */
    }
}
