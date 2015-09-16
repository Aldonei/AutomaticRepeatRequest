import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
/*A Go-Back-N sender. The variables savedpackets and timeoutTimes
 constitute the sender's window. The oldest non-ACK'd packet is always
 in element 0 and there are numSentInWindow valid entries in the
 arrays. When an error is detected (or an ACK is lost) the timeout
 value in element 0 will "expire" and the sender will resend all
 the frames in its window. This class extends the abstract
 base class Sender.*/

class GBN_Sender extends Sender{
	private JTextArea txaLog = new JTextArea(400, 250);
	private JLabel lbInfo = new JLabel("S E N D E R   LOG");
	private JFrame frame = new JFrame();
	
	private int n;//the window size
	private int	numSentInWindow;//how many frames are outstanding ( <= n )
	private int	numToResend; //number of frames to resend after timeout
	private int	lastframeACKd; //sequence # of last frame ACKnowledged
	private packet [] savedframes; //saved copies of frames sent but not ACK'd
	private long []	timeoutTimes; //timeout if no ACK by this time for the frame
	private long timeOfLastSend; //time when last frame was sent

	public GBN_Sender(int frames, int size, int netDelay, int interframeDelay, int ws){
		super(frames, size, netDelay, interframeDelay);
		txaLog.setEditable(false);
		txaLog.setFocusable(false);
		txaLog.setLineWrap(true);
		txaLog.setWrapStyleWord(true);
		JScrollPane scpLog = new JScrollPane(txaLog);
		Container container = frame.getContentPane();
		container.add(lbInfo, BorderLayout.NORTH);
		container.add(scpLog, BorderLayout.CENTER);
						
		frame.setTitle("Go Back N ARQ - Sender");
		frame.setBounds(10, 450, 400, 250);
		frame.setVisible(true);
		
		n = ws; //set the window size
		numSentInWindow = 0; //initially, no frames sent
		numToResend = 0; //no timeouts yet so no frames to resend
		lastframeACKd = 0; //initially, no frames ACK'd
		timeOfLastSend = -networkDelay;	//make sure we can send the first frame
		savedframes = new packet[n]; //n = ws window size
		timeoutTimes = new long[n];
	}

	public packet nextframe (long t){
		packet f = null;
		if ((numSentInWindow > 0) && (t > timeoutTimes[0])){	
			/* we have a timeout
			 Handle time out by resending last n frames (Go-Back-N).
			 This is accomplished by setting numToResend to the number of frames
			 to be resent so that we will resend that many frames before sending
			 any new ones.*/
			numToResend = numSentInWindow-1;
			//must resend this many frames (plus the one resent immediately)
			numSentInWindow = 1;			
			//since the frames must be resent (less the one resent immediately)
			//we begin by resending the first frame immediately
			f = savedframes[0];
			timeoutTimes[0] = t + 2*networkDelay + 5;	
			//reset timeout
			txaLog.append("Time is: " + t + "\n");
			txaLog.append("-> Retransmitting first frame after timeout. seq# = " + f.seqNum() + "\n");
		} 
		else if(numToResend > 0){
			/*resending from saved frames. The one we want to send now is
			 from element number numSentInWindow.*/
			f = savedframes[numSentInWindow];
			timeoutTimes[numSentInWindow] = t + 2*networkDelay + 5; //reset timeout
			numSentInWindow++;
			numToResend--;
			txaLog.append("Time is: " + t + "\n");
			txaLog.append("->> Retransmitting another frame after timeout. Seq# = " + f.seqNum() + "\n");
		} 
		else if (numSentInWindow < n){
			/*this is Go-Back-N so we must wait until there is space in
			 the sender window before sending another frame. This space
			 comes from frames being acknowledged by the receiver.*/
			if ((numframesSent < numframesToSend) && (t >= timeOfLastSend + interframeDelay)){
				//we have a frame to send and it is time to send it
				f = new Informationpacket(frameSize);//Put then in a sequence of bytes
				f.setSeqNum(numframesSent + 1);			
				//start at 1 not 0
				//could fill in information frame data here (if desired)
				savedframes[numSentInWindow] = f;		
				//save a copy for re-sending
				timeoutTimes[numSentInWindow] = t + 2*networkDelay + 5;	//5 extra msecs
				numSentInWindow++;
				numframesSent++; //new frame so count this one
				timeOfLastSend = t; //handle inter-frame space for application
				txaLog.append("Time is: " + t + "\n");
				txaLog.append("->>> Sending frame with seq# = " + f.seqNum() + "\n");
			}
		}
		return (f);
	}

	public void acceptframe (long t, packet f){
		// accept a C-frame containing the ACK for a previously sent I frame
		if (!f.error()){
			//slide the window up to the sequence number of the ACK received
			while ((f.seqNum() >= savedframes[0].seqNum()) && (numSentInWindow>0) && (lastframeACKd != numframesToSend)){
				// shufle saved entries down by 1
				for (int i=0;i<(numSentInWindow-1);i++){
					savedframes[i] = savedframes[i+1];
					timeoutTimes[i] = timeoutTimes[i+1];
				}
				//update to reflect ACK (implicit or explicit) of frame
				numSentInWindow--;
				if (numToResend > 0) 
					numToResend--;
			}
			
			lastframeACKd = (lastframeACKd > f.seqNum()) ?lastframeACKd:f.seqNum();	//record number of latest good ACK
			txaLog.append("Time is: " + t + "\n");
			txaLog.append("->>>> ACK for frame with seq# = " + f.seqNum() + "\n" );
		} 
		else{
			txaLog.append("Time is: " + t + "\n");
			txaLog.append("->>>>> Bad C-frame with seq# = " + f.seqNum()+ "\n");
			/*sender ignores this. Receipt of correct C-frame ACKs all I-frames
			up to the corresponding sequence number. If an ACK is lost or
			corrupted, it is covered by receipt of a later ACK. If no ACK
			arrives in time then timeout causes resending which should
			eventually lead to positive ACKs.*/
		}
	}
	//allDone is called in the mainline to see if the simulation is over
	public boolean allDone(){
		return ((lastframeACKd == numframesToSend) && (numframesSent >= numframesToSend));
	}
}