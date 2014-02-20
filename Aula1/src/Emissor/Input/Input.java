package Emissor.Input;

/**
 * Created by Leonel Araujo on 20-02-2014.
 */
public class Input {
    public String host;
    public String port_number;
    public String operacao;
    public String matricula;
    public String proprietario;


    public Input(String host, String port_number, String operacao, String matricula){
        this.host = host;
        this.port_number = port_number;
        this.operacao = operacao;
        this.matricula = matricula;
        this.proprietario = null;
    }

    public Input(String host, String port_number, String operacao, String matricula, String proprietario){
        this.host = host;
        this.port_number = port_number;
        this.operacao = operacao;
        this.matricula = matricula;
        this.proprietario = proprietario;
    }

    public Input(String input){
        //TODO: partir string;
    }

    public Input(Input in){
        this.host = in.host;
        this.port_number = in.port_number;
        this.operacao = in.operacao;
        this.matricula = in.matricula;
        this.proprietario = in.proprietario;
    }

    @Override
    public String toString(){
        if(this.proprietario == null){
            return "[" + this.operacao + ", " + this.matricula + "]";
        }
        else if(this.proprietario != null) {
            return "[" + this.operacao + ", " + this.matricula + ", " + this.proprietario + "]";
        }
        return "Error";
    }
}
