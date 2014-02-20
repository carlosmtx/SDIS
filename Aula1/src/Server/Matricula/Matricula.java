package Server.Matricula;

public class Matricula {
	String matricula;

	public Matricula(String b){

        /*
		String[] mat=matricula.split("-");
		this.matricula[0] = Integer.parseInt(mat[0]);
		this.matricula[1] = Integer.parseInt(mat[1]);
		this.matricula[2] = Integer.parseInt(mat[2]);
		*/

        this.matricula = b;
		
	}
	
	boolean equals(Matricula b){
		return this.matricula == b.matricula;
	}
	
	
}
