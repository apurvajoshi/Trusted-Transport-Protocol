/*
 * A sample client that uses DatagramService
 */

package applications;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import services.TTPSegmentService;

public class client {

	private static TTPSegmentService ts;
	private static final short serverPort = 6000;
	public static final short clientPort = 6001;
	public static int fileSize =0;
	public static String filename = "a.txt";
	
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
		
		/* Send data go back n */
		
		/* Now that connection is established, client can send filename */
        fileSize = ts.sendFileName(filename);
        System.out.println("Client has sent filename\n");
        
        
        while(TTPSegmentService.clientState != TTPSegmentService.DATA_OVER)
        {
        	 ;
        }
        System.out.println("BACK ");
        byte[] segment; 
		while((segment=ts.recievePackets())!=null)
        {
        	File file =new File("src/received.txt");
			if(!file.exists()){
    			file.createNewFile();
    		}
			
			FileOutputStream fos = new FileOutputStream(file,true);
		    ObjectOutputStream oos = new ObjectOutputStream(fos);
	     	fos.write(segment);
        }
		
		ts.closeConnection();
		System.out.println("Client Connection closed.");


	}
	
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport>\n");
		System.exit(-1);
	}
}
