package vnreal.evaluations.metrics;

public class AcceptedRatio {
	
	public double calculate(int acepted, int rejected ){
		return ((double)acepted/(double)(acepted + rejected))*100.0;
	}
	
	

}
