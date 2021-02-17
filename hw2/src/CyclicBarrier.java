//UT-EID= tjp2365, lwz83

import java.util.concurrent.Semaphore; // for implementation using Semaphores
import java.util.concurrent.TimeUnit;

public class CyclicBarrier {
	private int waiting;
	private int threshold;
	public Semaphore sem;
	
	public CyclicBarrier(int parties) {
		this.sem = new Semaphore(0, true);
		this.waiting = 0;
		this.threshold = parties;
	}
	
	public int await() throws InterruptedException {
		int index = threshold - 1;
		index = index - waiting;

		waiting +=1;
		if(waiting == threshold) {
			sem.release(threshold);
			waiting = 0;
		}
		sem.acquire();
		TimeUnit.NANOSECONDS.sleep(10);
		return index;
	}
}
