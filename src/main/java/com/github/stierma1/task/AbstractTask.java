package com.github.stierma1.task;

import com.github.stierma1.IToJson;
import com.github.stierma1.SlaxScheduler;

import java.io.Serializable;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractTask implements Task, IToJson{
    protected long estimatedExecutionTime;
    protected BiConsumer<Map<String, Object>, SlaxScheduler> delegate;

    public AbstractTask(long estimatedExecutionTime, BiConsumer<Map<String, Object>, SlaxScheduler> delegate){
        this.estimatedExecutionTime = estimatedExecutionTime;
        this.delegate = delegate;
    }

    public long getEstimatedExecutionTime() {
        return estimatedExecutionTime;
    }

    public long getEstimatedExecutionTime(Map<String, Object> params) {
        return this.getEstimatedExecutionTime();
    }

    @Override
    public BiConsumer<Map<String, Object>, SlaxScheduler> getDelegate() {
        return delegate;
    }

}
