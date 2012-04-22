/*
 *  A Stub that provides datagram send and receive functionality
 *  
 *  Feel free to modify this file to simulate network errors such as packet
 *  drops, duplication, corruption etc. But for grading purposes we will
 *  replace this file with out own version. So DO NOT make any changes to the
 *  function prototypes
 */

package services;

import java.net.SocketException;

public class TTPSegmentService{
	/* Definition of all constant variables */
	
	/* Flag values */
	public static final byte SYN_ACK = 18;
	public static final byte ACK = 16;
	public static final byte SYN = 2;
	public static final byte FIN = 1;
	public static final byte ACK_FIN = 17;
	public static final long MSL = 2; //20;
    public static final long TIMEOUT = 2 * MSL;
    
    
    /* State definitions */
	public static final int CLOSED  = 0;
	public static final int LISTEN  = 1;
	public static final int SYN_RECEIVED  = 2;
	public static final int SYN_SENT  = 3;
	public static final int ESTABLISHED  = 4;
	public static final int FIN_WAIT_1  = 5;
	public static final int FIN_WAIT_2  = 6;
	public static final int CLOSING  = 7;
	public static final int TIME_WAIT  = 8;
	public static final int CLOSE_WAIT  = 9;
	public static final int LAST_ACK  = 10;
	
	
	/* Starting sequence numbers */
	public static final int CLIENT_STARTING_SEQ_NO  = 0;
	public static final int SERVER_STARTING_SEQ_NO  = 1000;

	
	public static volatile int serverState;
	public static volatile  int clientState;
	
	private DatagramService ds;

	private ServerReceiverThread serverReceiverThread;
	private SenderThread serverSenderThread;
	private SenderThread clientSenderThread;
	private ClientReceiverThread clientReceiverThread;

	public TTPSegmentService(int port, int verbose) throws SocketException  {
		super();
		ds = new DatagramService(port, verbose);
		TTPSegmentService.serverState = CLOSED;
		TTPSegmentService.clientState = CLOSED;
	}

	public DatagramService getDS()
	{
		 return ds;
	}
	
	/* This function is used by the client to initiate a connection with the server */
    public void createConnection(short srcPort,short dstPort,String srcAddr,String dstAddr)
	{			
		/* Sending datagram */
		clientSenderThread = new SenderThread(this.ds, srcPort, dstPort, srcAddr, dstAddr);
		clientSenderThread.createSegment(CLIENT_STARTING_SEQ_NO, 0, SYN, "");
		clientSenderThread.send();
		clientState = SYN_SENT;
		
		/* Create a receiver thread */
		clientReceiverThread = new ClientReceiverThread(this.ds, clientSenderThread);
		clientReceiverThread.start();
		
		while(clientState != ESTABLISHED)
		{
			/* Wait until the client state changes to ESTABLISHED */
		}
		
		//clientSenderThread.timer.cancel();
		clientSenderThread.timeoutTask.cancel();

		
	}
    
    /*This function is used by the server to accept the connection with the server*/
    public void acceptConnection(short dstPort,short srcPort,String srcAddr,String dstAddr, int ackNo)
	{
      	/* Initialize a new sender and receiver thread*/
    	serverSenderThread = new SenderThread(this.ds, srcPort, dstPort, srcAddr, dstAddr);
		serverSenderThread.createSegment(SERVER_STARTING_SEQ_NO, ackNo, SYN_ACK, "");
		serverSenderThread.send();
		
    	serverReceiverThread = new ServerReceiverThread(this.ds, serverSenderThread);
		serverReceiverThread.start();    
		
		serverState = SYN_RECEIVED;
		
		while(serverState != ESTABLISHED)
		{
			/* Wait until the server state changes to ESTABLISHED */
		}
		
		serverSenderThread.timeoutTask.cancel();
	}
		
    public void closeConnection()
    {
		/* Sending datagram */
		clientSenderThread.createSegment(CLIENT_STARTING_SEQ_NO, 0, FIN, "FIN");
		clientSenderThread.send();
    	
		clientState = FIN_WAIT_1;
		

		while(clientState != CLOSED)
		{
			/* Wait until the client state changes to CLOSED */
		}
    	
		clientSenderThread.timer.cancel();
    }    
    
    
}
