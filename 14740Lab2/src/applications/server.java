/*
 * A sample server that uses DatagramService
 */
package applications;

import java.io.IOException;
import datatypes.Datagram;
import datatypes.TTPSegment;
import services.TTPSegmentService;

public class server {

	public static TTPSegmentService ts;
	public static short port;
	public static int window_size;
	public static int timer_interval;
    //private static final short serverListenPort = 6000;
    //private static final short serverRespondPort = 6000;
	//public static final short clientPort = 6001;


	public static void main(String[] args) throws IOException, ClassNotFoundException {

		if(args.length != 3) {
			printUsage();
		}
	
		port = Short.parseShort(args[0]);
		window_size = Integer.parseInt(args[1]);
		timer_interval = Integer.parseInt(args[2]);

		System.out.println("Starting Server ...");
		ts = new TTPSegmentService(port, 10);
		TTPSegmentService.serverState = TTPSegmentService.CLOSED;

		run();
	}

	private static void run() throws IOException, ClassNotFoundException {		
		if(true) 
		{
			/* Initial blocking call waiting for the first SYN packet. */
	       	Datagram datagram = ts.getDS().receiveDatagram(); 
	       	
	       	/* Set the server state to LISTEN */
	       	TTPSegmentService.serverState = TTPSegmentService.LISTEN;
	       	
	       	TTPSegment ackSeg=(TTPSegment)(datagram.getData());
	       	if(ackSeg.getFlags() == TTPSegmentService.SYN)
	       	{
	       		/* Create a new connection */
	       		System.out.println("Received SYN from client");
	       			       		
	       		/* Accept connection - > set the received sequence number + 1 as the acknowledgment number*/
	       		ts.acceptConnection(ackSeg.getSrcport(), port, "127.0.0.1","127.0.0.1", 
	       				TTPSegmentService.sizeOf(ackSeg.getData()),  window_size, timer_interval);
	    		System.out.println("Server Connection established.\n\n");

	       	}	       	
	       		       	
		}
	}

	private static void printUsage() {
		System.out.println("Usage: server <port> <window_size> <timer_interval>");
		System.exit(-1);
	}
}