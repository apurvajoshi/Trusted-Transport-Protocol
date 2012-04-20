/*
 * A sample client that uses DatagramService
 */

package applications;

import java.io.IOException;
import services.TTPSegmentService;

public class client {

	private static TTPSegmentService ts;

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

		ts = new TTPSegmentService(6001,10);		
		ts.createConnection((short)6001, (short)6000,"127.0.0.1","127.0.0.1");
		//ts.initiateDestroy((short)6001, (short)6000, "127.0.0.1", "127.0.0.1");

		/*ts = new TTPSegmentService(port,10);
		ts.createConnection((short)port,(short)Integer.parseInt(args[1]),"127.0.0.1","127.0.0.1");
		ts.initiateDestroy((short)port, (short)Integer.parseInt(args[1]), "127.0.0.1", "127.0.0.1");*/
	}

	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}