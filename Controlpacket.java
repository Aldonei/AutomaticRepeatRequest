/* Controlpacket - specializes Netpacket to 
add a control code (e.g. ACK, NACK, etc.) */
class Controlpacket extends packet{
	private String code = null;
	public Controlpacket(String type){
		code = type; //set C-packet type at creation time
	}

	public String type(){
		return(code);
	}
}