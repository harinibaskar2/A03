package csc364;

import java.util.Timer;
import java.util.TimerTask;

/**
 * CronTimer runs a repeated task at a fixed interval.
 */
public class CronTimer {

    private final Timer timer = new Timer(true);
    private TimerTask task;

    /**
     * Starts the timer with the given interval (in milliseconds) and task.
     * If already running, it stops the previous task first.
     *
     * @param checkInterval interval in milliseconds
     * @param check the task to run
     */
    public synchronized void start(long checkInterval, Runnable check) {
        stop(); // Stop previous task if any

        task = new TimerTask() {
            @Override
            public void run() {
                check.run();
            }
        };

        // Run immediately, then repeat at fixed interval
        timer.scheduleAtFixedRate(task, 0, checkInterval);
    }

    /**
     * Stops the currently running task, if any.
     */
    public synchronized void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Shuts down the timer completely.
     */
    public synchronized void shutdown() {
        stop();
        timer.cancel();
    }
}

