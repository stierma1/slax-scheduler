package com.github.stierma1;

import com.github.stierma1.job.Job;
import com.github.stierma1.processor.Processor;
import com.github.stierma1.task.Task;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.BiConsumer;

public interface SlaxScheduler {
    void addProcessor(Processor processor);
    void addTask(String taskName, Task task);
    boolean removeTask(String taskName);
    Job releaseJob(String taskName, Map<String, Object> params, long deadline);
    Job getLeastSlackJob();
    Processor getFreeProcessor();
    boolean hasFreeProcessor();
    void executeLeastSlackJob();
    Map<String, Job> getJobs();
    long getCurrentTime();
    long predictSlackAvailable(long currentTime, long deadline);
    ScheduledExecutorService getPeriodicExecutor();
    void addReporter(BiConsumer<String, Map<String, Object>> reporter);
    void report(String eventName, Map<String, Object> report);
}
