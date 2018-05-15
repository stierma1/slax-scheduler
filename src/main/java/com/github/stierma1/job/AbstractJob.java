package com.github.stierma1.job;

import com.github.stierma1.SlaxScheduler;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public abstract class AbstractJob implements Job, Comparable<Job> {

    protected String id = UUID.randomUUID().toString();
    protected String parentName;
    protected long deadline;
    protected long estimatedExecutionTime;
    protected Map<String, Object> params;
    protected BiConsumer<Map<String, Object>, SlaxScheduler> delegate;
    protected SlaxScheduler scheduler;
    protected long executionStartTime;
    protected long spawnTime;

    AbstractJob(String parentName, long spawnTime, long deadline, long estimatedExecutionTime, Map<String, Object> params, BiConsumer<Map<String, Object>, SlaxScheduler> delegate, SlaxScheduler scheduler){
        this.parentName = parentName;
        this.deadline = deadline;
        this.estimatedExecutionTime = estimatedExecutionTime;
        this.params = params;
        this.delegate = delegate;
        this.scheduler = scheduler;
        this.executionStartTime = -1;
        this.spawnTime = spawnTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getParentTaskName() {
        return parentName;
    }

    @Override
    public long getDeadLine() {
        return deadline;
    }

    @Override
    public long getEstimateExecutionTime() {
        return estimatedExecutionTime;
    }

    @Override
    public long computeSlack(long currentTime) {
        return deadline - currentTime - estimatedExecutionTime;
    }

    @Override
    public BiConsumer<Map<String, Object>, SlaxScheduler> getDelegate(){
        return delegate;
    }

    @Override
    public SlaxScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void execute() {
        delegate.accept(params, scheduler);
    }

    @Override
    public void setExecutionStartTime(long executionStartTime) {
        this.executionStartTime = executionStartTime;
    }

    @Override
    public long getExecutionStartTime(){
        return this.executionStartTime;
    }

    @Override
    public long getMaximumExecutionStartTime(){
        return this.deadline - this.estimatedExecutionTime;
    }

    public long getSpawnTime(){
        return this.spawnTime;
    }

    public int compareTo(Job j){
        long compare = this.getMaximumExecutionStartTime() - j.getMaximumExecutionStartTime();
        if(compare > 0L){
            return 1;
        } else if(compare < 0L){
            return -1;
        }
        return 0;
    }

    public long getEstimatedRemainingExcecutionTime(long currentTime){
        if(this.executionStartTime == -1){
            return estimatedExecutionTime;
        }
        return estimatedExecutionTime - (currentTime - executionStartTime);
    }

    public Map<String, Object> getParameters(){
        return params;
    }
}
