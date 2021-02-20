public class testPriorityQueue implements Runnable {
    PriorityQueue priorityQueue;

    public testPriorityQueue(PriorityQueue pq) {
        priorityQueue = pq;
    }

    public static void main(String[] args) {
        PriorityQueue priorityQueue = new PriorityQueue(10);

        Thread t1 = new Thread(new testPriorityQueue(priorityQueue));
        Thread t2 = new Thread(new testPriorityQueue2(priorityQueue));

        try {
            t1.start();
            t2.start();

            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        priorityQueue.printList();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        // System.out.println("removed: " + priorityQueue.getFirst());
        for (int i = 0; i < 6; i++){
            priorityQueue.add("Item" + i, i);
        }

        priorityQueue.printList();
        System.out.println("search thread 1: " + priorityQueue.search("Item5"));

        System.out.println("removed: " + priorityQueue.getFirst());
        System.out.println("removed: " + priorityQueue.getFirst());
    }
}
