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
	public void destroyConnection(TTPSegment ackSeg)  throws IOException, ClassNotFoundException
    {
		/* Check if there is data to be sent */

		/* If yes then send data with ACK flag */
		
		/* If no then respond with FIN ACK MSG*/
		senderThread.createSegment(ackSeg.getSeqNumber(),TTPSegmentService.ACK_FIN,"");
	    wait_for_final_ack = 1;
	    senderThread.sendWithoutTimeout();
    }


    public void run() {    	
    	while(true)
    	{
    		Datagram datagram;
    		try {
				datagram = ds.receiveDatagram();
				senderThread.timer.cancel();
				System.out.println("Server Received " + datagram.getData());
	       		TTPSegment ackSeg=(TTPSegment)(datagram.getData());

	       		switch(ackSeg.getFlags()) {
	    		case TTPSegmentService.ACK:
	    			  System.out.println("Server received ACK");
	    			  break;
	    			  
	    		case TTPSegmentService.FIN:
	    			  System.out.println("Server received FIN");
	    			  destroyConnection(ackSeg);
	    			  break;
	    			  
	    		}
	       		
	       		/* Close connection */
	       		if(wait_for_final_ack == 1)
	       			 break;

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
