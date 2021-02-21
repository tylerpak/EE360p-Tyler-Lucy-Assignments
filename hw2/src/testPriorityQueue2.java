public class testPriorityQueue2 implements Runnable {
    PriorityQueue priorityQueue;

    public testPriorityQueue2(PriorityQueue pq) {
        priorityQueue = pq;
    }

    @Override
    public void run() {
        int result = priorityQueue.search("Item5");
        int count = 0;
        final int WAIT = 3000;

        while (result == -1 && count < WAIT){ //check when added
            result = priorityQueue.search("Item5");
            count++;
        }
        System.out.println("search thread 2: " + priorityQueue.search("Item5"));
        count = 0;

        while (result != -1 && count < WAIT){ //check when removed
            result = priorityQueue.search("Item5");
            count++;
        }
        System.out.println("search thread 2: " + priorityQueue.search("Item5"));
    }
}
