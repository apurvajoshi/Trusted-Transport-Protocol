/*
 * A sample client that uses DatagramService
 */

package applications;

import java.io.IOException;
import java.net.SocketException;

import services.DatagramService;
import services.TTPSegmentService;
import datatypes.Datagram;

public class client {

	private static TTPSegmentService ts;
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if(args.length != 2) {
			printUsage();
		}
		
		System.out.println("Starting client ...");
		

		int port = Integer.parseInt(args[0]);
		//ds = new DatagramService(port, 10);
		
		ts = new TTPSegmentService(port,10);
		ts.createConnection((short)port,(short)Integer.parseInt(args[1]),"127.0.0.1","127.0.0.1");
	}
	
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}
