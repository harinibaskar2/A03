package csc364;

import java.util.List;



public class Helper {
    public interface TimeoutHandler {
        void onTimeout(Notetaker.JobRecord record);
    }

    private final Notetaker notetaker = new Notetaker();
    private final CronTimer cronTimer = new CronTimer();

    private final TimeoutHandler timeoutHandler;

    // how much time we are going to allow each job to take 
    private final long maxJobTime;

    // interval of when to check
    private final long checkInterval;

    public Helper(long maxJobTime, long checkInterval, TimeoutHandler timeoutHandler) {
        this.maxJobTime = maxJobTime;
        this.checkInterval = checkInterval;
        this.timeoutHandler = timeoutHandler;
        cronTimer.start(checkInterval, this::checkForTimeouts);
    }

    public Notetaker getNotetaker() {
        return notetaker;
    }

    public synchronized void onAssign(String workerId, String jobId, String jobContent) {
        notetaker.assign(workerId, jobId, jobContent);
    }

    private synchronized void checkForTimeouts() {
        long now = System.currentTimeMillis();

        List<Notetaker.JobRecord> records = notetaker.snapshotRecords();
        for (Notetaker.JobRecord rec : records) {
            long elapsed = now - rec.startTime;
            if (elapsed > maxJobTime) {
                notetaker.workerDone(rec.workerId);
                timeoutHandler.onTimeout(rec);
            }
        }
    }

    public void shutdown() {
        cronTimer.shutdown();
    }
}
