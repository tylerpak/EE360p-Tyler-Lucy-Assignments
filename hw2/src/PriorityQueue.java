//UT-EID= tjp2365, lwz83

import java.util.concurrent.locks.*;

public class PriorityQueue {
        Node head;
        int maxSize;
        volatile int currentSize;
        Lock lockHead = new ReentrantLock();
        Condition isFull = lockHead.newCondition();
        Condition isEmpty = lockHead.newCondition();

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
                try {
                        lockHead.lock();
                        while (currentSize >= maxSize) {
                                try {
                                        isFull.await();
                                } catch (Exception e) {
                                        e.printStackTrace();
                                }   
                        }
                } finally {
                        lockHead.unlock();
                }

                // insert into LinkedList
                Node node = new Node(name, priority);

                lockHead.lock();
                Node newNext = head;
                lockHead.unlock();
                Node newPrev = null;
                int index = 0;
                
                while (newNext != null) {
                        newNext.mutex.lock();

                        if (newNext.name == name){
                                return -1;
                        }
                        else if (newNext.priority < priority) {
                                break;
                        }
                        
                        if (newPrev != null)
                                newPrev.mutex.unlock();

                        newPrev = newNext;
                        newNext = newNext.next;
                        index++;
                }

                //both nextNode and prevNode are already locked
                if (newNext != null && newPrev != null) {
                        node.next = newNext;
                        newPrev.next = node;
                        newPrev.mutex.unlock();
                        newNext.mutex.unlock();
                } else if (newNext != null && newPrev == null) {
                        node.next = newNext;
                        lockHead.lock();
                        head = node;
                        lockHead.unlock();
                        newNext.mutex.unlock();
                } else if (newNext == null && newPrev != null){
                        newPrev.next = node;
                        newPrev.mutex.unlock();
                } else {
                        lockHead.lock();
                        head = node;
                        lockHead.unlock();
                }
                currentSize++;
                lockHead.lock();
                isEmpty.signalAll();
                lockHead.unlock();
                return index;
        }

        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
        public int search(String name) {
                int index = 0;
                lockHead.lock();
                Node node = head;
                lockHead.unlock();
                boolean found = false;

                while (node != null) {
                        node.mutex.lock();
                        if (name.compareTo(node.name) == 0) {
                                node.mutex.unlock();
                                found = true;
                                break;
                        }
                        node.mutex.unlock();
                        node = node.next;
                        index++;
                }

                if (!found){
                        return -1;
                }

                return index;
        }

        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
        public String getFirst() {
                try {
                        lockHead.lock();
                        while (currentSize == 0) {
                                try {
                                        isEmpty.await();
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                        }
                } finally {
                        lockHead.unlock();
                }

                // remove from linked list
                lockHead.lock();
                Node node = head;
                Node newHead = head.next;
                lockHead.unlock();
                node.mutex.lock();

                if (newHead != null)
                        newHead.mutex.lock();
                
                node.next = null;
                head = newHead;

                if (newHead != null)
                        newHead.mutex.unlock();
                        
                node.mutex.unlock();

                currentSize--;
                lockHead.lock();
                isFull.signalAll(); //wakes up all threads that were waiting for a node to be removed
                lockHead.unlock();
                return node.name;
	}

        // For testing purposes
        public void printList(){
                Node node = head;

                while (node != null){
                        System.out.print(node + ", ");
                        node = node.next;
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
                Condition inUse;

                public Node(){
                        name = "head";
                        priority = 10; //creates a head node
                        mutex = new ReentrantLock();
                        next = null;
                        inUse = mutex.newCondition();
                }

                public Node(String name, int priority){
                        this.name = name;
                        this.priority = priority;
                        mutex = new ReentrantLock();
                        next = null;
                        inUse = mutex.newCondition();
                }

                // For testing purposes
                @Override
                public String toString() {
                        return name + ": " + priority;
                }
        }
}

