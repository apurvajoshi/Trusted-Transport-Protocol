/*
 * A sample server that uses DatagramService
 */
package applications;

import java.io.IOException;
import services.TTPSegmentService;

public class server {

	public static TTPSegmentService ts;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		if(args.length != 1) {
			printUsage();
		}
		
		System.out.println("Starting Server ...");
		
		int port = Integer.parseInt(args[0]);
		ts = new TTPSegmentService(port, 10);
		
		run();
	}

	private static void run() throws IOException, ClassNotFoundException {
		while(true) {			
			ts.acceptConnection();
		}
	}

	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}
}
