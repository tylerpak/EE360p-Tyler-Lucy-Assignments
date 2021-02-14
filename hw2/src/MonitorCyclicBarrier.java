//UT-EID= tjp2365, lwz83

public class MonitorCyclicBarrier {
	int totalParties;
	int currentlyWaiting;
	boolean released;
	int exited;
	
	public MonitorCyclicBarrier(int parties) {
		totalParties = parties;
		currentlyWaiting = 0;
		released = false;
		exited = 0;
	}
	
	public synchronized int await() throws InterruptedException {
		int index = totalParties - currentlyWaiting - 1;
		currentlyWaiting++;
		while (currentlyWaiting < totalParties && !released){
			wait();
		}

		currentlyWaiting--;
		released = true;
		exited++;
		notifyAll();
		
		if (exited == totalParties){
			released = false;
			exited = 0;
		}
	    return index;
	}
}
