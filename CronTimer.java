import java.util.Timer;
import java.util.TimerTask;


public class CronTimer {
    private final Timer timer = new Timer(true);
    private TimerTask task;

    public synchronized void start(long checkInterval, Runnable check) {
        stop(); // if started restart

        task = new TimerTask() {
            @Override
            public void run() {
                check.run();
            }
        };

        // run then run at every interval 
        timer.scheduleAtFixedRate(task, 0, checkInterval);
    }

    public synchronized void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public synchronized void shutdown() {
        stop();
        timer.cancel();
    }
}
