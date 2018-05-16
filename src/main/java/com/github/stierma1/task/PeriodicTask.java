package com.github.stierma1.task;

import com.github.stierma1.SlaxScheduler;
import com.github.stierma1.job.HardJob;
import com.github.stierma1.job.Job;

import java.util.HashMap;
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

    public Map<String, Object> getParams(){
        return params;
    }

    public void setParams(Map<String, Object> params){
        this.params = params;
    }

    public PeriodicTask composeWith(SporadicTask otherTask, long newPeriod){
        BiConsumer<Map<String, Object>, SlaxScheduler> newDelegate = this.getDelegate().andThen(otherTask.getDelegate());
        long newEstimatedExecution = otherTask.getEstimatedExecutionTime() + this.getEstimatedExecutionTime();
        PeriodicTask newTask =  new PeriodicTask(this.getReleaseTime(), newPeriod, newEstimatedExecution, newDelegate);
        newTask.setParams(this.getParams());
        return newTask;
    }

    public PeriodicTask composeWith(AperiodicTask otherTask, long newPeriod){
        BiConsumer<Map<String, Object>, SlaxScheduler> newDelegate = this.getDelegate().andThen(otherTask.getDelegate());
        long newEstimatedExecution = otherTask.getEstimatedExecutionTime() + this.getEstimatedExecutionTime();
        PeriodicTask newTask = new PeriodicTask(this.getReleaseTime(), newPeriod, newEstimatedExecution, newDelegate);
        newTask.setParams(this.getParams());
        return newTask;
    }

    public PeriodicTask composeWith(PeriodicTask otherTask, long newPeriod){
        BiConsumer<Map<String, Object>, SlaxScheduler> newDelegate = this.getDelegate().andThen(otherTask.getDelegate());
        long newEstimatedExecution = otherTask.getEstimatedExecutionTime() + this.getEstimatedExecutionTime();

        PeriodicTask newTask = new PeriodicTask(otherTask.getReleaseTime(), newPeriod, newEstimatedExecution, newDelegate);
        Map<String, Object> newMap = new HashMap<String, Object>();
        newMap.putAll(this.getParams() == null ? new HashMap<>() : this.getParams());
        newMap.putAll(otherTask.getParams() == null ? new HashMap<>() : otherTask.getParams());

        newTask.setParams(newMap);
        return newTask;
    }

    public StringBuilder toJson(){
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"class:\":").append("\"PeriodicTask\",");
        sb.append("\"estimatedExecutionTime:\"").append(this.estimatedExecutionTime).append(",");
        sb.append("\"period:\"").append(this.period).append(",");
        sb.append("\"releaseTime:\"").append(this.releaseTime);
        sb.append("}");

        return sb;
    }
}
