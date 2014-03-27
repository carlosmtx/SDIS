/**
 * Created by Leonel Araujo on 27-03-2014.
 */
public class ByteString {
    final byte[] myByte;
    ByteString(byte[] a){
        myByte = new byte[a.length];
        for ( int i = 0 ; i < a.length ; i++){
            myByte[i]=a[i];
        }
    }

    ByteString[] split(byte splitChar,ByteString array,int maxSplit){
        int[] posSplit = new int[maxSplit];
        int j=0;
        for ( int i = 0; i < array.length() && maxSplit < j; i++){
            if(array.getBytes()[i] == splitChar){
                posSplit[j++] = i;
            }
        }
        ByteString[] odeioJavaAQuantidadeDeAbstracoesETaoGrandeQueJaNinguemPercebeNadaQueSeLixeVamosInventarARoda=new ByteString[j];
        for ( int i = 0 ; i < j ; i++){
            if(i+1 < j){
                odeioJavaAQuantidadeDeAbstracoesETaoGrandeQueJaNinguemPercebeNadaQueSeLixeVamosInventarARoda[i] = substring(posSplit[i],posSplit[i+1]);
            }
            else{
                odeioJavaAQuantidadeDeAbstracoesETaoGrandeQueJaNinguemPercebeNadaQueSeLixeVamosInventarARoda[i] = substring(posSplit[i],posSplit[array.length()-1]);
            }
        }
        return odeioJavaAQuantidadeDeAbstracoesETaoGrandeQueJaNinguemPercebeNadaQueSeLixeVamosInventarARoda;
    }

    ByteString substring(int posInicial, int posFinal){

        byte[] result = new byte[posFinal-posInicial];

        for(int i = posInicial, j = 0; i < posFinal; i++){
            result[j] = myByte[i];
            j++;
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
        for (j=i; i < b.length() ; i++){
            full[j] = b.getBytes()[i];
        }

    }
    byte[] getBytes(){
        return myByte;
    }

    int length(){
        return myByte.length;
    }

}
