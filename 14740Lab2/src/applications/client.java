/*
 * A sample client that uses DatagramService
 */

package applications;

import java.io.IOException;
import java.net.SocketException;

import services.DatagramService;
import datatypes.Datagram;

public class client {

	private static DatagramService ds;
	
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
		ds = new DatagramService(port, 10);
		
		Datagram datagram = new Datagram();
		datagram.setData("Hello World!");
		datagram.setSrcaddr("127.0.0.1");
		datagram.setDstaddr("127.0.0.1");
		datagram.setDstport((short)Integer.parseInt(args[1]));
		datagram.setSrcport((short)port);
		
		ds.sendDatagram(datagram);
		System.out.println("Sent datagram");
		
		datagram = ds.receiveDatagram();
		System.out.println("Received " + datagram.getData());
	}
	
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}
