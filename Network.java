/* Implements the network between the endpoints (sender and receiver)
 This class is primarily responsible for the transfer of frames
 using two queue (senderQ and receiverQ) implemented using Java's
 LinkedList class. It is also responsible for handling the simulation
 of errors and frames drops and for gathering the simulation
 statistics. */
import java.awt.BorderLayout;
import java.awt.Container;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class Network {
	private int	delay; //the one-way network delay
	private float errorProb; //the byte error probability
	private float dropProb; //the frame drop probability
	private int	frameSize;	//frame size (affects Prob of an error)
	private LinkedList senderQ = null;	//queue of frames destined for the sender (C frames)
	private LinkedList receiverQ = null; //queue of frames destined for the receiver (I frames)
	private Random	randGen = null;	//random generator for determining errors
	//variables used to track simulation statistics
	private long numErrframes;	//total number of frames with errors
	private long numDroppedframes; //total number of dropped frames
	private long numCFsSent; //number of C-frames sent
	private long numCFsRcvd; //number of C-frames received
	private long numIFsSent; //number of I-frames sent
	private long numIFsRcvd; //number of I-frames received
	
	private JTextArea txaLog = new JTextArea(400, 100);
	private JLabel lbInfo = new JLabel("N E T W O R K  LOG");
	private JFrame frame = new JFrame();

	Network(int del, float errProb, float drProb, int fsize)	{
		super();
		txaLog.setEditable(false);
		txaLog.setFocusable(false);
		txaLog.setLineWrap(true);
		txaLog.setWrapStyleWord(true);
		JScrollPane scpLog = new JScrollPane(txaLog);
		Container container = frame.getContentPane();
		container.add(lbInfo, BorderLayout.NORTH);
		container.add(scpLog, BorderLayout.CENTER);
						
		frame.setTitle("Communication between 2 Nodes");
		frame.setBounds(300, 300, 400, 150);
		frame.setVisible(true);

		delay = del;
		errorProb = errProb;
		dropProb = drProb;
		frameSize = fsize;
		//create frame queues
		senderQ = new LinkedList();
		receiverQ = new LinkedList();
		randGen = new Random();
		numErrframes = 0;
		numDroppedframes = 0;
		numCFsSent = 0; //number of C-frames sent
		numCFsRcvd = 0; //number of C-frames received
		numIFsSent = 0; //number of I-frames sent
		numIFsRcvd = 0; // number of I-frames received
	} // Network constructor

	private boolean frameDropped(){
		//use dropProb to determine whether or not to drop a frame
		float p;
		p = randGen.nextFloat(); //random generator for determining errors
		if (p < dropProb) 
			numDroppedframes++;
		
		return (p < dropProb);
	}

	private boolean hasError(){
		// use errorProb to determine whether or not to generate an error
		// considers frame size in the process
		float p;
		p = randGen.nextFloat();
		if (p < (errorProb * frameSize)) 
			// more errors possible for bigger frames
			numErrframes++;
		return (p < (errorProb * frameSize));
	}

	public boolean messagesInTransit(){
		//If queues are empty it  means nothing in transit
		return ((senderQ.size() == 0) || (receiverQ.size() == 0));
	}

	//injectCframe is called by the receiver (in main) to inject a C-frame into the network
	public void injectCframe(long time, packet f){
		//check for frame drop
		numCFsSent++; //Number of control frames sent
		if (frameDropped()){ //drop frame - just return
			txaLog.append("-> C-frame was dropped with seq# = " + f.seqNum() + "\n");
			return;
		}

		//set the Cframe's injectTime to the current simulation time
		f.setInjectionTime(time);
		//induce an error in the frame probabilistically
		f.setError(hasError());
		//add the new Cframe to the receiverQ
		receiverQ.addLast(f);
	}
	
	// injectIframe is called by the sender (in main) to inject an I-frame into the network
	public void injectIframe(long time, packet f){
		numIFsSent++;
		// check for frame drop
		if (frameDropped()){// drop frame - just return
			txaLog.append("-> I-frame was dropped with seq# = " +f.seqNum() + "\n");
			return;
		}
		// set the Iframe's injectTime to the current simulation time
		f.setInjectionTime(time);
		// induce an error in the frame probabilistically
		f.setError(hasError());
		// add the new Iframe to the senderQ
		senderQ.addLast(f);
	}
	// deliverCframe is called (in main) to retrieve a C-frame from the network 
	// to give to the sender
	public Controlpacket deliverCframe(long time){
		Controlpacket f = null; //Call contropacket class
		//check to see if there is a control frame to deliver
		if (receiverQ.size()!= 0){//There is something to delevery
			//check to see if it is time to deliver a control frame
			f = (Controlpacket) receiverQ.removeFirst();
			if (time >= (f.injectionTime() + delay - 1)){
				numCFsRcvd++;
				return (f);
			} 
			else{
				// not time yet so put frame back on queue
				receiverQ.addFirst(f);
				f = null;
			}
		}
		return(f);
	}

	//deliverIframe is called (in main) to retrieve an I-frame from the network to give to the receiver
	public Informationpacket deliverIframe(long time) {
		Informationpacket f = null;
		//check to see if there is an information frame to deliver
		if (senderQ.size()!= 0){
			//check to see if it is time to deliver an information frame
			f = (Informationpacket) senderQ.removeFirst();
			if (time >= (f.injectionTime() + delay - 1)){
				numIFsRcvd++;
				return (f);
			}
			else{
				//not time yet so put frame back on queue
				senderQ.addFirst(f);
				f = null;
			}
		}
		return(f);
	}
	
	// methods used to return simulation statistics
	public long numErrs(){
		return(numErrframes);
	}

	public long numDrops(){
		return(numDroppedframes);
	}

	public long numCframesSent(){
		return(numCFsSent);
	}

	public long numCframesReceived(){
		return (numCFsRcvd);
	}

	public long numIframesSent(){
		return(numIFsSent);
	}

	public long numIframesReceived(){
		return(numIFsRcvd);
	}

	public static void main(String args[]){
		Network application = new Network(0,0,0,0);
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void setDefaultCloseOperation(int exit_on_close) {
		// TODO Auto-generated method stub
		
	}
}