package services;

import java.util.Timer;
import java.util.TimerTask;

import datatypes.TTPSegment;

public class TimerThread {
    public Timer timer;
	public SenderThread senderThread;

    public TimerThread(SenderThread senderThread , TTPSegment seg, int seconds) {
		this.senderThread = senderThread;
        timer = new Timer();
        TimeoutTask t = new TimeoutTask(seg);
        timer.schedule(t, seconds*1000);
	}

    class TimeoutTask extends TimerTask {
    	public TTPSegment seg;
    	
    	public TimeoutTask(TTPSegment seg)
    	{
    		this.seg = seg;
    	}

        public void run() {
            System.out.format("Time's up!%n");
            /* Send the datagram again */
            senderThread.setTTPSegment(this.seg);
            senderThread.send();
            //timer.cancel(); //Terminate the timer thread
        }
    }
}