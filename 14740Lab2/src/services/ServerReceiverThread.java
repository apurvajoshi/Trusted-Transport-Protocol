package services;

import java.io.IOException;

import datatypes.Datagram;
import datatypes.TTPSegment;

public class ServerReceiverThread extends Thread {


	private static final boolean DATA_TO_BE_SENT = false;
	public DatagramService ds;
	public SenderThread senderThread;
	public int wait_for_final_ack;

	public ServerReceiverThread(DatagramService ds, SenderThread senderThread)
	{
		this.ds = ds;
		this.senderThread = senderThread;
	}
	public void destroyConnection(TTPSegment ackSeg)  throws IOException, ClassNotFoundException
    {
    	
		if(DATA_TO_BE_SENT)
		{
    	senderThread.createSegment(ackSeg.getSeqNumber(), TTPSegmentService.ACK,"");
    	senderThread.send();
		}
		else
		{
			 senderThread.createSegment(ackSeg.getSeqNumber(),TTPSegmentService.ACK_FIN,"");
		     wait_for_final_ack = 1;
			 senderThread.send();
		}
    	
    }


    public void run() {    	
    	while(true)
    	{
    		Datagram datagram;
    		try {
				datagram = ds.receiveDatagram();
				senderThread.timer.cancel();
				System.out.println("Received " + datagram.getData());
	       		TTPSegment ackSeg=(TTPSegment)(datagram.getData());

	       		switch(ackSeg.getFlags()) {
	    		case TTPSegmentService.SYN_ACK: 
	    			System.out.println("Connection established.");

	    			/* Send the next acknowledgment */
	    			senderThread.createSegment(ackSeg.getSeqNumber(), TTPSegmentService.ACK, "");
	    			senderThread.sendWithoutTimeout();
	    			break;
	    	/*Gautam code*/
	    	/*Handling a normal ack*/
	    		case TTPSegmentService.ACK:
	    			 /*Must send data*/
	    			  System.out.println("\nAcknowledgement recieved\n");
	    			  /*Check whether this is final acknowledgement*/
	    			  if(!(wait_for_final_ack == 1))
	    			  {
	    			 /*Send data*/
	    				  
	    			  }  
	    			  break;
	    		case TTPSegmentService.FIN:
	    			  System.out.println("\n Initiate destroy on server side\n");
	    			  //Should we do error check to see if client is receiving a FIN packet?
	    			  destroyConnection(ackSeg);
	    			  break;
	    			  
	    		}
	       		
	       		/*Close connection*/
	       		if(wait_for_final_ack == 1)
	       		{  
	       			System.out.println("\n Closing connection");
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
