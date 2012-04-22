package services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import datatypes.Datagram;
import datatypes.TTPSegment;

public class ServerReceiverThread extends Thread {


	public DatagramService ds;
	public SenderThread senderThread;
	public int wait_for_final_ack = 0;
	public int final_ack_recieved=0;
    
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
	    			  //If current state is just connection established then data will have filename.Perform a check
	    			  //Read data into buffer by using readFileAsString.Keep this buffer.Start segmentation and keep track of offset.
	    			  
	    		   		/* Close connection */
	  	       		if(wait_for_final_ack == 1)
	  	       		{  
	  	       			System.out.println("\n Closing connection");
	  	       			final_ack_recieved=1;
	  	       			 break;
	  	       		}
	    			  break;
	    			  
	    		case TTPSegmentService.FIN:
	    			  System.out.println("Server received FIN");
	    			  destroyConnection(ackSeg);
	    			  break;
	    			  
	    		}
	       		
	       		if(final_ack_recieved == 1)
  	       		{  
  	       			System.out.println("\n Closing connection");
  	       			final_ack_recieved=1;
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
    private static String readFileAsString(String filePath)
    throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
	
}
