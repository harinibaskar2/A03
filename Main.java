public class Main {
    public static void main(String[] args) throws InterruptedException {
        Repository repository = new Repository(5);

        int numWorkers = 2; 

        Thread producer = new Thread(new Producer(repository, numWorkers), "Producer");

        Thread[] workers = new Thread[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new Thread(new LocalWorker(repository, i + 1), "LocalWorker-" + (i + 1));
            workers[i].start();
        }

        producer.start();

        
        producer.join();
        for (Thread w : workers) w.join();

        System.out.println("done");
    }
}
