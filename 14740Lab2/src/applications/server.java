
/*
 * A sample server that uses DatagramService
 */
package applications;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import services.TTPSegmentService;

public class server {

	public static TTPSegmentService ts;
    public static ServerSocket serverSocket = null;   
    private static int serverPort   = 6000;

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		/*if(args.length != 1) {
			printUsage();
		}
		
		int port = Integer.parseInt(args[0]);*/
		
		System.out.println("Starting Server ...");
		ts = new TTPSegmentService(serverPort, 10);
		run();
	}

	private static void run() throws IOException, ClassNotFoundException {		
		while(true) {
            // Span a new thread to service the request
			ts.acceptConnection();
			
		}
		
	}

	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}
}

