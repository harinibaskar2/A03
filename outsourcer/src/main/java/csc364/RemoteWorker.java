package csc364;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class RemoteWorker implements MqttCallback, Runnable {

    private final MqttClient client;
    private final String workerId;

    public RemoteWorker(String broker, String workerId) throws Exception {
        this.workerId = workerId;
        client = new MqttClient(broker, workerId);
        client.setCallback(this);
        client.connect();

        // Subscribe to assigned jobs and retry jobs
        client.subscribe("csc364/work/assign/" + workerId);
        client.subscribe("csc364/work/assign/retry");

        System.out.println("RemoteWorker " + workerId + " ready.");
    }

    // Request a new job from Outsourcer (Publisher role)
    private void requestJob() throws Exception {
        client.publish("csc364/work/request", new MqttMessage(workerId.getBytes()));
        System.out.println(workerId + " requested a job.");
    }

    // Solve a job (Solver role)
    private String solveJob(String jobContent) {
        try {
            String[] parts = jobContent.trim().split("\\s+");
            int a = Integer.parseInt(parts[0]);
            String op = parts[1];
            int b = Integer.parseInt(parts[2]);

            double result = switch (op) {
                case "+" -> a + b;
                case "-" -> a - b;
                case "*" -> a * b;
                case "/" -> (double) a / b;
                default -> throw new IllegalArgumentException("Unknown operator: " + op);
            };
            return Double.toString(result);
        } catch (Exception e) {
            return "ERROR";
        }
    }

    // Handle incoming messages (Subscriber role)
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String jobContent = new String(message.getPayload());
        System.out.println(workerId + " received job: " + jobContent);

        // Solve the job
        String result = solveJob(jobContent);

        // Publish result back to Outsourcer
        String resultPayload = workerId + ":" + result;
        client.publish("csc364/work/result", new MqttMessage(resultPayload.getBytes()));
        System.out.println(workerId + " completed job: " + result);

        // Automatically request next job
        requestJob();
    }

    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    @Override
    public void run() {
        try {
            // Initial job request to start the pull model
            requestJob();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}