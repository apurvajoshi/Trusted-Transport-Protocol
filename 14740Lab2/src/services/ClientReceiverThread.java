package services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import datatypes.Datagram;
import datatypes.TTPSegment;

public class ClientReceiverThread extends Thread {

	
	
	public DatagramService ds;
	public SenderThread senderThread;
	/*Used to check whether this is the first time*/
	private int first_time =0;
	private String filename ;
	
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
	    		case TTPSegmentService.DATA:
	    			System.out.println("Client recieved data\n");
	    			
	    			
	    			
	    			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
	    			ObjectOutputStream oStream = new ObjectOutputStream( bStream );
	    			oStream.writeObject (ackSeg.getData());
	    			byte[] byteVal = bStream. toByteArray();
	    			
	    			
	    			System.out.println(Arrays.toString(byteVal));
	    		
	    			senderThread.createSegment(ackSeg.getAckNumber(), ackSeg.getSeqNumber()+1, TTPSegmentService.ACK, "");
	    			senderThread.send();
	    			/*Should put into reciever buffer*/
	    			
	    			
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