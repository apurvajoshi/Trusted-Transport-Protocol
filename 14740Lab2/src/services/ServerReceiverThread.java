package services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import datatypes.Datagram;
import datatypes.TTPSegment;

public class ServerReceiverThread extends Thread {


	public DatagramService ds;
	public SenderThread senderThread;
	public int wait_for_final_ack = 0;
	public int final_ack_recieved=0;
	public int server_first_time=0;
	public static String fileName ;
	public static final int SEGMENT_SIZE = 496;
	File file ;
    
	public ServerReceiverThread(DatagramService ds, SenderThread senderThread)
	{
		this.ds = ds;
		this.senderThread = senderThread;
	}

    public void run() {    	
    	
		while(true)
    	{
    		Datagram datagram;
    		try {
    			System.out.println("Server state is"+ TTPSegmentService.serverState);
				datagram = ds.receiveDatagram();
				senderThread.timeoutTask.cancel();
				System.out.println("Server Received " + datagram.getData());
	       		TTPSegment ackSeg=(TTPSegment)(datagram.getData());
	       		System.out.println("Data is "+ ackSeg.getData().toString());

	       		switch(ackSeg.getFlags()) {
	    		case TTPSegmentService.ACK:

	    			  System.out.println("Server received ACK");	
	    			  
	   
	    			  
	    			  /* Set the server state to ESTABLISHED */
	    			  if(TTPSegmentService.serverState == TTPSegmentService.SYN_RECEIVED)
	    				  TTPSegmentService.serverState = TTPSegmentService.ESTABLISHED;
	    			  else if (TTPSegmentService.serverState == TTPSegmentService.LAST_ACK && 
	    					  ackSeg.getData().toString().equals("FIN"))
	    			  {
			    			System.out.println("Server closed.");
		    			  TTPSegmentService.serverState = TTPSegmentService.CLOSED;
	    			  }
	    			else if(  TTPSegmentService.serverState == TTPSegmentService.ESTABLISHED)
	    			  {
	    				  byte[] segment = new byte[SEGMENT_SIZE];
	    				  byte [] dummy2 ={0};
	    				  segment =senderThread.getNextSegment();
	    				  if(segment!=null)
	    				  {
	    					  System.out.println("Just before sending "+ segment);
	    					  
	    					  Object obj = null;
	    					  try {
	    					    ByteArrayInputStream bis = new ByteArrayInputStream (segment);
	    					    ObjectInputStream ois = new ObjectInputStream (bis);
	    					    obj = ois.readObject();
	    					  }
	    					  catch (IOException ex) {
	    					    System.out.println("IO exception");
	    					  }
	    					  catch (ClassNotFoundException ex) {
	    					    //TODO: Handle the exception
	    					  }
	    					   
	    					  
	    				   senderThread.createSegment(ackSeg.getAckNumber()+1 , ackSeg.getSeqNumber()+2 ,TTPSegmentService.DATA,segment);
	   	    		    senderThread.send();
	    				  }
	    				  else
	    				  {
	    					   System.out.println("\n Data over");
	    					   //Must change
	    					
	    					   senderThread.createSegment(ackSeg.getAckNumber()+1 , ackSeg.getSeqNumber()+2 ,TTPSegmentService.DATA,dummy2);
	   	   	    		    senderThread.send();
	    					
	    				  }
	    			  }
	  
	    			  break;
	    			  
	    		case TTPSegmentService.FIN:
	    			  System.out.println("Server received FIN");
	    			  byte [] dummy ={0};
	    			 senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1,TTPSegmentService.ACK, ackSeg.getData());
	    			 // senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1,TTPSegmentService.ACK, dummy);
	    			  senderThread.send();

	    			  TTPSegmentService.serverState = TTPSegmentService.CLOSE_WAIT;
	    			  
	    			  /* 
	    			   * 
	    			   * Send data if anything is remaining.
	    			   * 
	    			   * 
	    			   */

	    			  
	    			senderThread.createSegment(ackSeg.getAckNumber()+1 , ackSeg.getSeqNumber()+2 ,TTPSegmentService.FIN,"FIN");
	    			//  senderThread.createSegment(ackSeg.getAckNumber()+1 , ackSeg.getSeqNumber()+2 ,TTPSegmentService.FIN,dummy);
	    			  senderThread.send();
	    			  TTPSegmentService.serverState = TTPSegmentService.LAST_ACK;
	    			  break;
	    			  
	    		case TTPSegmentService.FIRST:
	    			System.out.println("\n Filename recieved is "+ datagram.getData().toString());
	    		    file = new File(ackSeg.getData().toString());
	    		    
	    		    int size =senderThread.readAndCreateSegments(file);
	    		    System.out.println("\n Back\n");
	    		   
  				    
  				 //   System.out.println("Segment Size is" + segment_send.length);
  				    
  				    
	    		    byte [] dummy1={0} ;
	    		    senderThread.createSegment(ackSeg.getAckNumber()+1 , ackSeg.getSeqNumber()+2 ,TTPSegmentService.SIZE,size);
	    		 //   senderThread.createSegment(ackSeg.getAckNumber()+1 , ackSeg.getSeqNumber()+2 ,TTPSegmentService.SIZE,dummy1);
	    		    senderThread.send();
	    			break;
	    			  
	    		}
	       		

	       		/* Close connection */
	       		if(TTPSegmentService.serverState == TTPSegmentService.CLOSED)
	       		{
	       			senderThread.timer.cancel();
		    		System.out.println("Server closed connection");
	       			break;
	       		}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("IOException in server receiving data");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("ClassNotFoundException in server receiver thread");
				e.printStackTrace();
			}
    	}
    }

	
}
