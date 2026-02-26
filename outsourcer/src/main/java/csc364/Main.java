package csc364;

public class Main {
    public static void main(String[] args) throws Exception {
        // --- Step 1: Repository and Producer ---
        Repository repository = new Repository(10);

        int numLocalWorkers = 2;
        int numRemoteWorkers = 2;

        String[] jobs = {
            "2 + 3",
            "17 - 1",
            "22 * 5",
            "15 / 7",
            "8 * 8"
        };

        // Fill repository using Producer
        Thread producer = new Thread(() -> {
            try {
                for (String job : jobs) repository.produce(job);
                // Add "STOP" signals for local workers
                for (int i = 0; i < numLocalWorkers; i++) repository.produce("STOP");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");
        producer.start();

        // --- Step 2: Start LocalWorkers ---
        Thread[] localWorkers = new Thread[numLocalWorkers];
        for (int i = 0; i < numLocalWorkers; i++) {
            localWorkers[i] = new Thread(new LocalWorker(repository, i + 1), "LocalWorker-" + (i + 1));
            localWorkers[i].start();
        }

        // --- Step 3: Start Outsourcer ---
        Outsourcer outsourcer = new Outsourcer("tcp://localhost:1883", "Outsourcer");

        // Push jobs from Repository to Outsourcer queue for RemoteWorkers
        while (!repository.queueIsEmpty()) {
            String job = repository.consume();
            if (!job.equals("STOP")) outsourcer.addJob(job);
        }

        // --- Step 4: Start RemoteWorkers ---
        Thread[] remoteWorkers = new Thread[numRemoteWorkers];
        for (int i = 0; i < numRemoteWorkers; i++) {
            RemoteWorker rw = new RemoteWorker("tcp://localhost:1883", "Worker" + (i + 1));
            remoteWorkers[i] = new Thread(rw, "RemoteWorker-" + (i + 1));
            remoteWorkers[i].start();
        }

        // Wait for local workers to finish
        for (Thread w : localWorkers) w.join();

        // Wait for producer to finish
        producer.join();

        // RemoteWorkers run indefinitely, can add logic to stop after all jobs done
        System.out.println("Local workers done, Outsourcer + RemoteWorkers running...");
    }
}