package csc364;
import org.eclipse.paho.client.mqttv3.*;

public class Outsourcer implements
        MqttCallback,
        Helper.TimeoutHandler {

    private final MqttClient client;
    private final Helper helper;

    private static final String REQUEST_TOPIC =
            "csc364/work/request";

    private static final String RESULT_TOPIC =
            "csc364/work/result";

    private long jobCounter = 0;

    public Outsourcer(String broker,
                      String clientId) throws Exception {

        client = new MqttClient(broker, clientId);
        client.setCallback(this);
        client.connect();

        helper = new Helper(
                5000,   // 5 sec max job time
                1000,   // check every 1 sec
                this
        );

        client.subscribe(REQUEST_TOPIC);
        client.subscribe(RESULT_TOPIC);

        System.out.println("Outsourcer ready.");
    }

    @Override
    public void messageArrived(String topic,
                               MqttMessage message) throws Exception {

        String payload = new String(message.getPayload());

        if (topic.equals(REQUEST_TOPIC)) {
            handleWorkerRequest(payload);
        }

        else if (topic.equals(RESULT_TOPIC)) {
            handleResult(payload);
        }
    }

    private void handleWorkerRequest(String workerId)
            throws Exception {

        String jobId =
                workerId + "-" + System.currentTimeMillis();

        String jobContent =
                "Task_" + (++jobCounter);

        String assignTopic =
                "csc364/work/assign/" + workerId;

        client.publish(assignTopic,
                new MqttMessage(jobContent.getBytes()));

        helper.onAssign(workerId, jobId, jobContent);

        System.out.println("Assigned job to " + workerId);
    }

    private void handleResult(String payload) {

        String[] parts = payload.split(":");
        String workerId = parts[0];

        helper.getNotetaker().workerDone(workerId);

        System.out.println("Completed by " + workerId);
    }

    @Override
    public void onTimeout(Notetaker.JobRecord record) {

        System.out.println("TIMEOUT: " + record);

        try {
            String retryTopic =
                    "csc364/work/assign/retry";

            client.publish(retryTopic,
                    new MqttMessage(record.jobContent.getBytes()));

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

