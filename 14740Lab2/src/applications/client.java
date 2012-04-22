/*
 * A sample client that uses DatagramService
 */

package applications;

import java.io.IOException;
import services.TTPSegmentService;

public class client {

	private static TTPSegmentService ts;
	private static final short serverPort = 6000;
	public static final short clientPort = 6001;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		/*if(args.length != 2) {
			printUsage();
		}
		int port = Integer.parseInt(args[0]);*/ 
		
		System.out.println("Starting client ...");

		ts = new TTPSegmentService(clientPort,10);		
		ts.createConnection(clientPort, serverPort,"127.0.0.1","127.0.0.1");
		System.out.println("Client Connection established.");

		System.out.println("\n\n");

		
		ts.closeConnection();
		System.out.println("Client Connection closed.");

	}
	
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}
