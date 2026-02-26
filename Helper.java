import java.util.List;

public class Helper {
    public interface TimeoutHandler {
        void onTimeout(Book.JobRecord record);
    }

    private final Book book = new Book();
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

    public Book getBook() {
        return book;
    }

    public synchronized void onAssign(String workerId, String jobId, String jobContent) {
        book.assign(workerId, jobId, jobContent);
    }

    private synchronized void checkForTimeouts() {
        long now = System.currentTimeMillis();

        List<Book.JobRecord> records = book.snapshotRecords();
        for (Book.JobRecord rec : records) {
            long elapsed = now - rec.startTime;
            if (elapsed > maxJobTime) {
                book.workerDone(rec.workerId);
                timeoutHandler.onTimeout(rec);
            }
        }
    }

    public void shutdown() {
        cronTimer.shutdown();
    }
}
