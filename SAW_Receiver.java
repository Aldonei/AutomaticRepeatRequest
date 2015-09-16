import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/*A Stop-And-Wait receiver. The SAW_Receiver waits for the
 next I-frame (information frame) in sequence and ACKs it. It ignores any I-frames
 that are out of sequence and relies on the sender's timeout
 mechanism to deal with lost and error frames (of both types).
 This class extends the abstract base class Receiver.*/
class SAW_Receiver extends Receiver  
{
	private JTextArea txaLog = new JTextArea(400, 250);
	private JLabel lbInfo = new JLabel("R E C E I V E R   LOG");
	private JFrame frame = new JFrame();
	
	private boolean	gotGoodIframe;	//indicates next I-frame (information) received so we should ACK it
	private int theIframeSeqNum; //last received I-frame's sequence number

	public SAW_Receiver() {
		super();
		txaLog.setEditable(false);
		txaLog.setFocusable(false);
		txaLog.setLineWrap(true);
		txaLog.setWrapStyleWord(true);
		JScrollPane scpLog = new JScrollPane(txaLog);
		Container container = frame.getContentPane();
		container.add(lbInfo, BorderLayout.NORTH);
		container.add(scpLog, BorderLayout.CENTER);
						
		frame.setTitle("Stop & Wait ARQ");
		frame.setBounds(600, 450, 400, 250);
		frame.setVisible(true);
		gotGoodIframe = false;	//initially nothing has been received
	}
	
	public packet nextframe(){
		packet f = null;
		if (gotGoodIframe){//Looking for next frame to send
			//construct a C-frame (control frame)with an ACK in it and return it to the sender
			f = new Controlpacket("ACK"); //call  Network.java 
			f.setSeqNum(theIframeSeqNum); // ACK with sequence number of sent frame
			gotGoodIframe = false;
			txaLog.append("-> ACKing frame with sequence # = " + theIframeSeqNum + "\n");
		}
		return(f);
	}
	
	//Method to accept frame
	public void acceptframe (packet f){
		if (!f.error()){ //if NOT an error
			if (theIframeSeqNum == f.seqNum()){
				; // duplicate frame - ignore it
			} 
			else
				// new frame so record the sequence number
				theIframeSeqNum = f.seqNum();
			//set flag so we ACK this I frame in the next cycle (even if it is a duplicate)
			gotGoodIframe = true;
			txaLog.append(" Waiting for frame with sequence # = " + theIframeSeqNum + "\n");
			txaLog.append("->> Got a frame with sequence # = " + theIframeSeqNum + "\n");
		} 
		else
			txaLog.append("->>> Bad I-frame with sequence # = " + f.seqNum() + "\n");
			 //just let the sender's timeout handle a bad I frame
	}
}