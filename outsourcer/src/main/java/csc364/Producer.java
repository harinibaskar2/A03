package csc364;

public class Producer implements Runnable {
    private final Repository repository;
    private final int numWorkers;

    public Producer(Repository repository, int numWorkers) {
        this.repository = repository;
        this.numWorkers = numWorkers;
    }

    @Override
    public void run() {
        String[] equations = {
            "2 + 3",
            "17 - 1",
            "22 * 5",
            "15 / 7"
        };
        try {
            for (String eq : equations) {
                repository.produce(eq);
                Thread.sleep(300);
            }

            for (int i = 0; i < numWorkers; i++) {
                repository.produce("STOP");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}