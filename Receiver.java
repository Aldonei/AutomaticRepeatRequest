/*An abstract class describing what each type of receiver
 must provide. The methods are actually implemented by
 the concrete classes corresponding to different ARQ algorithms.
 (i.e. SAWReceiver, GBNReceiver, and (later) SRReceiver)*/
abstract class Receiver {
	public abstract packet nextframe();
	public abstract void acceptframe(packet f);
}