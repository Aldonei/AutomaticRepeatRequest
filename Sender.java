/*An abstract class describing what each type of sender
 must provide. The methods are actually implemented by
 the concrete classes corresponding to different ARQ algorithms.
 (i.e. SAW_Sender, GBN_Sender, and SR_Sender).
 The class also provides a generic constructor that does
 initialization of the protected class data which is common
 to all types of senders. */

abstract class Sender {
	protected int numframesToSend;	//the number of frame's to send
	protected int frameSize; //size of information frames
	protected int interframeDelay;	//delay between sent frames
	protected int networkDelay;	//network delay
	protected int numframesSent; //number of frames sent so far

	public Sender(int frames, int size, int delay, int ifd) {
		numframesToSend = frames;
		frameSize = size;
		networkDelay = delay;
		interframeDelay = ifd;
		numframesSent = 0; //no frames sent initially
	}
	public abstract boolean allDone();
	public abstract packet nextframe (long t);
	public abstract void acceptframe (long t, packet f);
}