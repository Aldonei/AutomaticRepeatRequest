import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
/* The GBNReceiver ignores any I-frames (information)
 in error (since these will be re-sent by the sender) and
 ACKs all frames it receives (including duplicates) in
 case the ACKs were previously lost. The code does NOT use NAKs,
 instead it uses time out.
 This class extends the abstract base class Receiver.*/

class GBN_Receiver extends Receiver {
	private JTextArea txaLog = new JTextArea(400, 250);
	private JLabel lbInfo = new JLabel("R E C E I V E R   LOG");
	private JFrame frame = new JFrame();
	
	private boolean	gotIframe;	//We got an I-frame and should ACK it
	private int theIframeSeqNum;//I-frame sequence number to ACK
	private int	lastGoodSeqNum;	//Highest sequence number of a received I-frame

	public GBN_Receiver() {
		super();
		txaLog.setEditable(false);
		txaLog.setFocusable(false);
		txaLog.setLineWrap(true);
		txaLog.setWrapStyleWord(true);
		JScrollPane scpLog = new JScrollPane(txaLog);
		Container container = frame.getContentPane();
		container.add(lbInfo, BorderLayout.NORTH);
		container.add(scpLog, BorderLayout.CENTER);
						
		frame.setTitle("Go back N ARQ - Reciver");
		frame.setBounds(600, 450, 400, 250);
		frame.setVisible(true);
		gotIframe = false;	//initially no I-frame received
		lastGoodSeqNum = 0;	//initially no last good sequence number
	}
	//Call class called packet.java (seqNo, injectTime, inError)
	public packet nextframe () {
		packet f = null;
		if (gotIframe) {//If false
			//construct a C-frame with an ACK in it and return it to the sender
			f = new Controlpacket("ACK"); //Sets the variable code = ACK inside Controlpacket.java
			//Call a method inside packet.java where sets the sequence number of a frame
			f.setSeqNum(theIframeSeqNum); // ACK with sequence number of sent frame
			gotIframe = false; //Set false again for the next frame
			//Write in a log
			txaLog.append("-> ACKing frame with seq# = " + theIframeSeqNum + "\n");
		}
		return (f);//Return the sequence number
	}

	public void acceptframe (packet f) {
		if (!f.error()) {//If NOT an error is false 
			txaLog.append("->> Out of sequence frame with seq# = " + f.seqNum() + "\n");
			theIframeSeqNum = lastGoodSeqNum; //out of sequence frame so resend old ACK
			lastGoodSeqNum++;
			theIframeSeqNum = f.seqNum();
			//set flag so we ACK this I frame in the next cycle
			gotIframe = true;
			txaLog.append(" Waiting for frame with seq# = " + theIframeSeqNum + "\n");
			txaLog.append("->> Got a frame with seq# = " + f.seqNum() + "\n");//Informe that got a frame
		} 
		else {
			txaLog.append("->>> Bad I-frame with seq# = " + f.seqNum() + "\n");
			//we will just let the sender's timeout handle a bad I frame
		}
	}
}