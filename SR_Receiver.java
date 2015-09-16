import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/* A Selective repeated receiver. The SR_Receiver waits for the
 next I-frame in sequence and ACKs it. It accepts any I-frames
 that are out of sequence. If error oocurs, signal SR_Sender to resend this frame.
 This class extends the abstract base class Receiver.*/

class SR_Receiver extends Receiver {
	private JTextArea txaLog = new JTextArea(400, 250);
	private JLabel lbInfo = new JLabel("R E C E I V E R   LOG");
	private JFrame frame = new JFrame();
	private boolean	gotGoodIframe; //indicates next I-frame received so we should ACK it
	private int theIframeSeqNum; //last received I-frame's sequence number (information frame)

	public SR_Receiver() {
		super();
		txaLog.setEditable(false);
		txaLog.setFocusable(false);
		txaLog.setLineWrap(true);
		txaLog.setWrapStyleWord(true);
		JScrollPane scpLog = new JScrollPane(txaLog);
		Container container = frame.getContentPane();
		container.add(lbInfo, BorderLayout.NORTH);
		container.add(scpLog, BorderLayout.CENTER);
		
		frame.setTitle("Selective Repeat ARQ");
		frame.setBounds(600, 450, 400, 250);
		frame.setVisible(true);
		gotGoodIframe = false; //initially nothing has been received
	}

	public packet nextframe () {
		packet f = null;
		if (gotGoodIframe) { //If false
			//construct a C-frame with an ACK in it and return it to the sender
			f = new Controlpacket("ACK");
			f.setSeqNum(theIframeSeqNum); //ACK with sequence number of sent frame
			gotGoodIframe = false;
			txaLog.append("-> ACKing frame with seq# = " + theIframeSeqNum + "\n");
		}
		return (f);//Return f value
	}

	public void acceptframe (packet f) 
	{
		if (!f.error()) {//If NOT an error
			if (theIframeSeqNum == f.seqNum())
				; //duplicate frame - ignore it
			else {
				//new frame so record the sequence number
				theIframeSeqNum = f.seqNum();
			}
			// set flag so we ACK this I frame in the next cycle (even if it is a duplicate)
			gotGoodIframe = true;
			txaLog.append(" Waiting for frame with sequence # = " + theIframeSeqNum + "\n");
			txaLog.append("->> Got a frame with seq# = " + theIframeSeqNum + "\n");
		} 
		else {
			txaLog.append("->>> Bad I-frame with seq# = " + f.seqNum() + "\n");
			 //we will just let the sender's timeout handle a bad I frame
		}
	}
}