package services;

import java.io.IOException;

import datatypes.Datagram;
import datatypes.TTPSegment;

public class ServerReceiverThread extends Thread {
	public DatagramService ds;
	public SenderThread senderThread;
	public int wait_for_final_ack = 0;

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
				datagram = ds.receiveDatagram();
				senderThread.timeoutTask.cancel();
				System.out.println("Server Received " + datagram.getData());
	       		TTPSegment ackSeg=(TTPSegment)(datagram.getData());

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
	    			  break;
	    			  
	    		case TTPSegmentService.FIN:
	    			  System.out.println("Server received FIN");
	    			  senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1,TTPSegmentService.ACK, ackSeg.getData());
	    			  senderThread.send();

	    			  TTPSegmentService.serverState = TTPSegmentService.CLOSE_WAIT;
	    			  
	    			  /* 
	    			   * 
	    			   * Send data if anything is remaining.
	    			   * 
	    			   * 
	    			   */

	    			  
	    			  senderThread.createSegment(ackSeg.getAckNumber()+1 , ackSeg.getSeqNumber()+2 ,TTPSegmentService.FIN,"FIN");
	    			  senderThread.send();
	    			  TTPSegmentService.serverState = TTPSegmentService.LAST_ACK;
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
