package com.github.stierma1.task;

import com.github.stierma1.SlaxScheduler;
import com.github.stierma1.job.Job;
import com.github.stierma1.job.SoftJob;

import java.util.Map;
import java.util.function.BiConsumer;

public class AperiodicTask extends AbstractTask {

    public AperiodicTask(long estimatedExecutionTime, BiConsumer<Map<String, Object>, SlaxScheduler> delegate){
        super(estimatedExecutionTime, delegate);
    }

    @Override
    public Task clone() {
        return null;
    }

    @Override
    public Job spawnJob(String thisTasksName, long spawnTime, long deadline, Map<String, Object> params, SlaxScheduler scheduler) {
        return new SoftJob(thisTasksName, spawnTime, deadline, this.getEstimatedExecutionTime(params), params, delegate, scheduler);
    }

    @Override
    public double getUtilization() {
        return 0;
    }
}
