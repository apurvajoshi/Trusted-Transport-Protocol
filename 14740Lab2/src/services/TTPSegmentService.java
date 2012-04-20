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
import services.test;
import datatypes.Datagram;
import datatypes.TTPSegment;

public class TTPSegmentService{


	/* Definition of all constant variables */
	public static final byte SYN_ACK = 18;
	public static final byte ACK = 16;
	public static final byte SYN = 2;
	public static final byte FIN = 1;
	public static final byte FIN_ACK = 17;

	private DatagramService ds;
	private ServerReceiverThread serverReceiverThread;
	private SenderThread serverSenderThread;
	

	public TTPSegmentService(int port, int verbose) throws SocketException  {
		super();
		ds = new DatagramService(port, verbose);
	}

	public DatagramService getDS()
	{
		 return ds;
	}	


    /*This function is used by the server to accept the connection with the server*/
    public void acceptConnection(short dstPort,short srcPort,String srcAddr,String dstAddr) throws IOException, ClassNotFoundException
	{
    
    	/*Modified Code*/
    	/*Initial blocking call waiting for the first SYN packet.Should we do check here for SYN?*/
    	Datagram datagram = ds.receiveDatagram(); 
    	/*Once received initialize a new sender and receiver thread*/
    	 serverSenderThread = new SenderThread(this.ds, srcPort, dstPort, srcAddr, dstAddr);
		serverSenderThread.createSegment(0, SYN_ACK, "");
		serverSenderThread.send();
    	serverReceiverThread = new ServerReceiverThread(this.ds, serverSenderThread);
		serverReceiverThread.start();
    	
    	/*End of modified code */
    
	}
    
    
    

  
    
    
    
}