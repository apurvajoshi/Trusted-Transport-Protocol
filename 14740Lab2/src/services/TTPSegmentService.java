/*
 *  A Stub that provides datagram send and receive functionality
 *  
 *  Feel free to modify this file to simulate network errors such as packet
 *  drops, duplication, corruption etc. But for grading purposes we will
 *  replace this file with out own version. So DO NOT make any changes to the
 *  function prototypes
 */

package services;

import java.io.IOException;
import java.net.SocketException;
import datatypes.Datagram;
import datatypes.TTPSegment;

public class TTPSegmentService{
	/* Definition of all constant variables */
	public static final byte SYN_ACK = 18;
	public static final byte ACK = 16;
	public static final byte SYN = 2;
	public static final byte FIN = 1;
	public static final byte ACK_FIN = 17;
	public static final long MSL = 120;
    public static final long TIMEOUT = 2 * MSL;
	
	private DatagramService ds;

	private ServerReceiverThread serverReceiverThread;
	private SenderThread serverSenderThread;
	private SenderThread clientSenderThread;
	private ClientReceiverThread clientReceiverThread;

	public TTPSegmentService(int port, int verbose) throws SocketException  {
		super();
		ds = new DatagramService(port, verbose);
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
		clientSenderThread.createSegment(0, SYN, "");
		clientSenderThread.send();
		
		/* Create a receiver thread */
		clientReceiverThread = new ClientReceiverThread(this.ds, clientSenderThread);
		clientReceiverThread.start();
	}
    
    /*This function is used by the server to accept the connection with the server*/
    public void acceptConnection(short dstPort,short srcPort,String srcAddr,String dstAddr) throws IOException, ClassNotFoundException
	{
    
    	/*Modified Code*/
    	/*Initial blocking call waiting for the first SYN packet.Should we do check here for SYN?*/
    	Datagram datagram = ds.receiveDatagram(); 
    	/*Once received initialize a new sender and receiver thread*/
    	TTPSegment ackSeg=(TTPSegment)(datagram.getData());
    	if(ackSeg.getFlags() == TTPSegmentService.SYN)
    	{
    	/*Currently put dummy source port*/
    	serverSenderThread = new SenderThread(this.ds, (short)9000, dstPort, srcAddr, dstAddr);
		serverSenderThread.createSegment(0, SYN_ACK, "");
		serverSenderThread.send();
    	serverReceiverThread = new ServerReceiverThread(this.ds, serverSenderThread);
		serverReceiverThread.start();
    	}
    	return;
    	/*End of modified code */
	}
		
    public void initiateDestroy() throws IOException, ClassNotFoundException
    {
		/* Sending datagram */
		clientSenderThread.createSegment(0, FIN, "");
		clientSenderThread.send();
    	
    }   
    
    
    public void clientSendFileRequest()
    {
    	/*Inserted dummy values*/
        final String filename = "a.txt";
    	clientSenderThread.createSegment(3, ACK ,filename);
    	clientSenderThread.send();
    }
    public int serverCheckFilePresent(String filename)
    {
    /*Check in a list whether the file is present and if yes then 
     * send the file.	 
     */
    	return 1;
    	
    }
    
    
}
