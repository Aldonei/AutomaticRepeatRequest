import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.border.*;
public class FTP extends JFrame{
	//- Variables declaration
	private JPanel contentPane;
	private JPanel pnlCenter;
	private JPanel pnlSouth;
	private JPanel pnlLocalSystemAndArrows;
	private JComboBox cmbWindowSize;
	static JTextArea txaLogSender;
	private JScrollPane jScrollPane1;
	private JPanel pnlLocalSystem;
	private JTextField txfFileName;
	private JTextArea txaLogReceiver;
	private JScrollPane jScrollPane2;
	private JPanel pnlRemoteSystem;
	private JButton btnFindFile;
	private JButton btnOpenFile;
	private JButton btnExec;
	private JButton btnRemoteView;
	private JLabel lblLatency;
	private JLabel lblDataRate;
	private JLabel lblNumFrames;
	private JLabel lblFrameSize;
	private JLabel lblInterFrameDelay;
	private JLabel lblErrorProb;
	private JTextField txfLatency;
	private JTextField txfDataRate;
	private JTextField txfNumframes;
	private JTextField txfframeSize;
	private JTextField txfInterframeDelay;
	private JTextField txfErrorProb;
	private JTextField txfDropProb;
	private JPanel pnlRemoteButtons;
	private JFileChooser fcFindFile;
	private final String protocol[] = {"Stop & Wait ARQ", "Go Back N ARQ", "Selective Repeat ARQ"};
	private JRadioButton rbtnAlgorithem[];
	private JPanel pnlProtocol;
	private JScrollPane jScrollPane3;
	private JPanel pnlLoginfo;
	private JButton btnConnect;
	private JButton btnClear;
	private JButton btnLogWnd;
	private JButton btnHelp;
	private JButton btnOptions;
	private JButton btnAbout;
	private JButton btnExit;
	private JPanel pnlMainButtons;
	JTextArea txaLoginfo;
	private ButtonGroup btnGroup1;
	//End of variables declaration
	//the simulation parameters
	long remainPkt, fileSize, pkt; //Size of the file
	int	latency; //network latency in msec
   	int	dataRate; //network data rate in bytes per second
   	int	numframes;	//number of frames to send
   	int	frameSize;	//size of each frame in bytes in msec
   	int	interframeDelay; //sender's delay between frames in msec
   	float errorProb; //probability of a byte being in error
   	float dropProb;	//probability of a frame being dropped
   	String algorithem; //ARQ algorithm to use - one of: SAW, GBN, SR
   	int n = 0; //number to go back (only for Go-Back-N)
	// the simulation variables
  	int delay; //network delay given frame size
	long simTime; //the simulation time
	Network	network = null; //the network being simulated
	Sender	sender = null; //the transmitting endpoint
	Receiver receiver = null; // the receiving endpoint
	packet senderIframe = null; //the frame being "sent"
	packet receiverCframe = null; //the frame being "received"
	Informationpacket sentIframe = null; //a sent frame
	Controlpacket receivedCframe = null; //a received frame

	public FTP(){
		super();
		initializeComponent();
		this.setVisible(true);
	}

