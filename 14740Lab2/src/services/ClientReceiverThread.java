package services;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import datatypes.Datagram;
import datatypes.TTPSegment;

public class ClientReceiverThread extends Thread {

	
	
	public DatagramService ds;
	public SenderThread senderThread;
	/*Used to check whether this is the first time*/
	private int first_time =0;
	private String filename ;
	public static final int SEGMENT_SIZE = 492;
	
	public static List<byte[]> segmentList;
	public static int SegmentNumber=0;
 public static int total_number_of_segments=0;
 public static int Segments_expected =0;
	
	public ClientReceiverThread(DatagramService ds, SenderThread senderThread)
	{
		this.ds = ds;
		this.senderThread = senderThread;
		segmentList = new ArrayList<byte[]>();
	}
    public byte[] getNextSegment()
    {
    	byte[] segment = new byte[SEGMENT_SIZE];
    	if(SegmentNumber >Segments_expected)
    	{
    		 
    		 return null;
    	}
    	segment=segmentList.get(SegmentNumber);
    	SegmentNumber++;
    	return segment;
    	
   	 
    }
	
	
    public void run() {    	
    	while(true)
    	{
    		Datagram datagram;
    		try {
				datagram = ds.receiveDatagram();
				senderThread.timeoutTask.cancel();
				System.out.println("Received " + datagram.getData());
	       		TTPSegment ackSeg=(TTPSegment)(datagram.getData());
	       		System.out.println("\n Data is" + ackSeg.getData());
	       		switch(ackSeg.getFlags()) {
	       		
	    		case TTPSegmentService.SYN_ACK: 
	    			System.out.println("Client received  SYN_ACK.");
	    			
	    			/* Send the next acknowledgment */
	//senderThread.setDstPort(ackSeg.getSrcport());
	    			senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1, TTPSegmentService.ACK, "");
	    			senderThread.send();
	    			
	    			/* Set client state to established */
	    			TTPSegmentService.clientState = TTPSegmentService.ESTABLISHED;

	    			break;
	    			
	    		case TTPSegmentService.ACK:
	    			System.out.println("Client received  ACK.");
	    	
	    			/* Make sure if received ACK  is for FIN */
	    			if(TTPSegmentService.clientState == TTPSegmentService.FIN_WAIT_1 && 
	    					ackSeg.getData().toString().equals("FIN"))
	    			{
		    			System.out.println("Client FIN_WAIT_2.");
	    				TTPSegmentService.clientState = TTPSegmentService.FIN_WAIT_2;

	    			}
	    			else if (TTPSegmentService.clientState == TTPSegmentService.CLOSING &&
	    					ackSeg.getData().toString().equals("FIN"))
	    			{
	    				TTPSegmentService.clientState = TTPSegmentService.TIME_WAIT;
	    				try {
							sleep(TTPSegmentService.TIMEOUT * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.out.println("Issues in waiting.");
							e.printStackTrace();
						}
	    			}
	    			
	    			/* Do nothing */
	    			break;
	    			
	    		case TTPSegmentService.FIN:
	    			System.out.println("Client received FIN.");


	    			if(TTPSegmentService.clientState == TTPSegmentService.FIN_WAIT_1)
	    			{
	    				senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1, TTPSegmentService.ACK, ackSeg.getData());
		    			senderThread.send();
	    				TTPSegmentService.clientState = TTPSegmentService.CLOSING;
	    			}
	    			else if(TTPSegmentService.clientState == TTPSegmentService.FIN_WAIT_2)
	    			{
	    				senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1, TTPSegmentService.ACK, ackSeg.getData()); 
		    			senderThread.send();
	    				TTPSegmentService.clientState = TTPSegmentService.TIME_WAIT;
		    			System.out.println("Client TIME_WAIT.");
	    				try {
							sleep(TTPSegmentService.TIMEOUT * 1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.out.println("Issues in waiting.");
							e.printStackTrace();
						}
	    				
	    			}
	    			
	    			break;
	    		case TTPSegmentService.ACK_FIN:
	    			System.out.println("Client received ACK_FIN.");
	    			senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1, TTPSegmentService.ACK, "");
	    			senderThread.send();
	    			
	    			/* Set the state to TIME WAIT */
	    			TTPSegmentService.clientState = TTPSegmentService.TIME_WAIT;
	    			try {
						sleep(TTPSegmentService.TIMEOUT * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Issues in waiting.");
						e.printStackTrace();
					}
	    			break;
	    			
	    		case TTPSegmentService. All_DATA_SENT :
	    			System.out.println("data over");
	    			   //TTPSegmentService.clientState = TTPSegmentService.DATA_OVER;
	    		  
	    			 break;
	    		case TTPSegmentService.DATA:
	    			
	    			
	    			 total_number_of_segments++;
	    			 if(total_number_of_segments == Segments_expected)
	    				 
	    			 {
	    				 TTPSegmentService.clientState = TTPSegmentService.DATA_OVER;
	    			 }
	    			System.out.println("Client recieved data\n");
	    			
	    			
	    			System.out.println("\n Data is Before" + ackSeg.getData());
	      
	    		       System.out.println("\n Data is AFTER" + ackSeg.getData());
	    		       
	    		
	    			segmentList.add((byte[])ackSeg.getData());
	    		   	   
	    		   	   
	    			   senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1, TTPSegmentService.ACK, "");
	    			   senderThread.send();
	    			   /*Should put into reciever buffer*/
	    			break;
	    			
	    			
	    			
	    		case TTPSegmentService.SIZE:
	    			   System.out.println("Size is "+ Integer.parseInt(ackSeg.getData().toString()));
	    			   int size =Integer.parseInt(ackSeg.getData().toString());
	    			   Segments_expected =size /(SEGMENT_SIZE); 
	    			   System.out.println("Segments expected " + Segments_expected);
	    			   
	    			   senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1, TTPSegmentService.ACK, "");
	    			   senderThread.send();
	    			   break;
	    		}
	       		
	       		if(TTPSegmentService.clientState == TTPSegmentService.TIME_WAIT)
	       		{
	       			/* Set the state to Closed */
	    			TTPSegmentService.clientState = TTPSegmentService.CLOSED;
	    			System.out.println("Client Closed.");
	    			break;
	       		}
	       		
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("IOException in receiving data");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("ClassNotFoundException in receiver thread");
				e.printStackTrace();
			}
    	}
    }
}