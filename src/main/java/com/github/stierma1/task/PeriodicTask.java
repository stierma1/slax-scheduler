package com.github.stierma1.task;

import com.github.stierma1.SlaxScheduler;
import com.github.stierma1.job.HardJob;
import com.github.stierma1.job.Job;

import java.util.Map;
import java.util.function.BiConsumer;

public class PeriodicTask extends AbstractTask {
    protected long releaseTime;
    protected long period;
    protected Map<String, Object> params;

    public PeriodicTask(long releaseTime, long period, long estimatedExecutionTime, BiConsumer<Map<String, Object>, SlaxScheduler> delegate){
        super(estimatedExecutionTime, delegate);
        this.params = null;
    }

    public Task clone() {
        return null;
    }

    public Job spawnJob(String thisTasksName, long spawnTime, long deadline, Map<String, Object> params, SlaxScheduler scheduler) {
        return new HardJob(thisTasksName, spawnTime, deadline,  this.getEstimatedExecutionTime(params), this.params, delegate, scheduler);
    }

    public double getUtilization() {
        return 0;
    }

    public BiConsumer<Map<String, Object>, SlaxScheduler> getDelegate() {
        return (Map<String, Object> params, SlaxScheduler scheduler) -> {
            return ;
        };
    }

    public long getReleaseTime() {
        return releaseTime;
    }

    public long getPeriod(){
        return period;
    }

    public void setParams(Map<String, Object> params){
        this.params = params;
    }
}
