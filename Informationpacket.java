/*Information packet - specializes packet
 to add the actual data sent (not yet used).*/
class Informationpacket extends packet {
	private byte [] frameData = null;
	Informationpacket (int size) {
		frameData = new byte [size]; //allocate space for data
	}
}