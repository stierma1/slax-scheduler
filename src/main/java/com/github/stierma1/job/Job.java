package com.github.stierma1.job;

import com.github.stierma1.SlaxScheduler;

import java.util.Map;
import java.util.function.BiConsumer;

public interface Job {
    String getId();
    String getParentTaskName();
    long getDeadLine();
    long getEstimateExecutionTime();
    long computeSlack(long currentTime);
    void execute();
    BiConsumer<Map<String, Object>, SlaxScheduler> getDelegate();
    SlaxScheduler getScheduler();
    boolean hasHardDeadline();
    void setExecutionStartTime(long time);
    long getExecutionStartTime();
    long getMaximumExecutionStartTime();
    long getSpawnTime();
    long getEstimatedRemainingExcecutionTime(long currentTime);
    Map<String, Object> getParameters();
}
