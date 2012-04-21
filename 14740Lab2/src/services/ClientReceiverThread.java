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
	    			System.out.println("Client received  SYN_ACK.");
	    			System.out.println("Client Connection established." + ackSeg.getSrcport());
	    			/* Send the next acknowledgment */
	    			senderThread.setDstPort(ackSeg.getSrcport());
	    			senderThread.createSegment(ackSeg.getSeqNumber(), TTPSegmentService.ACK, "");
	    			senderThread.sendWithoutTimeout();
	    			System.out.println("Client closing connection");
	    			senderThread.createSegment(0, TTPSegmentService.FIN, "");
	    			senderThread.send();
	    			break;
	    			
	    		case TTPSegmentService.ACK:
	    			System.out.println("Client received  ACK.");
	    			/* Do nothing */
	    			break;
	    			
	    		case TTPSegmentService.FIN:
	    			System.out.println("Client received FIN.");

	    			senderThread.createSegment(ackSeg.getSeqNumber(), TTPSegmentService.ACK, "");
	    			senderThread.sendWithoutTimeout();
	    			try {
						sleep(TTPSegmentService.TIMEOUT * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Issues in waiting.");
						e.printStackTrace();
					}
	    			break;
	    		case TTPSegmentService.ACK_FIN:
	    			System.out.println("Client received ACK_FIN.");
	    			senderThread.createSegment(ackSeg.getSeqNumber(), TTPSegmentService.ACK, "");
	    			senderThread.sendWithoutTimeout();
	    			try {
						sleep(TTPSegmentService.TIMEOUT * 1000);
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