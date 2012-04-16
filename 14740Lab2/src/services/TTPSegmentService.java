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
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import services.test;
import datatypes.Datagram;
import datatypes.TTPSegment;

public class TTPSegmentService{

	private DatagramService ds;

	public TTPSegmentService(int port, int verbose) throws SocketException  {
		super();
		ds = new DatagramService(port, verbose);
	}

	public DatagramService getDS()
	{
		 return ds;
	}

    public void createConnection(short srcPort,short dstPort,String srcAddr,String dstAddr) throws IOException, ClassNotFoundException
	{
		String data ="";
		int seqNumber=1;
		int ackNumber = 0;			
		byte flag = 2;
		int serverSeqNumber;	

		TTPSegment seg = new TTPSegment(srcPort, dstPort, seqNumber, ackNumber, (byte)16,  flag,  (short)750, (Object)data);

		Datagram datagram = new Datagram();
		datagram.setSrcaddr(srcAddr);
		datagram.setDstaddr(dstAddr);
		datagram.setDstport(dstPort);
		datagram.setSrcport(srcPort);
		datagram.setData(seg);		
		System.out.println("Datagram service port" + ds.getPort());
		ds.sendDatagram(datagram);
		datagram = ds.receiveDatagram();		
		System.out.println("Received " + datagram.getData());


		TTPSegment ackSeg=(TTPSegment)(datagram.getData());
		if(ackSeg.getFlags() == 18)
		{
			//Syn + Ack
			System.out.println("Connection established.");
			//Send the next acknowledgement

			//Making use of the same datagram.Is this fine?
			serverSeqNumber = ackSeg.getSeqNumber();
			seg.setFlags((byte)16);


			//Initially assume one byte is read
            seg.setSrcport(srcPort);
            seg.setDstport(dstPort);
			seg.setSeqNumber(seqNumber+1);
			seg.setAckNumber(serverSeqNumber + 1);


			datagram.setSrcport(srcPort);
			datagram.setDstport(dstPort);
			datagram.setSrcaddr(srcAddr);
			datagram.setDstaddr(dstAddr);
			datagram.setData(seg);
			ds.sendDatagram(datagram);
		}
		else
		{

			//ERROR
			System.out.println("Error");
		}
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
			System.out.println("Datagram service port" + ds.getPort());
			System.out.println("Server : Sending datagram");

			datagram = ds.receiveDatagram();		
			System.out.println("Received datagram from " + datagram.getSrcaddr() + ":" + datagram.getSrcport() + " Data: " + datagram.getData());
			rcvSegment = (TTPSegment)datagram.getData();
			System.out.println("Flag received is " + rcvSegment.getFlags());
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
		System.out.println("Datagram service port" + ds.getPort());
		ds.sendDatagram(datagram);

		//Should start timer now to wait for 2MSL that is 120 seconds


		try{  
			(new test(ds)).getInput();  
			}  
			catch( Exception e ){  
			System.out.println( e );  
			}  





	    //ds.receiveDatagram();	
    	
    	 
    	
    }
    
    
    
}

