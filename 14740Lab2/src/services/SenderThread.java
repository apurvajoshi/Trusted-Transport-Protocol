package services;

import java.io.IOException;
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
	public TTPSegment seg;
    public Timer timer;
    public TimeoutTask timeoutTask;

	public SenderThread (DatagramService ds, short srcPort, short dstPort, String srcAddr, String dstAddr)
	{
		this.ds = ds;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
        this.timer = new Timer();
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

	public TTPSegment createSegment(int seqNo, int ackNumber, byte flag, Object data)
	{		
		this.seg = new TTPSegment(this.srcPort, this.dstPort, seqNo, ackNumber, (byte)16,  flag,  (short)750, (Object)data);
		return this.seg;
	}

    public void send() {
    	try {
			ds.sendDatagram(createDatagram(this.seg));
			
			this.timeoutTask = new TimeoutTask(createDatagram(this.seg));
			System.out.println("Timer started");
		    timer.schedule(timeoutTask, 5*1000, 5*1000);
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
}