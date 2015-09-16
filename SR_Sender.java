import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
/*Selective repeated sender: resends the packet as soon as it 
 times out or error. Frames are not in order. 
 This class extends the abstract base class Sender.*/

class SR_Sender extends Sender 
{
	private JTextArea txaLog = new JTextArea(400, 250);
	private JLabel lbInfo = new JLabel("S E N D E R   LOG");
	private JFrame frame = new JFrame();
	
	private boolean	lastframeACKd;	//was the last frame ACKnowledged?
	private long[] timeoutTimes; //timeout if no ACK by this time.
	private packet[] savedframes; //a copy of all frames in a array
	private long timeOfLastSend; //time when last frame was sent
	private int[] status; //Not_Send = -1, sent = 111, ACK = 1, ERR = 999;
	private int successfulsent = 0;
   
	public SR_Sender(int frames, int size, int netDelay, int interframeDelay) {
		super(frames, size, netDelay, interframeDelay);
		txaLog.setEditable(false);
		txaLog.setFocusable(false);
		txaLog.setLineWrap(true);
		txaLog.setWrapStyleWord(true);
		JScrollPane scpLog = new JScrollPane(txaLog);
		Container container = frame.getContentPane();
		container.add(lbInfo, BorderLayout.NORTH);
		container.add(scpLog, BorderLayout.CENTER);
						
		frame.setTitle("Selective Repeat ARQ");
		frame.setBounds(10, 450, 400, 250);
		frame.setVisible(true);
		lastframeACKd = true; //make sure we can send the first frame
		timeOfLastSend = -networkDelay;	//make sure we can send the first frame
		savedframes = new Informationpacket[frames];
		status = new int[frames];
		timeoutTimes = new long[frames];
		
		for( int intb = 0; intb < frames; intb++) {   
		    savedframes[intb] = new Informationpacket(frameSize);
		    savedframes[intb].setSeqNum(intb + 1);//seqNums are fixed before send
		    status[intb] = -1; //status is not send
		}
	}
	//Selects the next frame to be sent
	public packet nextframe (long t){	
	    //next is chosen from savedframes, 
	    int i = 0;
	    packet f = null;
	    //time to send next frame
	    if((successfulsent < numframesToSend) && (t >= timeOfLastSend + interframeDelay )){
	        for(; i < numframesToSend; i++) {
	        	if (status[i] == 1)//ACK
	        		; //ignore when ack
	        	//Remember Not_Send = -1, sent = 111, ACK = 1, ERR = 999;
	        	else if (status[i] == -1){ //no sent yet, send it
	        		f = savedframes[i];
	        		timeoutTimes[i] = t + 2*networkDelay + 5; //set timeout
	        		timeOfLastSend = t;					 
	        		status[i] = 111; //set status to sent
	        		txaLog.append("-> Transmitting frame seq# = " + f.seqNum() + "\n");
	        		break;
	        	}
	        	//error or time out, resend needed
	        	else if((status[i] == 999) || (t > timeoutTimes[i])) { //Check if not error or time out
	        		//resend the lowest seqnum with error or timeout first 
	        		f = savedframes[i];
	        		timeoutTimes[i] = t + 2*networkDelay + 5;// reset timeout
	        		timeOfLastSend = t; //Keep the time of last sending 
	        		status[i] = 111;//reset status to sent
	        		txaLog.append("->> Retransmitting frame seq# = " + f.seqNum() + "\n");
	        		break;
	        	}
	        	else
	        		; //do nothing, everything is fine    
	        }//end for
	    }//end if
        return f; //Return value in f
    }//end nextframe

	public void acceptframe (long t, packet f) {   
	    int i = 0;
	    for(; i < numframesToSend; i++){
	    	//find the frame in copy savedframes[i]
	        if(savedframes[i].seqNum() == f.seqNum())
	        	break;
	    }
		// look for a C-frame (control frame) containing the ACK for our previously sent I frame
		if (!f.error()) {//If NOT any error, go on
			lastframeACKd = true;
			status[i] = 1; //ACK
			successfulsent++;
			txaLog.append("->>> Got ACK for frame with seq# = " + f.seqNum() + "\n");
		} 
		else if (status[i] == 1) 
		    ;//ingnore duplicated frame    
		else{
			txaLog.append("->>>> Bad C-frame with seq# = " + f.seqNum() + "\n");
			status[i] = 999; //set error for resending
		}
	}
	//allDone is called in the mainline to see if the simulation is over
	public boolean allDone(){ 
		return(successfulsent == numframesToSend);
	}
}