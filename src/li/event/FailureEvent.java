package li.event;

import vnreal.network.substrate.SubstrateLink;

public class FailureEvent extends NetEvent {

	SubstrateLink failureLink;
	
	public FailureEvent(double time,SubstrateLink sl) {
		super(time);
		this.failureLink=sl;
	}

	public SubstrateLink getFailureLink() {
		return failureLink;
	}
	
}
