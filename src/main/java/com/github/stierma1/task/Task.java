package com.github.stierma1.task;

import com.github.stierma1.job.Job;
import com.github.stierma1.SlaxScheduler;

import java.util.Map;
import java.util.function.BiConsumer;

public interface Task {
    BiConsumer<Map<String, Object>, SlaxScheduler> getDelegate();
    Job spawnJob(String thisTasksName, long startTime, long deadline, Map<String, Object> params, SlaxScheduler scheduler);
    double getUtilization();
    long getEstimatedExecutionTime();
    long getEstimatedExecutionTime(Map<String, Object> params);
}
