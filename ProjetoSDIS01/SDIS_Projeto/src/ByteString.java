/**
 * Created by Leonel Araujo on 27-03-2014.
 */
public class ByteString {
    byte[] myByte;
    String c;
    ByteString(byte[] a){
        myByte = new byte[a.length];
        for ( int i = 0 ; i < a.length ; i++){
            myByte[i]=a[i];
        }


    }

    ByteString[] split(byte splitChar,int maxSplit){
        ByteString array = this;
        int[] posSplit = new int[maxSplit-1];
        int j=0;
        for ( int i = 0; i < array.length() && j < maxSplit-1; i++){
            if(array.getBytes()[i] == splitChar){
                posSplit[j++] = i;
            }
        }
        int prevPos =0;
        ByteString[] aux = new ByteString[j+1];

        this.c = new String(myByte);
        for(int i = 0 ; i < j ;i++){
            aux[i]=substring(prevPos,posSplit[i]);
            prevPos = posSplit[i]+1;
        }
        aux[aux.length-1] = substring(prevPos,this.length());
        return aux;
    }

    ByteString substring(int posInicial, int posFinal){
        this.c = new String(myByte);
        byte[] result = new byte[posFinal-posInicial];

        for(int i = posInicial, j = 0; i < posFinal; i++,j++){
            result[j] = myByte[i];

        }

        ByteString res = new ByteString(result);
        return res;
    }
    void add(ByteString b){
        ByteString a = this;
        byte[] full=new byte[this.length()+b.length()];
        int i;
        for(i = 0 ; i < a.length() ; i++){
            full[i]=a.getBytes()[i];
        }
        int j;
        for (j=0; j < b.length() ; j++,i++){
            full[i] = b.getBytes()[j];
        }

        myByte = full;
    }

    void add(byte[] b){
        ByteString f = new ByteString(b);
        add(f);
    }
    byte[] getBytes(){
        return myByte;
    }

    int length(){
        return myByte.length;
    }

}
