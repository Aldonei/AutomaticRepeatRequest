import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/*A stop and wait sender: sends another frame if it has one to send
 (i.e. it is not done sending and the time has come to send one) if
 it has received an ACK for the previous frame sent. This class
 extends the abstract base class Sender.*/

class SAW_Sender extends Sender {
	private JTextArea txaLog = new JTextArea(400, 250);
	private JLabel lbInfo = new JLabel("S E N D E R   LOG");
	private JFrame frame = new JFrame();
	private boolean	lastframeACKd;	//was the last frame ACKnowledged?
	private long timeoutTime; //timeout if no ACK by this time.
	private packet savedframe; //saved copy of last frame sent
	private long timeOfLastSend; //time when last frame was sent

	public SAW_Sender(int frames, int size, int netDelay, int interframeDelay) {
		super(frames, size, netDelay, interframeDelay);
		txaLog.setEditable(false);
		txaLog.setFocusable(false);
		txaLog.setLineWrap(true);
		txaLog.setWrapStyleWord(true);
		JScrollPane scpLog = new JScrollPane(txaLog);
		Container container = frame.getContentPane();
		container.add(lbInfo, BorderLayout.NORTH);
		container.add(scpLog, BorderLayout.CENTER);
						
		frame.setTitle("Stop & Wait ARQ");
		frame.setBounds(10, 450, 400, 250);
		frame.setVisible(true);

		lastframeACKd = true; //make sure we can send the first frame
		timeOfLastSend = -networkDelay;// make sure we can send the first frame
	}

	public packet nextframe (long t) {
		packet f = null;
		if (lastframeACKd){
			/* this is stop and wait so we must wait until the last
			 frame has been acknowledged by the receiver */
			if ((numframesSent < numframesToSend) && (t >= timeOfLastSend + interframeDelay)) {
				//we have a frame to send and it is time to send it
				f = new Informationpacket(frameSize);
				f.setSeqNum(numframesSent + 1); //start at 1 not 0
				// could fill in information frame data here (if desired)
				savedframe = f; // save a copy for re-sends
				timeoutTime = t + 2*networkDelay + 5; // round trip time plus 5 extra msecs
				lastframeACKd = false;
				numframesSent++;
				timeOfLastSend = t;
				txaLog.append("Time is: " + t + "\n");
				txaLog.append("-> Sending frame with sequence # = " + f.seqNum() + "\n");
			}
		} 
		else if (t > timeoutTime) {
			//handle time out by resending
			f = savedframe;
			timeoutTime = t + 2*networkDelay + 5; // reset timeout
			timeOfLastSend = t; //resends count as sends too
			txaLog.append("Time is: " + t + "\n");
			txaLog.append("->> Retransmitting after timeout. Sequence # = " + f.seqNum() + "\n");
		}
		return (f);
	}

	public void acceptframe (long t, packet f) {
		//look for a C-frame containing the ACK for our previously sent I frame
		if (!f.error()) {
			lastframeACKd = true;
			txaLog.append("Time is: " + t + "\n");
			txaLog.append("->>> Got ACK for frame with sequence # = " + f.seqNum() + "\n");
		} 
		else {
			txaLog.append("Time is: " + t + "\n");
			txaLog.append("->>>> Got a bad C-frame with sequence # = " + f.seqNum() + "\n");
			timeoutTime = t; //set timeout time to now to force resend
		}
	}
    //allDone is called in the mainline to see if the simulation is over
	public boolean allDone(){
		return (lastframeACKd && (numframesSent >= numframesToSend));
	}
}