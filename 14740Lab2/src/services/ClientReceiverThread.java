package services;

import java.io.IOException;

import datatypes.Datagram;
import datatypes.TTPSegment;

public class ClientReceiverThread extends Thread {

	
	
	public DatagramService ds;
	public SenderThread senderThread;
	
	
	public ClientReceiverThread(DatagramService ds, SenderThread senderThread)
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
	    			
	    		case TTPSegmentService.ACK:
	    			/* Do nothing */
	    			break;
	    		case TTPSegmentService.FIN:
	    			senderThread.createSegment(ackSeg.getSeqNumber(), TTPSegmentService.ACK, "");
	    			senderThread.sendWithoutTimeout();
	    			try {
						wait(TTPSegmentService.TIMEOUT * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Issues in waiting.");
						e.printStackTrace();
					}
	    			break;
	    		case TTPSegmentService.ACK_FIN:
	    			senderThread.createSegment(ackSeg.getSeqNumber(), TTPSegmentService.ACK, "");
	    			senderThread.sendWithoutTimeout();
	    			try {
						wait(TTPSegmentService.TIMEOUT * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Issues in waiting.");
						e.printStackTrace();
					}
	    			break;
	    		}
	       		
	       		if(ackSeg.getFlags() == TTPSegmentService.FIN || ackSeg.getFlags() == TTPSegmentService.ACK_FIN)
	       			break;
	       		
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