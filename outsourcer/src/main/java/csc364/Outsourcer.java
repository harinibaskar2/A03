package csc364;

import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Outsourcer implements MqttCallback, Helper.TimeoutHandler {

    private final MqttClient client;
    private final Helper helper;
    private final Queue<String> jobQueue = new LinkedList<>();

    private static final String REQUEST_TOPIC = "csc364/work/request";
    private static final String RESULT_TOPIC = "csc364/work/result";
    private static final String RETRY_TOPIC = "csc364/work/assign/retry";

    private long jobCounter = 0;

    public Outsourcer(String broker, String clientId) throws Exception {
        client = new MqttClient(broker, clientId);
        client.setCallback(this);
        client.connect();

        // Helper: maxJobTime=5s, checkInterval=1s
        helper = new Helper(5000, 1000, this);

        // Subscribe to worker requests and results
        client.subscribe(REQUEST_TOPIC);
        client.subscribe(RESULT_TOPIC);

        System.out.println("Outsourcer ready.");
    }

    // Add jobs to internal queue (from Repository or prefilled)
    public synchronized void addJob(String jobContent) {
        jobQueue.add(jobContent);
        System.out.println("Job added to queue: " + jobContent + " | queue size=" + jobQueue.size());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());

        if (topic.equals(REQUEST_TOPIC)) {
            handleWorkerRequest(payload);
        } else if (topic.equals(RESULT_TOPIC)) {
            handleResult(payload);
        }
    }

    private synchronized void handleWorkerRequest(String workerId) throws Exception {
        if (jobQueue.isEmpty()) {
            System.out.println("No jobs available for worker " + workerId);
            return;
        }

        String jobContent = jobQueue.poll(); // Take job from queue
        String jobId = workerId + "-" + System.currentTimeMillis();
        String assignTopic = "csc364/work/assign/" + workerId;

        client.publish(assignTopic, new MqttMessage(jobContent.getBytes()));
        helper.onAssign(workerId, jobId, jobContent);

        System.out.println("Assigned job to " + workerId + ": " + jobContent);
    }

    private void handleResult(String payload) {
        // Payload format: workerId:result
        String[] parts = payload.split(":");
        if (parts.length < 2) {
            System.out.println("Invalid result format: " + payload);
            return;
        }
        String workerId = parts[0];

        helper.getNotetaker().workerDone(workerId);

        System.out.println("Completed by " + workerId + ": " + parts[1]);
    }

    @Override
    public void onTimeout(Notetaker.JobRecord record) {
        System.out.println("TIMEOUT: " + record);

        try {
            // Republish the job for retry
            client.publish(RETRY_TOPIC, new MqttMessage(record.jobContent.getBytes()));
            System.out.println("Job republished for retry: " + record.jobContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}
}


