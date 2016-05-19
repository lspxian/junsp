package li.event;

import vnreal.network.virtual.VirtualNetwork;

public class VnEvent extends NetEvent {
	private VirtualNetwork concernedVn;
	private int flag=0;			//if flag=0 then arrival else departure
	
	public VnEvent (VirtualNetwork concernedVn,double AoDTime,int flag){
		super(AoDTime);
		this.concernedVn=concernedVn;
		this.flag=flag;
	}
	
	public VirtualNetwork getConcernedVn(){
		return concernedVn;
	}
	
	public int getFlag(){
		return flag;
	}
	
	public void setConcernedVn(VirtualNetwork vn){
		concernedVn = vn;
	}
	
	public void setFlag(int flag){
		this.flag = flag;
	}


}