	private void initializeComponent() {
		contentPane = (JPanel)this.getContentPane();
		pnlCenter = new JPanel();
		pnlSouth = new JPanel();
		pnlLocalSystemAndArrows = new JPanel();
		cmbWindowSize = new JComboBox(); //Define the size of the window
		txaLogSender = new JTextArea();
		jScrollPane1 = new JScrollPane();
		pnlLocalSystem = new JPanel();
		txfFileName = new JTextField();
		txaLogReceiver = new JTextArea();
		jScrollPane2 = new JScrollPane();
		pnlRemoteSystem = new JPanel();
		btnRemoteView = new JButton();
		btnFindFile = new JButton();
		btnOpenFile = new JButton();
		btnExec = new JButton();
		
		lblLatency = new JLabel("Latency");
		lblDataRate = new JLabel("Data Rate");
		lblNumFrames = new JLabel("Num Frames");
		lblFrameSize = new JLabel("Frame Size");
		lblInterFrameDelay = new JLabel("Frame Delay");
		lblErrorProb = new JLabel("Error Probability");
				
		//Define values for simulation
		txfLatency = new JTextField();
		txfDataRate = new JTextField();
		txfNumframes = new JTextField();
		txfframeSize = new JTextField();
		txfInterframeDelay = new JTextField();
		txfErrorProb = new JTextField();
		txfDropProb = new JTextField();
		fcFindFile = new JFileChooser();
		btnRemoteView = new JButton();
		pnlRemoteButtons = new JPanel();
		rbtnAlgorithem = new JRadioButton[protocol.length];
		pnlProtocol = new JPanel();
		jScrollPane3 = new JScrollPane();
		pnlLoginfo = new JPanel();
		txaLoginfo = new JTextArea();
		btnConnect = new JButton();
		btnClear = new JButton();
		btnLogWnd = new JButton();
		btnHelp = new JButton();
		btnOptions = new JButton();
		btnAbout = new JButton();
		btnExit = new JButton();
		pnlMainButtons = new JPanel();
		btnGroup1 = new ButtonGroup();

		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.add(pnlCenter, BorderLayout.CENTER);
		contentPane.add(pnlSouth, BorderLayout.SOUTH);
		pnlCenter.setLayout(new GridLayout(1, 0, 0, 0));
		pnlCenter.add(pnlLocalSystemAndArrows, 0);
		pnlCenter.add(pnlRemoteSystem, 1);
		pnlSouth.setLayout(new BorderLayout(0, 0));
		pnlSouth.add(pnlProtocol, BorderLayout.NORTH);
		pnlSouth.add(pnlLoginfo, BorderLayout.CENTER);
		pnlSouth.add(pnlMainButtons, BorderLayout.SOUTH);
		pnlLocalSystemAndArrows.setLayout(new BorderLayout(0, 0));
		pnlLocalSystemAndArrows.add(pnlLocalSystem, BorderLayout.CENTER);
		//Difine window size
		cmbWindowSize.addItem("Window Size"); //Consider 0
		cmbWindowSize.addItem("Window Size = 1");
		cmbWindowSize.addItem("Window Size = 2");
		cmbWindowSize.addItem("Window Size = 3");
		cmbWindowSize.addItem("Window Size = 4");
		cmbWindowSize.addItem("Window Size = 5");
		cmbWindowSize.addItem("Window Size = 6");
		cmbWindowSize.addItem("Window Size = 7");

		cmbWindowSize.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				cmbWindowSize_actionPerformed(e);
			}
		});
		//1 1000 50 500 200 0.0002 0.05
		//Set field for simulation
		txfLatency.setText("1");
		txfDataRate.setText("1000");
		txfNumframes.setText("50");
		txfframeSize.setText("500");
		txfInterframeDelay.setText("200");
		txfErrorProb.setText("0.0002");
		txfDropProb.setText("0.05");

		jScrollPane1.setViewportView(txaLogSender);
		pnlLocalSystem.setLayout(new BorderLayout(5, 5));
		pnlLocalSystem.add(cmbWindowSize, BorderLayout.NORTH);
		pnlLocalSystem.add(jScrollPane1, BorderLayout.CENTER);
		pnlLocalSystem.setBorder(new TitledBorder("Sender System"));
		//Set the path to get the file, but it can be chanced on time
		txfFileName.setText("C:\\DevJava\\ARQ\\Files\\Send\\Radiohead.txt");
		
		txfFileName.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				txfFileName_actionPerformed(e);
			}
		});

		jScrollPane2.setViewportView(txaLogReceiver);
		pnlRemoteSystem.setLayout(new BorderLayout(5, 5));
		pnlRemoteSystem.add(txfFileName, BorderLayout.NORTH);
		pnlRemoteSystem.add(jScrollPane2, BorderLayout.CENTER);
		pnlRemoteSystem.add(pnlRemoteButtons, BorderLayout.EAST);
		pnlRemoteSystem.setBorder(new TitledBorder("Receiver System"));

		txfLatency.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				txfLatency_actionPerformed(e);
			}
		});
		btnRemoteView.setText("View");
		btnRemoteView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnRemoteView_actionPerformed(e);
			}

		});

		btnFindFile.setText("Find");
		btnFindFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				btnFindFile_actionPerformed(e);
			}

		});
		
		btnOpenFile.setText("Open");
		btnOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				btnOpenFile_actionPerformed(e);
			}

		});

		btnExec.setText("Exec");
		btnExec.setEnabled(false);
		btnExec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnExec_actionPerformed(e);
			}
		});

		pnlRemoteButtons.setLayout(new GridLayout(15, 0, 0, 0));
		pnlRemoteButtons.add(btnFindFile, 0);
		pnlRemoteButtons.add(btnOpenFile, 1);
		pnlRemoteButtons.add(lblLatency, 2);
		pnlRemoteButtons.add(txfLatency, 3);
		pnlRemoteButtons.add(lblDataRate, 4);
		pnlRemoteButtons.add(txfDataRate, 5);
		pnlRemoteButtons.add(lblNumFrames, 6);
		pnlRemoteButtons.add(txfNumframes, 7);
		pnlRemoteButtons.add(lblFrameSize, 8);
		pnlRemoteButtons.add(txfframeSize, 9);
		pnlRemoteButtons.add(lblInterFrameDelay, 10);
		pnlRemoteButtons.add(txfInterframeDelay, 11);
		pnlRemoteButtons.add(lblErrorProb, 12);
		pnlRemoteButtons.add(txfErrorProb, 13);
		pnlRemoteButtons.add(btnExec, 14);
		
		pnlProtocol.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 0));
		ItemHandler handler = new ItemHandler();
		for(int count = 0; count < rbtnAlgorithem.length; count++){
			rbtnAlgorithem[count] = new JRadioButton(protocol[count]);
			rbtnAlgorithem[count].addItemListener(handler);
			pnlProtocol.add(rbtnAlgorithem[count]);
		}
		
		txaLogSender.setEditable(false);
		txaLogSender.setFocusable(false);
		txaLogSender.append("Frame control field\n");
		txaLogSender.append("Not_Send = -1\nsent = 111\nACK = 1\nERR = 999\n\n");
		txaLogSender.append("Information Frame\n");
		txaLogSender.append("|-----|-----------------------|\n");
		txaLogSender.append("|Seq# | I N F O R M A T I O N |\n");
		txaLogSender.append("|-----|-----------------------|\n");
		
		txaLogSender.append("Control Frame\n");
		txaLogSender.append("|----------|------|\n");
		txaLogSender.append("| 1 or 999 | Seq# |\n");
		txaLogSender.append("|----------|------|\n");
		txaLogReceiver.setEditable(false);
		txaLogReceiver.setFocusable(false);

		txaLoginfo.setOpaque(false);
		txaLoginfo.setText("Hey I am waiting for a simulation here. Please start it when you ready\n" +
				"pressing the Execute button... GOOD LUCK!!!!\n\n");
		txaLoginfo.setEditable(false);
		txaLoginfo.setFocusable(false);
		txaLoginfo.setLineWrap(true);
		txaLoginfo.setWrapStyleWord(true);
		jScrollPane3.setViewportView(txaLoginfo);
		jScrollPane3.setPreferredSize(new Dimension(74, 60));
		pnlLoginfo.setLayout(new GridLayout(1, 0, 0, 0));
		pnlLoginfo.add(jScrollPane3, 0);
		pnlLoginfo.setBorder(BorderFactory.createRaisedBevelBorder());

		btnConnect.setText("Tools");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnConnect_actionPerformed(e);
			}

		});

		btnClear.setText("Clear");
		btnClear.setMnemonic('C');
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnClear_actionPerformed(e);
			}
		});

		btnLogWnd.setText("Info");
		btnLogWnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnLogWnd_actionPerformed(e);
			}
		});

		btnHelp.setText("Help me");
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnHelp_actionPerformed(e);
			}

		});

		btnOptions.setText("Options");
		btnOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnOptions_actionPerformed(e);
			}

		});
		//
		// btnAbout
		//
		btnAbout.setText("About");
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnAbout_actionPerformed(e);
			}

		});

		btnExit.setText("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				btnExit_actionPerformed(e);
			}

		});

		pnlMainButtons.setLayout(new GridLayout(1, 0, 0, 0));
		pnlMainButtons.add(btnConnect, 0);
		pnlMainButtons.add(btnClear, 1);
		pnlMainButtons.add(btnLogWnd, 2);
		pnlMainButtons.add(btnHelp, 3);
		pnlMainButtons.add(btnOptions, 4);
		pnlMainButtons.add(btnAbout, 5);
		pnlMainButtons.add(btnExit, 6);

		btnGroup1.add(rbtnAlgorithem[0]);
		btnGroup1.add(rbtnAlgorithem[1]);
		btnGroup1.add(rbtnAlgorithem[2]);
		rbtnAlgorithem[0].setSelected(true);
		this.setTitle("Sliding Window Protocol Simulation 2015 - Automatic Repeat-reQuest (ARQ)");
		this.setLocation(new Point(0, 0));
		this.setSize(new Dimension(800, 556));
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	private class ItemHandler implements ItemListener{
		public void itemStateChanged(ItemEvent event){
			for(int count = 0; count < rbtnAlgorithem.length; count++){
				if(rbtnAlgorithem[count].isSelected()){
					algorithem = protocol[count];
				}
			}
		}
	}

	private void cmbWindowSize_actionPerformed(ActionEvent e)
	{
		Object o = cmbWindowSize.getSelectedItem();
		System.out.println(">>" + ((o==null)? "null" : o.toString()) + " is selected.");
		if(o != "Window Size"){
			algorithem = "Go Back N ARQ";
			rbtnAlgorithem[1].setSelected(true);
			n = cmbWindowSize.getSelectedIndex();
		}
		else{
			algorithem = "Stop & Wait ARQ";
			rbtnAlgorithem[0].setSelected(true);
		}
	}

	private void btnExec_actionPerformed(ActionEvent e)
	{
		txfNumframes.setText(Long.toString(pkt));
	    latency=Integer.parseInt(txfLatency.getText());
	    dataRate=Integer.parseInt(txfDataRate.getText());
	    numframes = Integer.parseInt(txfNumframes.getText());
	    frameSize=Integer.parseInt(txfframeSize.getText());
	    interframeDelay=Integer.parseInt(txfInterframeDelay.getText());
	    errorProb=Float.parseFloat(txfErrorProb.getText());
	    dropProb=Float.parseFloat("0.05");
	    // compute network delay given I-frame (information frame) size
	    delay = (int)((float)latency + (float)frameSize/(float)dataRate*1000.0);
	    //includes conversion from secs to msecs
		//initialize simulation timer
		simTime = 0; //Simulation time
		//Instantiate non algorithm-specific simulation objects
		network = new Network(delay, errorProb, dropProb, frameSize);
        //instantiate algorithm-specific simulation objects, dynamic binding
		if(algorithem.equals("Stop & Wait ARQ")){
			sender = new SAW_Sender(numframes, frameSize, delay, interframeDelay);
			receiver = new SAW_Receiver();
		}
		else if (algorithem.equals("Go Back N ARQ")){
			//make sure N (the sender's window size) is specified
			if (n == 0){
				JOptionPane.showMessageDialog(this, "Must specify N when you select GBN, I'll assume 4 anyway",
						"Selection Error", JOptionPane.ERROR_MESSAGE);
				n = Integer.parseInt("4");	//get number of frames to go back
			}
			sender = new GBN_Sender(numframes, frameSize, delay, interframeDelay,n);
			receiver = new GBN_Receiver();
		}
		else if (algorithem.equals("Selective Repeat ARQ")){
			sender = new SR_Sender(numframes, frameSize, delay, interframeDelay);
			receiver = new SR_Receiver();

		}
		else{
			System.out.println("ARQ: error - unknown algorithm.");
			System.exit(1);
		}
		if (algorithem.equals("Go Back N ARQ")){
			txaLoginfo.append("\n   Sender's window size: "+ n +" frames");
		}
		//Do the simulation
	    while(!sender.allDone()){
			//create any new frames and inject them into the network
			senderIframe = sender.nextframe(simTime);
			if(senderIframe != null){
				network.injectIframe(simTime, senderIframe);
			}
			receiverCframe = receiver.nextframe();
			if(receiverCframe != null){
				network.injectCframe(simTime, receiverCframe);
			}
			//allow sender and receiver to accept frames from the network
			receivedCframe = network.deliverCframe(simTime);
			if(receivedCframe!=null){
				sender.acceptframe(simTime, receivedCframe);
			}
			sentIframe = network.deliverIframe(simTime);
			if(sentIframe != null){
				receiver.acceptframe(sentIframe);
			}
			simTime++;
		}//End while
	    // Print the simulation results
	    txaLoginfo.append("\n*------------------------------------------------------------------*\n" +
	    		   algorithem + "\n" +
	    		   " Simulation Completed... Let's check it now\n\n" +
	    		   "   Unidirectional transmission delay: "+delay+" millisecs\n" +
	    		   "   Total Transmission time: "+simTime+" millseconds\n" +
	    		   "   Number of frames (Control and Information) with transmission errors: "+network.numErrs()+ "\n" +
	    		   "   Number of frames dropped: "+network.numDrops()+ "\n" +
	    		   "   Number of control frames sent: "+network.numCframesSent() + "\n" +
	    		   "   Number of control frames received: "+network.numCframesReceived() + "\n" +
	    		   "   Number of information frames sent: "+network.numIframesSent() + "\n" +
	    		   "   Number of information frames received: "+network.numIframesReceived());

	    txaLoginfo.append("\n\n*------------------------------------------------------------------*\n" +
				" ARQ Simulation Starting... At last!!!\n\n" +
				"   Network latency: "+latency+" millisecs\n" +
				"   Network data rate: "+dataRate+" bytes per second\n" +
				"   Sending "+numframes+" frames of "+frameSize+" bytes each.\n" +
				"   Delay between sending frames: "+interframeDelay+" millisecs\n" +
				"   Probability of a byte error: "+errorProb +"\n" +
				"   Probability of a frame being dropped: "+dropProb);

	       txaLogSender.setText("");
	       txaLogSender.append("File was transfered successufully...\n" +
	    		   "check on directory C:\\DevJava\\ARQ\\Receive\n");
	       
	       try{
	    	   File getName = new File(txfFileName.getText());
	    	   FileReader fr = new FileReader(getName);
	    	   BufferedReader in =  new BufferedReader(fr);
	    	   String text;
	    	   text = in.readLine();
	    	   //Reading line by line
	    	   while(text != null){
	    		   txaLogReceiver.append(text + "\n"); //insert new line
	    		   text = in.readLine();
	    	   }
	    	   putBytesInFile(getName);
	       }
			catch(Exception ev){
				JOptionPane.showMessageDialog(this, "File Error, something went wrong, check it...",
						"File Error", JOptionPane.ERROR_MESSAGE);
			}

	       btnExec.setEnabled(false);
	       btnOpenFile.setEnabled(false);
	       btnFindFile.setEnabled(false);
	}// End of execution process

	private void txfFileName_actionPerformed(ActionEvent e)
	{
		System.out.println("\ncmbRemoteFolder_actionPerformed(ActionEvent e) called.");
	}

	private void txfLatency_actionPerformed(ActionEvent e)
	{
		System.out.println("\ntxfRemote_actionPerformed(ActionEvent e) called.");
	}

	private void btnRemoteView_actionPerformed(ActionEvent e)
	{
		System.out.println("\nbtnRemoteView_actionPerformed(ActionEvent e) called.");
	}

	private void btnFindFile_actionPerformed(ActionEvent e)
	{
		int res = fcFindFile.showOpenDialog(null);
		if(res == JFileChooser.CANCEL_OPTION)
			return;
		else{
			try{
				btnExec.setEnabled(true);
				btnOpenFile.setEnabled(false);
				btnFindFile.setEnabled(false);
				File getFile = fcFindFile.getSelectedFile();
				FileReader fr = new FileReader(getFile);
				BufferedReader in =  new BufferedReader(fr);
				String text, fDate;
				long fileDate;
				fileDate = getFile.lastModified();
				Date d = new Date(fileDate);
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT);
				fDate = df.format(d);
				txaLogSender.setText("");
				fileSize = getFile.length();
				pkt = (fileSize/Integer.parseInt(txfNumframes.getText()));
				if((fileSize % Integer.parseInt(txfNumframes.getText()) != 0)){
					remainPkt = fileSize % Integer.parseInt(txfNumframes.getText());
					pkt = pkt + 1;
				}
				txaLoginfo.setText("File name: " + getFile.getName() + "\n" +
						"Absolute path: " + getFile.getAbsolutePath() + "\n" +
						"Parent directory: " +  getFile.getParent() + "\n" +
						"Last modified: " + fDate + "\n" +
						"File Length: " + getFile.length() + " bytes" + "\n" +
						"Number of frames: " + pkt + "\n" +
						"Size of frames: " + txfNumframes.getText() + " bytes" + "\n" +
						"Remain frame size = " + remainPkt + " bytes");
				txfFileName.setText(getFile.getAbsolutePath());
				text = in.readLine();
				//Reading line by line
				while(text != null){
					txaLogSender.append(text + "\n"); //insert new line
					text = in.readLine();
				}
				in.close(); //Close file
			}
			catch(IOException ev){
				JOptionPane.showMessageDialog(this, "File Error, something went wrong, check it...",
						"File Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void btnOpenFile_actionPerformed(ActionEvent e)
	{
		try{
			btnExec.setEnabled(true);
			btnOpenFile.setEnabled(false);
			btnFindFile.setEnabled(false);

			txaLogSender.setText("");
			File getName = new File(txfFileName.getText());
			if(getName.exists()){
				fileSize = getName.length();
				pkt = (fileSize/Integer.parseInt(txfNumframes.getText()));
				if((fileSize % Integer.parseInt(txfNumframes.getText()) != 0)){
					remainPkt = fileSize % Integer.parseInt(txfNumframes.getText());
					pkt = pkt + 1;
				}
				txaLoginfo.setText("File name: " + getName.getName() + "\n" +
						"Absolute path: " + getName.getAbsolutePath() + "\n" +
						"Parent directory: " +  getName.getParent() + "\n" +
						"Last modified: " + getName.lastModified() + "\n" +
						"File Length: " + getName.length() + " bytes" + "\n" +
						"Number of frames: " + pkt + "\n" +
						"Size of frames: " + txfNumframes.getText() + "\n" +
						"Remain frame size = " + remainPkt + " bytes");
			}
			getBytesFromFile(getName);
		}
		catch(Exception ev){
			JOptionPane.showMessageDialog(this, "File Error, something went wrong, check it...",
					"File Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	//Returns the content of a file in a byte array
	public static byte[] getBytesFromFile(File fileName) throws IOException{
		FileReader fr = new FileReader(fileName);
		BufferedReader in =  new BufferedReader(fr);
		String text;
		text = in.readLine();
		InputStream inQueue = new FileInputStream(fileName);

		//Get the size of the file
		long fileLenght = fileName.length();

		/*We cannot create an array using a long type, it needs to be an INT type
		 *So before converting to an INT type, check to ensure that file is not
		 *larger than Integer.MAX_VALUE.*/
		if(fileLenght > Integer.MAX_VALUE){
			System.out.println("File is too large...");
		}
		//Create a byte array to hold the data
		byte[] bytes = new byte[(int)fileLenght];
		//Read in the bytes
		int offset = 0;
		int numRead = 0;
		while(offset < bytes.length && (numRead = inQueue.read(bytes, offset, bytes.length - offset)) >= 0)
			offset += numRead;
		//Ensure all the bytes have been read in
		if(offset < bytes.length)
			throw new IOException("Could not completely read file " + fileName.getName());

		//Reading line by line, only to show the file on the screen
		while(text != null){
			txaLogSender.append(text + "\n"); //insert new line
			text = in.readLine();
		}
		//Close the input stream and return bytes
		inQueue.close();
		return bytes;
	}
	public void putBytesInFile(File fileName) throws IOException{
		//Crate a file stream
		FileInputStream inQueue = new FileInputStream(fileName);
        FileOutputStream outQueue = new FileOutputStream("C:\\DevJava\\ARQ\\Files\\Receive\\Receive.txt");
        //read in one file and write it out to the second
        int c;
        while ((c = inQueue.read()) != -1)
           outQueue.write(c);
        // close both files
        inQueue.close();
        outQueue.close();
	}

	private void btnConnect_actionPerformed(ActionEvent e)
	{
		System.out.println("\nbtnConnect_actionPerformed(ActionEvent e) called.");
	}

	private void btnClear_actionPerformed(ActionEvent e)
	{
		System.out.println("\nbtnCancel_actionPerformed(ActionEvent e) called.");
		txaLoginfo.setText("");
		txaLogSender.setText("");
		txaLogReceiver.setText("");
		btnExec.setEnabled(false);
		btnOpenFile.setEnabled(true);
		btnFindFile.setEnabled(true);
	}

	private void btnLogWnd_actionPerformed(ActionEvent e)
	{
		System.out.println("\nbtnLogWnd_actionPerformed(ActionEvent e) called.");
	}

	private void btnHelp_actionPerformed(ActionEvent e)
	{
		System.out.println("\nbtnHelp_actionPerformed(ActionEvent e) called.");
	}

	private void btnOptions_actionPerformed(ActionEvent e)
	{
		System.out.println("\nbtnOptions_actionPerformed(ActionEvent e) called.");
	}

	private void btnAbout_actionPerformed(ActionEvent e)
	{
		System.out.println("\nbtnAbout_actionPerformed(ActionEvent e) called.");
		System.out.println("Aldonei de Avila Souza");
	}

	private void btnExit_actionPerformed(ActionEvent e)
	{
		System.exit(1); //Close the program
	}

	public static void main(String[] args)
	{
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		}
		catch (Exception ex)
		{
			System.out.println("Failed loading L&F: ");
			System.out.println(ex);
		}
		new FTP();
	}
}
