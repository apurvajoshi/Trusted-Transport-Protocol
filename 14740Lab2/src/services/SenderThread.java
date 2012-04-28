package services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	private static int total_number_of_segments;
	public static final int SEGMENT_SIZE = 492;
	public static int SegmentNumber=0;


	public SenderThread (DatagramService ds, short srcPort, short dstPort, String srcAddr, String dstAddr)
	{
		this.ds = ds;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
        this.timer = new Timer();
        segmentList = new ArrayList<byte[]>();
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

	public Datagram createDatagram(TTPSegment seg)
	{
		Datagram datagram = new Datagram();
		datagram.setSrcaddr(srcAddr);
		datagram.setDstaddr(dstAddr);
		datagram.setDstport(dstPort);
		datagram.setSrcport(srcPort);
		datagram.setData(seg);	
		return datagram;
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
		    timer.schedule(timeoutTask, 5*1000, 5*1000);
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
            //timer.cancel(); //Terminate the timer thread
        }
    }
    
    

    
    
	 /* Reads file data into byte array and then partitions them */
	public  int readAndCreateSegments(File file) throws FileNotFoundException, IOException 
	{
	  	FileInputStream fis = new FileInputStream(file);
	  	BufferedInputStream bir = new BufferedInputStream(fis);
	  	byte[] fileContents = new byte[(int) file.length()];
		bir.read(fileContents);
		System.out.println(Arrays.toString(fileContents));
	  	createPacketList(fileContents);
	 	return fileContents.length;
	}
	 
	 /* Creates a list of data Objects.These can be sent to the function create segment */
	public  void createPacketList(byte[] readBuffer)
	{
	 	int length = readBuffer.length;
	 	byte[] segment = new byte[SEGMENT_SIZE];
	 	int offset = 0;
	 	int i = 0;
	 	
	 	while(offset < length)
	 	{
	 		if(length > SEGMENT_SIZE)
	 			segment = Arrays.copyOfRange(readBuffer, offset, (offset+ SEGMENT_SIZE));
	 		else
	 			segment = Arrays.copyOfRange(readBuffer, offset, (offset+ length));
	     	segmentList.add(segment);
	     	System.out.println("Segment is " + segment);
	     	i++;
	     	offset += SEGMENT_SIZE;
	 	
	 	}
	 	i--;
	 	total_number_of_segments = i;	
	 
	}
     
	public byte[] getNextSegment()
	{
	 	byte[] segment = new byte[SEGMENT_SIZE];
	 	if(SegmentNumber > total_number_of_segments)
	 	{
	 		 return null;
	 	}
	 	segment=segmentList.get(SegmentNumber);
	 	SegmentNumber++;
	 	return segment;
	}	
    
} 