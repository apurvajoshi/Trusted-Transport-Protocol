package services;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import datatypes.Datagram;
import datatypes.TTPSegment;

public class ServerReceiverThread extends Thread {
	public DatagramService ds;
	public SenderThread senderThread;
	public WindowTimer windowTimer;
	public int clientExpectedSeqNo;
	File file ;
    
	public ServerReceiverThread(DatagramService ds, SenderThread senderThread, WindowTimer windowTimer)
	{
		this.ds = ds;
		this.senderThread = senderThread;
		this.windowTimer = windowTimer;
	}
	
	 public void sendGoBackN(List<byte[]> segmentList) throws NoSuchAlgorithmException  
	    {


	 		if(TTPSegmentService.window.size() < TTPSegmentService.MAX_WINDOW_SIZE)
	    	{
				TTPSegment s;    	
				
				System.out.println("Window before? " + TTPSegmentService.window.size());
				
				/* Add data to the window */
				while(!segmentList.isEmpty() && TTPSegmentService.window.size() < TTPSegmentService.MAX_WINDOW_SIZE)
				{
		 			byte[] data = segmentList.get(0);
					s = senderThread.createSegment(0, TTPSegmentService.DATA_GO_BACK, data);
					System.out.println("Sending data starting with seq no : " + s.getSeqNumber());
					if(TTPSegmentService.window.size() == 0)
					{
						windowTimer.startTimer(senderThread);
					}
					TTPSegmentService.window.add(s);
					segmentList.remove(0);
					
					senderThread.sendWithoutTimeout();
				}
				
				System.out.println("Window full " + TTPSegmentService.window.size());
	    	}
	    	else
	    	{
	    		System.out.println("Window is full");
	    	}
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
	    		case TTPSegmentService.ACK:
	    			  System.out.println("Server received ACK");	
	    			  
	    			  
    				  /* Set the server state to ESTABLISHED */
	    			  if(TTPSegmentService.serverState == TTPSegmentService.SYN_RECEIVED && 
	    					  ackSeg.getAckNumber() == this.senderThread.seqNo)
	    				  TTPSegmentService.serverState = TTPSegmentService.ESTABLISHED;
	    			  
	    			  else if (TTPSegmentService.serverState == TTPSegmentService.LAST_ACK && 
	    					  ackSeg.getData().toString().equals("FIN"))
	    			  {
	    				  System.out.println("Server closed.");
	    				  TTPSegmentService.serverState = TTPSegmentService.CLOSED;
	    			  }

	    			  else if(  TTPSegmentService.serverState == TTPSegmentService.ESTABLISHED)
	    			  {  	    				  
	    				  System.out.println("received ack for packet " + ackSeg.getAckNumber());
	    				  int index = -1;
	    				  /* Check if the received packet is in window */
	    				  for(int i = 0 ; i < TTPSegmentService.window.size(); i++)
	    				  {
	    					  TTPSegment s = TTPSegmentService.window.get(i);
	    					  if(s.getSeqNumber() == ackSeg.getAckNumber())
	    					  {
	    						  index = i;
	    						  System.out.println("New index is " + index);
	    					  }
	    				  }
	    				  
	    				  if(index != -1 )
	    				  {
	    					 
	    					  this.windowTimer.stopTimer();

		    				  for(int i = index; i >= 0; i--)
		    				  {
		    					  TTPSegment s = TTPSegmentService.window.get(i);
		    					  System.out.println("Element to be removed is " + s.getSeqNumber());
	    						  TTPSegmentService.window.remove(i);
		    				  }
		    				  
		    				  /* Check if there are more packets to send */
		    				  if(!senderThread.segmentList.isEmpty())
		    					  sendGoBackN(senderThread.segmentList);
		    				  
		    				  if(TTPSegmentService.window.size() == 0)
		    				  {
		    					  System.out.println("Stopping the window timer");
		    					  this.windowTimer.stopTimer();
		    				  }
		    				  else
		    				  {
		    					  this.windowTimer.startTimer(this.senderThread);
		    				  }
	    				  }
	    			  }
	    			  break;
	    			  
	    		case TTPSegmentService.FIN:
	    			  System.out.println("Server received FIN");

	    			  this.clientExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
	    			  senderThread.createSegment(clientExpectedSeqNo,TTPSegmentService.ACK, ackSeg.getData());

	    			  senderThread.send();
	    		
	    			

	    			  TTPSegmentService.serverState = TTPSegmentService.CLOSE_WAIT;
	    			  
	    			  /* 
	    			   * 
	    			   * Send data if anything is remaining.
	    			   * 
	    			   * 
	    			   */

	    			  

	    			  senderThread.createSegment(clientExpectedSeqNo,TTPSegmentService.FIN,"FIN");

	    			  senderThread.send();
	    			  TTPSegmentService.serverState = TTPSegmentService.LAST_ACK;
	    			  break;
	    			  

	    		case TTPSegmentService.ACK_FILESIZE:
	    			  sendGoBackN(senderThread.segmentList);
	    			  break;

	    			  
	    		case TTPSegmentService.FILEPATH:
					  System.out.println("Filename recieved is "+ ackSeg.getData());
					  file = new File("src/applications/" + ackSeg.getData());
					  
					  int length = senderThread.readAndCreateSegments(file);
					
					  System.out.println("Back");
						  	    			 
					  this.clientExpectedSeqNo = ackSeg.getSeqNumber() + TTPSegmentService.sizeOf(ackSeg.getData());
		    		  senderThread.createSegment(clientExpectedSeqNo ,TTPSegmentService.FILESIZE, length);
		    		  senderThread.send();
		    		  break;	    			  

	    		}
	       		

	       		/* Close connection */
	       		if(TTPSegmentService.serverState == TTPSegmentService.CLOSED)
	       		{
	       			senderThread.timer.cancel();
	       			windowTimer.timer.cancel();
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
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }

	
}
