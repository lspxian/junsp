package li.event;

public class NetEvent implements Comparable<NetEvent>{
	private double AoDTime=0.0;	 //Arrival or Departure Time 

	public NetEvent(double time){
		this.AoDTime = time;
	}
	
	public double getAoDTime() {
		return AoDTime;
	}

	public void setAoDTime(double aoDTime) {
		AoDTime = aoDTime;
	}
	
	public int compareTo(NetEvent obj){
		//return this.getAoDTime()-obj.getAoDTime();
		
		double time1 = obj.getAoDTime();
		double time2 = this.getAoDTime();
		if(time1>time2) return -1;
		else if (time1 == time2) return 0;
		else  return 1;
	}
}
