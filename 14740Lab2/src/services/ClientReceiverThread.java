package services;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.List;

import datatypes.Datagram;
import datatypes.TTPSegment;

public class ClientReceiverThread extends Thread {	
	public DatagramService ds;
	public SenderThread senderThread;

	public int serverExpectedSeqNo;
	public int previousSeqNo;
	public int fileSize;
	public List<byte[]> listOfSegments;
	public static final int SEGMENT_SIZE = 496;
	public int segmentsExpected =0;
	public int flag=0;
	public int segmentNumber=0;
	public int checkVar = 0;

	
	public ClientReceiverThread(DatagramService ds, SenderThread senderThread)
	{
		this.ds = ds;
		this.senderThread = senderThread;
		this.listOfSegments = new ArrayList<byte[]>();
	}

	
	public short calculate_checksum(byte[] data)
	{
		short checksum=0;
		int overflow_flag=0;
		for(int i =0;i<(data.length-1);i+=2)
		{   
			overflow_flag=0;
			checksum += (data[i]<<8)|(data[i+1]);
			if((((data[i]>>7) & 0x01) == 1) && (((data[i+1]>>7) & 0x01)==1))
			{
				overflow_flag=1;
			}
		}
		if(overflow_flag ==1)
		{ 
			checksum =(short)(checksum +1);
		}
		checksum = (short) ~checksum;
		return checksum;
	}
	   
	public byte[] getNextSegment()
    {
    	byte[] segment;
    	if(segmentNumber > 0)
    	{
    		segment=this.listOfSegments.get(0);
    		this.listOfSegments.remove(0);
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
	       		TTPSegment ackSeg=(TTPSegment)(datagram.getData());
	       		System.out.println("\nThe first seq no : " + ackSeg.getSeqNumber());
				senderThread.timeoutTask.cancel();
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
	    			else
	    			{
		    			System.out.println(ackSeg.getData());
		    			System.exit(-1);
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
	    			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
    				ObjectOutputStream oStream = new ObjectOutputStream( bStream );
    				oStream.writeObject (ackSeg.getData());
    				byte[] byteVal = bStream. toByteArray();
    			
    				short checksum_data = calculate_checksum(byteVal);
    			
    				
    				/* Uncomment to check for checksum error
    				if(flag==2)
    				{
    					checksum_data = (short) (checksum_data & 0x1);
    					System.out.println("Data is incorrect");
    			 
    				}
    				flag++;
                  
                  
                    if(((short)checksum_data == (short)datagram.getChecksum()))
                    {
                    	System.out.println("TRUE");
                    }
                    else 
                    	System.out.println("False"); */
                    
    				if(checkVar != 2 )
    				{
    				
	    			if(ackSeg.getSeqNumber() == this.serverExpectedSeqNo && ((short)checksum_data == (short)datagram.getChecksum()))
	    			{

	    				senderThread.createSegment(serverExpectedSeqNo, TTPSegmentService.ACK, "a");
	    				senderThread.sendWithoutTimeout();

	    				this.previousSeqNo =  ackSeg.getSeqNumber();


		    			// ACK packet with received seq # 
	    				this.serverExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
	    		
	    				/* Add the received data into buffer */
	    				byte[] fileBytes = (byte[])ackSeg.getData();
	    				listOfSegments.add(fileBytes);
		    				
		    			segmentNumber++;
		    			
		    			if(segmentNumber == segmentsExpected)
			    			TTPSegmentService.clientState = TTPSegmentService.DATA_OVER;
	    			}
	    			else
	    			{
	    				// Re ACK packet with highest inorder seq # 
	    				senderThread.createSegment(this.previousSeqNo, TTPSegmentService.ACK, "a");
		    			senderThread.send();
	    			}
	    			
    				}
	    			
	    			checkVar++;
	    			break;
	    		case TTPSegmentService.FILESIZE:
	    			System.out.println("Client recieved FIELSIZE " + Integer.parseInt(ackSeg.getData().toString()));
	    			fileSize = Integer.parseInt(ackSeg.getData().toString());
	    			segmentsExpected = (int)Math.ceil((float)fileSize / (SEGMENT_SIZE)); 
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