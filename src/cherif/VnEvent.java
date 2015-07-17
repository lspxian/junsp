package cherif;

import vnreal.network.virtual.VirtualNetwork;

public class VnEvent  implements Comparable<VnEvent>{
	private VirtualNetwork concernedVn;
	private double AoDTime=0.0;	 //Arrival or Departure Time 
	private int flag=0;			//if flag=0 then arrival else departure
	
	public VnEvent (VirtualNetwork concernedVn,double AoDTime,int flag){
		this.concernedVn=concernedVn;
		this.AoDTime=AoDTime;
		this.flag=flag;
	}
	
	public VirtualNetwork getConcernedVn(){
		return concernedVn;
	}
	
	public double getAoDTime(){
		return AoDTime;
	}
	
	public int getFlag(){
		return flag;
	}
	
	public void setConcernedVn(VirtualNetwork vn){
		concernedVn = vn;
	}
	
	public void setAoDTime (double AoDTime){
		this.AoDTime = AoDTime;
	}
	
	public void setFlag(int flag){
		this.flag = flag;
	}
	
	public int compareTo(VnEvent obj){
		//return this.getAoDTime()-obj.getAoDTime();
		
		double time1 = obj.getAoDTime();
		double time2 = this.getAoDTime();
		if(time1>time2) return -1;
		else if (time1 == time2) return 0;
		else  return 1;
	}

	/*public void Mapping(SubstrateNetwork sn,int i)
	{
			System.out.println("virtual network "+i+": \n"+this);
		//node mapping
			AvailableResourcesNodeMapping arnm = new AvailableResourcesNodeMapping(sn,50,true,false);
		
			if(arnm.nodeMapping(this))
			{
				System.out.println("node mapping succes, virtual netwotk "+i);
			}
			else{
				System.out.println("node resource error, virtual network "+i);
	   }*/


}
