package csc364;



public class LocalWorker implements Runnable {
    private final Repository repository;
    private final int workerId;
    public LocalWorker(Repository repository, int workerId) {
        this.repository = repository;
        this.workerId = workerId;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String eq = repository.consume();

                if ("STOP".equals(eq)) {
                    System.out.println("LocalWorker-" + workerId + " exiting.");
                    return;
                }

                double result = evaluate(eq);
                System.out.println("LocalWorker-" + workerId + " solved: " + eq + " = " + result);

                Thread.sleep(800);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private double evaluate(String equation) {
        String[] parts = equation.trim().split("\\s+");
        int a = Integer.parseInt(parts[0]);
        String op = parts[1];
        int b = Integer.parseInt(parts[2]);

        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> (double) a / b;
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };
    }
}
