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

	private DatagramService ds;

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
		SenderThread senderThread = new SenderThread(this.ds, srcPort, dstPort, srcAddr, dstAddr);
		senderThread.createSegment(0, SYN, "");
		senderThread.send();
		
		/* Create a receiver thread */
		ReceiverThread receiverThread = new ReceiverThread(this.ds, senderThread);
		receiverThread.start();
	}
    
    public void acceptConnection() throws IOException, ClassNotFoundException
	{
    
    	/* Check for the error condition when the client send the SYN packet
    	 * even when the connection is open */
    	
    	Datagram datagram = ds.receiveDatagram();
		System.out.println("Received datagram from " + datagram.getSrcaddr() + ":" + datagram.getSrcport() + " Data: " + datagram.getData());    	
    	TTPSegment rcvSegment = (TTPSegment)datagram.getData();
    	
    	if(rcvSegment.getFlags() == 2)
    	{
	    	String data = "";
			int seqNumber = 100;
			int ackNumber = rcvSegment.getSeqNumber() + 1;			
			byte flag = 18;
			TTPSegment ackSeg = new TTPSegment(datagram.getDstport(), datagram.getSrcport(), seqNumber, ackNumber, (byte)16,  flag,  (short)750, (Object)data);	
			Datagram ack = new Datagram();
			ack.setSrcaddr(datagram.getDstaddr());
			ack.setDstaddr(datagram.getSrcaddr());
			ack.setDstport(datagram.getSrcport());
			ack.setSrcport(datagram.getDstport());
			ack.setData(ackSeg);			
			ds.sendDatagram(ack);
			System.out.println("Sent datagram at dst port " + ack.getDstport());
			System.out.println("Server : Sending datagram");

			datagram = ds.receiveDatagram();		
			System.out.println("Received datagram from " + datagram.getSrcaddr() + ":" + datagram.getSrcport() + " Data: " + datagram.getData());
			rcvSegment = (TTPSegment)datagram.getData();
			System.out.println("Connection Established Flag received is " + rcvSegment.getFlags());
    	}
    	else
    	{
    		System.out.println("Error : wrong flag received : " + rcvSegment.getFlags());
    	}

	}

    
    
    public void destroyConnection()  throws IOException, ClassNotFoundException
    {
    	/* Check for the error condition when the client send the SYN packet
    	 * even when the connection is open */
    	
    	Datagram datagram = ds.receiveDatagram();
		System.out.println("Received datagram from " + datagram.getSrcaddr() + ":" + datagram.getSrcport() + " Data: " + datagram.getData());    	
    	TTPSegment rcvSegment = (TTPSegment)datagram.getData();
    	
    	/* Checking for a FIN packet */
    	if(rcvSegment.getFlags() == 1)
    	{
	    	String data = "";
			int seqNumber = 200;
			int ackNumber = rcvSegment.getSeqNumber() + 1;			
			byte flag = 16;
			TTPSegment ackSeg = new TTPSegment(datagram.getDstport(), datagram.getSrcport(), seqNumber, ackNumber, (byte)16,  flag,  (short)750, (Object)data);	
			Datagram ack = new Datagram();
			ack.setSrcaddr(datagram.getDstaddr());
			ack.setDstaddr(datagram.getSrcaddr());
			ack.setDstport(datagram.getSrcport());
			ack.setSrcport(datagram.getDstport());
			ack.setData(ackSeg);			
			ds.sendDatagram(ack);
			System.out.println("Server : Sending datagram ACK");
			
			
			/* Check if the server has any more bytes to send to the server */
			
			
			/* Send another packet with a FIN status */

	    	data = "";
		    seqNumber = 201;
			ackNumber = rcvSegment.getSeqNumber() + 2;			
			flag = 1;
			ackSeg = new TTPSegment(datagram.getDstport(), datagram.getSrcport(), seqNumber, ackNumber, (byte)16,  flag,  (short)750, (Object)data);	
			ack.setData(ackSeg);			
			ds.sendDatagram(ack);
			System.out.println("Server : Sending datagram FIN");
			
						
			/* Wait for an ACK from client */
			datagram = ds.receiveDatagram();		
			System.out.println("Received datagram from " + datagram.getSrcaddr() + ":" + datagram.getSrcport() + " Data: " + datagram.getData());
			rcvSegment = (TTPSegment)datagram.getData();
			if(rcvSegment.getFlags() == 16)
	    	{
				System.out.println("Closing Connection Flag received is " + rcvSegment.getFlags());
				// Close the connection
				
	    	}
			else
			{
	    		System.out.println("Error : wrong flag received : " + rcvSegment.getFlags());
			}
    	}
    	else
    	{
    		System.out.println("Error : wrong flag received : " + rcvSegment.getFlags());
    	}
    }
		


    public void initiateDestroy(short srcPort,short dstPort,String srcAddr,String dstAddr) throws IOException, ClassNotFoundException
    {
    
    	
    	String data ="";
    	 
    	//Dummmy data.Should be changed
		int seqNumber=100;
		int ackNumber = 1000;			

		byte flag = 1;
    	
		TTPSegment seg = new TTPSegment(srcPort, dstPort, seqNumber, ackNumber, (byte)16,  flag,  (short)750, (Object)data);
		Datagram datagram = new Datagram();
		datagram.setSrcaddr(srcAddr);
		datagram.setDstaddr(dstAddr);
		datagram.setDstport(dstPort);
		datagram.setSrcport(srcPort);
		datagram.setData(seg);		
		ds.sendDatagram(datagram);

		//Should start timer now to wait for 2MSL that is 120 seconds

		try{  
			(new test(ds)).getInput();  
			}  
			catch( Exception e ){  
			System.out.println( e );  

			}  	
		

 
   	
    }
    
    
    
}

