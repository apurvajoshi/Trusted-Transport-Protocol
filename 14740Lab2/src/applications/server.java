/*
 * A sample server that uses DatagramService
 */
package applications;

import java.io.IOException;
import java.net.SocketException;

import services.DatagramService;
import services.TTPSegmentService;
import datatypes.Datagram;
import datatypes.TTPSegment;

public class server {

	//public TTPSegmentService ts;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		if(args.length != 1) {
			printUsage();
		}
		
		System.out.println("Starting Server ...");
		
		int port = Integer.parseInt(args[0]);
		TTPSegmentService ts = new TTPSegmentService(port, 10);
		
		while(true) {			
			ts.acceptConnection();
		}
		//run();
	}

	private static void run() throws IOException, ClassNotFoundException {

		
		
	}

	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}
}
