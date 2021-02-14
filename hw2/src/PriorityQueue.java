//UT-EID= tjp2365, lwz83

import java.util.concurrent.locks.*;

public class PriorityQueue {
        Node head;
        int maxSize;
        int currentSize;
        // Shouldn't have this?
        Lock list = new ReentrantLock();
        Condition isFull = list.newCondition();
        Condition isEmpty = list.newCondition();

        // Creates a Priority queue with maximum allowed size as capacity
        public PriorityQueue(int maxSize) {
                this.maxSize = maxSize;
                currentSize = 0;
                head = null;
        }

        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // This method blocks when the list is full.
        public int add(String name, int priority) {
                while (currentSize >= maxSize) {
                        try {
                                isFull.await();
                                // wait();
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                }

                // insert into LinkedList
                Node node = new Node(name, priority);
                list.lock();
                Node newNext = head;
                list.unlock();
                Node newPrev = null;
                int index = -1;
                
                while (newNext != null) {
                        index++;
                        newNext.mutex.lock();

                        if (newNext.priority < priority) {
                                break;
                        }
                        
                        if (newPrev != null)
                                newPrev.mutex.unlock();

                        newPrev = newNext;
                        newNext = newNext.next;
                }

                //both nextNode and prevNode are already locked
                if (newNext != null && newPrev != null) {
                        node.next = newNext;
                        newNext.next = node;
                        newPrev.mutex.unlock();
                        newNext.mutex.unlock();
                } else if (newNext != null && newPrev == null) {
                        node.next = newNext;
                        head = node;
                        newNext.mutex.unlock();
                } else {
                        head = node;
                }
                currentSize++;
                isEmpty.signalAll();
                // notifyAll();

                return index;
        }

        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
        public int search(String name) {
                int index = -1;
                Node node = head;
                boolean found = false;

                while (node != null) {
                        index++;
                        node.mutex.lock();
                        if (node.name == name) {
                                node.mutex.unlock();
                                found = true;
                                break;
                        }
                        node.mutex.unlock();
                        node = node.next;
                }

                if (!found){
                        return -1;
                }

                return index;
        }

        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
        public String getFirst() {
                while (currentSize == 0) {
                        try {
                                isEmpty.await();
                                // wait();
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
                
                // remove from linked list
                list.lock();
                Node node = head;
                Node newHead = head.next;
                node.mutex.lock();
                list.unlock();

                if (newHead != null)
                        newHead.mutex.lock();
                
                node.next = null;
                head = newHead;

                if (newHead != null)
                        newHead.mutex.unlock();
                        
                node.mutex.unlock();

                currentSize--;
                isFull.signalAll();
                // notifyAll();
                return node.name;
	}

        // For testing purposes
        public void printList(){
                Node node = head;

                while (node != null){
                        System.out.print(node + ", ");
                }
        }

        /**
         * Node used in PriorityQueue
         */
        public class Node{
                String name;
                int priority;
                Lock mutex;
                Node next;

                public Node(String name, int priority){
                        this.name = name;
                        this.priority = priority;
                        mutex = new ReentrantLock();
                        next = null;
                }

                // For testing purposes
                @Override
                public String toString() {
                        return name + ": " + priority;
                }
        }
}

