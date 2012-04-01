/*
 * A sample server that uses DatagramService
 */
package applications;

import java.io.IOException;
import java.net.SocketException;

import services.DatagramService;
import datatypes.Datagram;

public class server {

	private static DatagramService ds;
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		if(args.length != 1) {
			printUsage();
		}
		
		System.out.println("Starting Server ...");
		
		int port = Integer.parseInt(args[0]);
		ds = new DatagramService(port, 10);
		
		run();
	}

	private static void run() throws IOException, ClassNotFoundException {

		Datagram datagram;
		
		while(true) {
			datagram = ds.receiveDatagram();
			System.out.println("Received datagram from " + datagram.getSrcaddr() + ":" + datagram.getSrcport() + " Data: " + datagram.getData());
			Datagram ack = new Datagram();
			ack.setSrcaddr(datagram.getDstaddr());
			ack.setSrcport(datagram.getDstport());
			ack.setDstaddr(datagram.getSrcaddr());
			ack.setDstport(datagram.getSrcport());
			ack.setData("ACK");
			ds.sendDatagram(ack);
		}
	}

	private static void printUsage() {
		System.out.println("Usage: server <port>");
		System.exit(-1);
	}
}
