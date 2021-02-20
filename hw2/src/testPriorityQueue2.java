public class testPriorityQueue2 implements Runnable {
    PriorityQueue priorityQueue;

    public testPriorityQueue2(PriorityQueue pq) {
        priorityQueue = pq;
    }

    @Override
    public void run() {
        int result = priorityQueue.search("Item5");

        while (result == -1){ //check when added
            result = priorityQueue.search("Item5");
        }
        System.out.println("search thread 2: " + priorityQueue.search("Item5"));

        while (result != -1){ //check when removed
            result = priorityQueue.search("Item5");
        }
        System.out.println("search thread 2: " + priorityQueue.search("Item5"));
    }
}
