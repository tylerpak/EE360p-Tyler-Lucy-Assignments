//UT-EID= tjp2365, lwz83

import java.util.ArrayList;

public class FairUnifanBathroom {
	private int OUin = 0;
	private int UTin = 0;
	private int ticketNumber = 0;
	private int assignTicket = -1;

	
  public synchronized void enterBathroomUT() {
  	int myTicket = assignTicket + 1;
  	assignTicket += 1;
  	while(ticketNumber != myTicket) {
		try {
			wait();
		} catch (InterruptedException e) {

		}
	}
  	while((OUin > 0) || (UTin >= 4)) {
		try {
			wait();
		} catch (InterruptedException e) {

		}
	}
  	UTin += 1;
  	ticketNumber += 1;
  	System.out.println("UT fan entered. Fans inside = "+  UTin + ". Ticket number = " + myTicket);
  }
	
  public synchronized void enterBathroomOU() {
    // Called when a OU fan wants to enter bathroom
	  int myTicket = assignTicket + 1;
	  assignTicket += 1;
	  while(ticketNumber != myTicket) {
		  try {
			  wait();
		  } catch (InterruptedException e) {

		  }
	  }
	  while((UTin > 0) || (OUin >= 4)) {
		  try {
			  wait();
		  } catch (InterruptedException e) {

		  }
	  }
	  OUin += 1;
	  ticketNumber += 1;
	  System.out.println("OU fan entered. Fans inside = "+  OUin + ". Ticket number = " + myTicket);
  }
	
  public synchronized void leaveBathroomUT() {
    // Called when a UT fan wants to leave bathroom
	  UTin -= 1;
	  System.out.println("UT fan left. Fans inside = " +  UTin);
	  notifyAll();
  }

  public synchronized void leaveBathroomOU() {
    // Called when a OU fan wants to leave bathroom
	  OUin -= 1;
	  System.out.println("OU fan left. Fans inside = "+  OUin);
	  notifyAll();
  }
}
	
