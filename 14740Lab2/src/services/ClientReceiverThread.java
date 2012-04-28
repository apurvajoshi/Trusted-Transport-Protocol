package services;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import datatypes.Datagram;
import datatypes.TTPSegment;

public class ClientReceiverThread extends Thread {

	
	
	public DatagramService ds;
	public SenderThread senderThread;
	public int serverExpectedSeqNo;
	public int fileSize;
	public List<byte[]> segmentList;
	public static final int SEGMENT_SIZE = 492;
	public int segmentsExpected =0;
	public static int segmentNumber=0;


	
	public ClientReceiverThread(DatagramService ds, SenderThread senderThread)
	{
		this.ds = ds;
		this.senderThread = senderThread;
		segmentList = new ArrayList<byte[]>();
	}
	
	public byte[] getNextSegment()
    {
    	byte[] segment;
    	if(segmentNumber > 0)
    	{
    		segment=segmentList.get(0);
    		segmentList.remove(0);
        	segmentNumber--;
        	return segment;
    	}
		return null;
    }
	
    public void run() {    	
    	while(true)
    	{
    		Datagram datagram;
    		try {
				datagram = ds.receiveDatagram();
				senderThread.timeoutTask.cancel();
	       		TTPSegment ackSeg=(TTPSegment)(datagram.getData());
	       		switch(ackSeg.getFlags()) {
	       		
	    		case TTPSegmentService.SYN_ACK: 
	    			System.out.println("Client received  SYN_ACK.");
	    			
	    			/* Send the next acknowledgment */
	    			//senderThread.setDstPort(ackSeg.getSrcport());
    				this.serverExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
    				senderThread.createSegment(this.serverExpectedSeqNo, TTPSegmentService.ACK, "a");
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
	    				this.serverExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
	    				senderThread.createSegment(serverExpectedSeqNo, TTPSegmentService.ACK, ackSeg.getData());
		    			senderThread.send();
	    				TTPSegmentService.clientState = TTPSegmentService.CLOSING;
	    			}
	    			else if(TTPSegmentService.clientState == TTPSegmentService.FIN_WAIT_2)
	    			{
	    				this.serverExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
	    				senderThread.createSegment(serverExpectedSeqNo, TTPSegmentService.ACK, ackSeg.getData()); 
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
    				this.serverExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
	    			senderThread.createSegment(serverExpectedSeqNo, TTPSegmentService.ACK, "a");
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
	    			
	    		case TTPSegmentService.DATA_GO_BACK:
	    			System.out.println("Client recieved DATA_GO_BACK. expecting : " + this.serverExpectedSeqNo);
	    			if(ackSeg.getSeqNumber() == this.serverExpectedSeqNo)
	    			{
	    				senderThread.createSegment(serverExpectedSeqNo, TTPSegmentService.ACK, "a");
	    				senderThread.sendWithoutTimeout();
		    			
		    			// ACK packet with received seq # 
	    				this.serverExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
	    				
	    				/* Add the received data into buffer */
		    			segmentList.add((byte[])ackSeg.getData());
		    			segmentNumber++;
		    			
		    			if(segmentNumber == segmentsExpected)
			    			TTPSegmentService.clientState = TTPSegmentService.DATA_OVER;
	    			}
	    			else
	    			{
	    				// Re ACK packet with highest inorder seq # 
	    				senderThread.createSegment(serverExpectedSeqNo, TTPSegmentService.ACK, "a");
		    			senderThread.sendWithoutTimeout();
	    			}
	    			break;
	    			
	    		case TTPSegmentService.FILESIZE:
	    			System.out.println("Client recieved FIELSIZE " + Integer.parseInt(ackSeg.getData().toString()));
	    			fileSize = Integer.parseInt(ackSeg.getData().toString());
	    			segmentsExpected = (int) Math.ceil(fileSize /(SEGMENT_SIZE)); 
	    			System.out.println("Segments expected " + segmentsExpected);
	    			segmentNumber = 0;
	    			
	    			senderThread.createSegment(serverExpectedSeqNo, TTPSegmentService.ACK_FILESIZE, "a");
	    			senderThread.send();
    				this.serverExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
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