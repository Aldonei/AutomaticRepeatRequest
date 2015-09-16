/*Describes a generic network frame which may be specialized by
 either Controlpacket to be a C-frame or
 I-frame, respectively.*/

class packet {
	private int	seqNo; // the frame's sequence number
	private long injectTime; // simulated time of frame injection
	private boolean	inError; // was an error incurred in sending this frame?
	// Accessor and Updater methods
	public int seqNum(){
		return (seqNo);
	}

	public void setSeqNum(int s){
		seqNo = s;
	}

	public long injectionTime(){
		return (injectTime);
	}

	public void setInjectionTime(long t){
		injectTime = t;
	}

	public boolean error(){
		return (inError);
	}

	public void setError(boolean err){
		inError = err;
	}
}