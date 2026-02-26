package csc364;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Repository {
    private final Queue<String> queue = new LinkedList<>();
    private final Semaphore empty;
    private final Semaphore full;
    private final Lock lock = new ReentrantLock();

    public Repository(int capacity) {
        this.empty = new Semaphore(capacity);
        this.full = new Semaphore(0);
    }

    public void produce(String equation) throws InterruptedException {
        empty.acquire();
        lock.lock();
        try {
            queue.add(equation);
            System.out.println("Produced: " + equation + " | size=" + queue.size());
        } finally {
            lock.unlock();
        }
        full.release();
    }

    public String consume() throws InterruptedException {
        full.acquire();
        lock.lock();
        try {
            String eq = queue.remove();
            System.out.println("Worker Consumed: " + eq + " | size=" + queue.size());
            return eq;
        } finally {
            lock.unlock();
            empty.release();
        }
    }

    // Add this method to check if queue is empty
    public boolean queueIsEmpty() {
        lock.lock();
        try {
            return queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }
}


