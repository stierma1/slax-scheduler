package com.github.stierma1.job;

import com.github.stierma1.SlaxScheduler;

import java.util.Map;
import java.util.function.BiConsumer;

public class SoftJob extends AbstractJob {

    public SoftJob(String parentName, long spawnTime, long deadline, long estimatedExecutionTime, Map<String, Object> params, BiConsumer<Map<String, Object>, SlaxScheduler> delegate, SlaxScheduler scheduler){
        super(parentName, spawnTime, deadline,  estimatedExecutionTime, params, delegate, scheduler);
    }


    @Override
    public boolean hasHardDeadline() {
        return false;
    }
}
