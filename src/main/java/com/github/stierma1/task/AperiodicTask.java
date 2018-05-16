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

    public SporadicTask composeWith(SporadicTask otherTask){
        BiConsumer<Map<String, Object>, SlaxScheduler> newDelegate = this.getDelegate().andThen(otherTask.getDelegate());
        long newEstimatedExecution = otherTask.getEstimatedExecutionTime() + this.getEstimatedExecutionTime();
        return new SporadicTask(newEstimatedExecution, newDelegate);
    }

    public AperiodicTask composeWith(AperiodicTask otherTask){
        BiConsumer<Map<String, Object>, SlaxScheduler> newDelegate = this.getDelegate().andThen(otherTask.getDelegate());
        long newEstimatedExecution = otherTask.getEstimatedExecutionTime() + this.getEstimatedExecutionTime();
        return new AperiodicTask(newEstimatedExecution, newDelegate);
    }

    public PeriodicTask composeWith(PeriodicTask otherTask){
        return composeWith(otherTask, otherTask.getPeriod());
    }

    public PeriodicTask composeWith(PeriodicTask otherTask, long newPeriod){
        BiConsumer<Map<String, Object>, SlaxScheduler> newDelegate = this.getDelegate().andThen(otherTask.getDelegate());
        long newEstimatedExecution = otherTask.getEstimatedExecutionTime() + this.getEstimatedExecutionTime();

        PeriodicTask newTask = new PeriodicTask(otherTask.getReleaseTime(), newPeriod, newEstimatedExecution, newDelegate);
        newTask.setParams(otherTask.getParams());
        return newTask;
    }

    public StringBuilder toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"class:\":").append("\"AperiodicTask\",");
        sb.append("\"estimatedExecutionTime:\"").append(this.estimatedExecutionTime);
        sb.append("}");

        return sb;
    }
}
