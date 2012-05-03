package services;

import java.util.Timer;
import java.util.TimerTask;
import datatypes.TTPSegment;

public class WindowTimer {

    public Timer timer;
    public WindowTimeoutTask timeoutTask;
	public static final int TIMER_INTERVAL = 20;

	public WindowTimer()
	{
        this.timer = new Timer();
	}
	
	public void startTimer(SenderThread s) {
		System.out.println("Window Timer started");
    	this.timeoutTask = new WindowTimeoutTask(s);
	    timer.schedule(timeoutTask, TIMER_INTERVAL*1000, TIMER_INTERVAL*1000);
    }
    
    public void stopTimer() {
    	this.timeoutTask.cancel();
    }
	
    public class WindowTimeoutTask extends TimerTask {
        public SenderThread senderThread;
    	
    	public WindowTimeoutTask(SenderThread s)
    	{
    		this.senderThread = s;
    	}

        public void run() {
            System.out.println("Window's Timer up!");            
            /* Send all the Unacked packets in the window */
			for(int i = 0; i < TTPSegmentService.window.size(); i++)
			{
				TTPSegment seg = TTPSegmentService.window.get(i);
				System.out.println("Window -> Sending data starting with seq no : " + seg.getSeqNumber());
				this.senderThread.setTTPSegment(seg);
				this.senderThread.sendWithoutTimeout();
			}
			
        }
    }

}