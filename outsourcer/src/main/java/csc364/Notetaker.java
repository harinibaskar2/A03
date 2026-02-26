package csc364;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Notetaker {
    public static class JobRecord {
        public final String workerId;
        public final String jobId;
        public final String jobContent; 
        public final long startTime; 

        public JobRecord(String workerId, String jobId, String jobContent, long startTime) {
            this.workerId = workerId;
            this.jobId = jobId;
            this.jobContent = jobContent;
            this.startTime = startTime;
        }

        @Override
        public String toString() {
            return "JobRecord{workerId=" + workerId + 
            ", jobId=" + jobId + 
            ", jobContent=" + jobContent + 
            ", startTime=" + startTime + "}";
        }
    }

    // hash table, jobs per worker
    private final Map<String, JobRecord> workerToJob = new ConcurrentHashMap<>();

    // this assigns a job to a worker
    public void assign(String workerId, String jobId, String jobContent) {
        long now = System.currentTimeMillis();
        workerToJob.put(workerId, new JobRecord(workerId, jobId, jobContent, now));
    }

    // worker done, need to remove from list (from my class notes we need this)
    public void workerDone(String workerId) {
        workerToJob.remove(workerId);
    }

    // get job record
    public JobRecord getRecord(String workerId) {
        return workerToJob.get(workerId);
    }

    // does worker have a job
    public boolean hasJob(String workerId) {
        return workerToJob.containsKey(workerId);
    }

    public List<JobRecord> snapshotRecords() {
        return new ArrayList<>(workerToJob.values());
    }


}
