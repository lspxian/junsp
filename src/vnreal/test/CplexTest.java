package vnreal.test;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

public class CplexTest {

	public static void main(String[] args) throws IloException {
		// TODO Auto-generated method stub
		IloCplex cplex = new IloCplex();
		//cplex.importModel("ILP-LP-Models/VNE-ModelC.mod");
		cplex.importModel("ILP-LP-Models/new.sav");
		
	}

}
