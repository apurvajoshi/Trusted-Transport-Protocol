/*
 * A sample client that uses DatagramService
 */

package applications;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import services.TTPSegmentService;

public class client {

	private static TTPSegmentService ts;
	public static int fileSize =0;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		if(args.length != 4) {
			printUsage();
		}
		short port = Short.parseShort(args[0]); 
		short serverPort = Short.parseShort(args[1]);
		int timer_interval = Integer.parseInt(args[2]); 
		String fileName = args[3];

		
		System.out.println("Starting client ...");

		ts = new TTPSegmentService(port,10);		
		TTPSegmentService.clientState = TTPSegmentService.CLOSED;
		ts.createConnection(port, serverPort,"127.0.0.1","127.0.0.1", timer_interval);

		System.out.println("Client Connection established.");
		System.out.println("\n\n");

		/* Now that connection is established, client can send filename */
        fileSize = ts.sendFileName(fileName);
        System.out.println("Client has sent filename\n");
        

        while(TTPSegmentService.clientState != TTPSegmentService.DATA_OVER)
        {
        	 ;
        }
        
        System.out.println("BACK ");

        byte[] segment;
        File file =new File("src/" + fileName);
		if(!file.exists()){
			file.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(file,true);
		while((segment=ts.recievePackets())!=null)
		{
		    fos.write(segment);

        }
      
		ts.closeConnection();
		System.out.println("Client Connection closed.");



	}
	
	private static void printUsage() {
		System.out.println("Usage: server <localport> <serverport> <timer_interval> <filename>\n");
		System.exit(-1);
	}
}
