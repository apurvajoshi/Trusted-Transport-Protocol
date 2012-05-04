package services;

import java.io.BufferedInputStream;
import java.security.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import datatypes.Datagram;
import datatypes.TTPSegment;

public class SenderThread {

	/* Definition of all constant variables */

	public DatagramService ds;
	public short srcPort;
	public short dstPort;
	public String srcAddr;
	public String dstAddr;
	public int seqNo;
	
	public TTPSegment seg;
    public Timer timer;
    public TimeoutTask timeoutTask;
	public List<byte[]> segmentList;
	public static final int SEGMENT_SIZE = 496;
	public static int TIMER_INTERVAL = 5; // IN MS



	public SenderThread (DatagramService ds, short srcPort, short dstPort, String srcAddr, String dstAddr, int timer)
	{
		this.ds = ds;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
        this.timer = new Timer();
        segmentList = new ArrayList<byte[]>();
        TIMER_INTERVAL = timer;
	}
	
	public void setSeqNo(int seqNo)
	{
		this.seqNo = seqNo;
	}
	
	public void setDstPort(short port)
	{
		this.dstPort = port;
	}

	public void setTTPSegment(TTPSegment seg)
	{
		this.seg = seg;
	}

	
    public short calculate_checksum(byte[] data)
    {
    	short checksum=0;
    	int overflow_flag=0;
    	 for(int i =0;i<(data.length-1);i+=2)
    	 {   
    		  overflow_flag=0;
    		  checksum += (data[i]<<8)|(data[i+1]);
    		  if((((data[i]>>7) & 0x01) == 1) && (((data[i+1]>>7) & 0x01)==1))
    		  {
    			   overflow_flag=1;
    		  }
    	 }
    	if(overflow_flag ==1)
    	{ 
    		 checksum =(short)(checksum +1);
    	}
    	 checksum = (short) ~checksum;
    	 return checksum;
    }

	public Datagram createDatagram(TTPSegment seg) 
	{
		Datagram datagram = new Datagram();
		datagram.setSrcaddr(srcAddr);
		datagram.setDstaddr(dstAddr);
		datagram.setDstport(dstPort);
		datagram.setSrcport(srcPort);
		
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		ObjectOutputStream oStream = null;
		try {
			oStream = new ObjectOutputStream( bStream );
			oStream.writeObject (seg.getData());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		byte[] byteVal = bStream.toByteArray();
		short checksum = calculate_checksum(byteVal);
	    datagram.setChecksum(checksum);
	    
		datagram.setData(seg);	
		return datagram;
	}


    
   
    public byte[] fileToByteArray(File file) throws FileNotFoundException, IOException{  
        int length = (int) file.length();  
        byte[] array = new byte[length];  
        InputStream in = new FileInputStream(file);  
        int offset = 0;  
        while (offset < length) {  
            in.read(array, offset, (length - offset));  
            offset += length;  
        }  
        in.close();  
        return array;  
        }  
    
    public byte[] caclulate_file_checksum(File file) throws IOException, NoSuchAlgorithmException 
    {
     	FileInputStream fis = new FileInputStream(file);
	  	BufferedInputStream bir = new BufferedInputStream(fis);
	  	byte[] fileContents = new byte[(int) (file.length()-1)];
		bir.read(fileContents);
    	MessageDigest md = MessageDigest.getInstance("MD5");
    	byte[] thedigest = md.digest(fileContents);
    	return thedigest;
    }

	public TTPSegment createSegment(int ackNumber, byte flag, Object data)
	{		
		this.seg = new TTPSegment(this.srcPort, this.dstPort, this.seqNo, ackNumber, (byte)16,  flag,  (short)750, (Object)data);
		this.seqNo += TTPSegmentService.sizeOf(data);
		return this.seg;
	}
	

    public void send() {
    	try {

    		this.timeoutTask = new TimeoutTask(createDatagram(this.seg));

			System.out.println("Timer started");
		    timer.schedule(timeoutTask, TIMER_INTERVAL*1000, TIMER_INTERVAL*1000);
			ds.sendDatagram(createDatagram(this.seg));
		} catch (IOException e) { 
			// TODO Auto-generated catch block
			System.out.println("Exception in sending datagram");
			e.printStackTrace();
		}
    }
    
    public void sendWithoutTimeout() {
    	try {
			ds.sendDatagram(createDatagram(this.seg));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception in sending datagram");
			e.printStackTrace();
		}
    }
    
    public class TimeoutTask extends TimerTask {
    	public Datagram datagram;
    	
    	public TimeoutTask(Datagram datagram)
    	{
    		this.datagram = datagram;
    	}
    	

    	
        public void run() {
            System.out.format("Time's up!%n");
            /* Send this datagram again */
			try {
				ds.sendDatagram(this.datagram);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
	 /* Reads file data into byte array and then partitions them */
	public  int readAndCreateSegments(File file) throws FileNotFoundException, IOException 
	{
	  	FileInputStream fis = new FileInputStream(file);
	  	BufferedInputStream bir = new BufferedInputStream(fis);
	  	byte[] fileContents = new byte[(int) (file.length()-1)];
		bir.read(fileContents);
		//System.out.println("File in bytes is ");
		//System.out.println(Arrays.toString(fileContents));
	  	createPacketList(fileContents);
	 	return fileContents.length;
	}
	 
	 /* Creates a list of data Objects.These can be sent to the function create segment */
	public  void createPacketList(byte[] readBuffer)
	{
	 	int length = readBuffer.length;
	 	byte[] segment = new byte[SEGMENT_SIZE];
	 	int offset = 0;
	 	
	 	while(offset < length)
	 	{
	 		if(length > SEGMENT_SIZE)
	 			segment = Arrays.copyOfRange(readBuffer, offset, (offset+ SEGMENT_SIZE));
	 		else
	 			segment = Arrays.copyOfRange(readBuffer, offset, (offset+ length));
	     	segmentList.add(segment);
	     	
	     	offset += SEGMENT_SIZE;	 	
	 	}	 
	}	
    
} 