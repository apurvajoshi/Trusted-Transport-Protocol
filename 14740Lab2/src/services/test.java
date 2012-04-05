/*
 *  A Stub that provides datagram send and receive functionality
 *  
 *  Feel free to modify this file to simulate network errors such as packet
 *  drops, duplication, corruption etc. But for grading purposes we will
 *  replace this file with out own version. So DO NOT make any changes to the
 *  function prototypes
 */
package services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import datatypes.Datagram;

public class test extends Thread {
   private DatagramService ds;
   private Datagram datagram;
   
    public test(DatagramService ds)
    {
    	  this.ds = ds;
    }
	TimerTask task = new TimerTask(){  
		public void run() {  
			
			
			System.out.println("\n.................\n");
         if(datagram== null)
		System.out.println( "Must retransmit" );  
         //Go to some other function.
	
	
		}
		
		
};



public void getInput() throws Exception{  
Timer timer = new Timer();  
System.out.println("NANANNANAN");
//For now 20 seconds
timer.schedule( task, 20*1000 );  
  
System.out.println( "Input a string within 10 seconds: " );  
datagram= ds.receiveDatagram();
System.out.println( "Recieved\n");   
timer.cancel();
}  


}

