package com.github.stierma1;

import com.github.stierma1.job.Job;
import com.github.stierma1.processor.Processor;
import com.github.stierma1.task.PeriodicTask;
import com.github.stierma1.task.Task;
import com.github.stierma1.job.HardJob;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class SlaxSchedulerImpl implements SlaxScheduler {
    protected Map<String, Task> tasks;
    protected SortedMap<Long, List<Job>> softJobs;
    protected SortedMap<Long, List<Job>> hardJobs;
    protected List<Processor> processors;
    protected ScheduledExecutorService periodicExecutor;
    protected Map<String, PeriodicRunnable> periodicRunnables;
    protected List<BiConsumer<String, Map<String, Object>>> reporters;

    public SlaxSchedulerImpl(){
        this.tasks = new HashMap<>();
        this.softJobs = new TreeMap<>();
        this.hardJobs = new TreeMap<>();
        this.processors = new ArrayList<>();
        this.periodicExecutor = Executors.newSingleThreadScheduledExecutor();
        this.periodicRunnables = new HashMap<>();
        this.reporters = new ArrayList<>();
    }

    @Override
    public void finalize(){
        this.periodicExecutor.shutdown();
    }

    public void addProcessor(Processor processor){
        this.processors.add(processor);
    }

    public void addTask(String taskName, Task task) {
        if(this.tasks.getOrDefault(taskName, null) == null){
            this.tasks.put(taskName, task);

            Map reportParams = new LinkedHashMap<String, Object>();
            reportParams.put("name", taskName);
            reportParams.put("task", task);
            this.report("task-added", Collections.unmodifiableMap(reportParams));

            if(task instanceof PeriodicTask){
                this.periodicRunnables.put(taskName, new PeriodicRunnable(this, taskName, (PeriodicTask) task));
            }

        } else{
            throw new RuntimeException("Task with name: " + taskName + " already exists");
        }
    }

    public boolean removeTask(String taskName) {
        if(this.tasks.getOrDefault(taskName, null) == null){
            return false;
        }
        this.tasks.remove(taskName);
        if(this.periodicRunnables.getOrDefault(taskName, null) != null){
            this.periodicRunnables.get(taskName).cancel();
            this.periodicRunnables.remove(taskName);
        }
        return true;
    }

    //This is only if you want to force an injection into the scheduler
    public void forceScheduleJob(Job job){
        Map<Long, List<Job>> jobsList;
        if(job instanceof HardJob){
            jobsList = this.hardJobs;
        } else {
            jobsList = this.softJobs;
        }

        if(jobsList.getOrDefault(job.getMaximumExecutionStartTime(), null) == null){
            jobsList.put(job.getMaximumExecutionStartTime(), new ArrayList<>());
        }

        jobsList.get(job.getMaximumExecutionStartTime()).add(job);
    }

    public Job releaseJob(String taskName, Map<String, Object> params, long deadline) {
        Task task = this.tasks.getOrDefault(taskName, null);
        if(task == null || task instanceof PeriodicTask){
            Map reportParams = new LinkedHashMap<String, Object>();
            reportParams.put("jobId", null);
            reportParams.put("job", null);
            reportParams.put("reason", "No task with taskName: " + taskName + " found");
            this.report("job-rejected", Collections.unmodifiableMap(reportParams));
            return null;
        }

        long currentTime = this.getCurrentTime();
        Job job = task.spawnJob(taskName, currentTime, deadline, params, this);

        //If job is soft then we can admit the job but it may not make its deadline
        if(!job.hasHardDeadline()){
            if(this.softJobs.getOrDefault(job.getMaximumExecutionStartTime(), null) == null){
                this.softJobs.put(job.getMaximumExecutionStartTime(), new ArrayList<Job>());
            }
            this.softJobs.get(job.getMaximumExecutionStartTime()).add(job);

            Map reportParams = new LinkedHashMap<String, Object>();
            reportParams.put("name", taskName);
            reportParams.put("task", task);
            reportParams.put("jobId", job.getId());
            reportParams.put("job", job);
            this.report("job-released", Collections.unmodifiableMap(reportParams));

            this.executeLeastSlackJob();
            return job;
        }

        long slack = job.computeSlack(currentTime);

        // If slack is already negative the job cannot be admitted
        if(slack < 0){
            Map reportParams = new LinkedHashMap<String, Object>();
            reportParams.put("jobId", job.getId());
            reportParams.put("job", job);
            reportParams.put("reason", "negative slack value");
            this.report("job-rejected", Collections.unmodifiableMap(reportParams));
            return null;
        }

        long predictedSlackAvailable = this.predictSlackAvailable(currentTime, deadline);

        if(predictedSlackAvailable >= slack){
            if(this.hardJobs.getOrDefault(job.getMaximumExecutionStartTime(), null) == null){
                this.hardJobs.put(job.getMaximumExecutionStartTime(), new ArrayList<Job>());
            }
            this.hardJobs.get(job.getMaximumExecutionStartTime()).add(job);
            Map reportParams = new LinkedHashMap<String, Object>();
            reportParams.put("name", taskName);
            reportParams.put("task", task);
            reportParams.put("jobId", job.getId());
            reportParams.put("job", job);
            this.report("job-released", Collections.unmodifiableMap(reportParams));
            this.executeLeastSlackJob();
            return job;
        }

        Map reportParams = new LinkedHashMap<String, Object>();
        reportParams.put("jobId", job.getId());
        reportParams.put("job", job);
        reportParams.put("reason", "Not enough predictated slack available: Predictated=" + predictedSlackAvailable + ", Slack=" + slack);
        this.report("job-rejected", Collections.unmodifiableMap(reportParams));

        return null;
    }

    public void removeJob(long maximumExecutionStartTime, String jobId){
        if(this.hardJobs.getOrDefault(maximumExecutionStartTime, null) != null){
            List<Job> jobs = this.hardJobs.get(maximumExecutionStartTime);
            for(int i = 0; i < jobs.size(); i++){
                Job job = jobs.get(0);
                if(job.getId().equals(jobId)){
                    jobs.remove(i);
                    if(jobs.size() == 0){
                        this.hardJobs.remove(maximumExecutionStartTime);
                        return;
                    }
                }
            }

        }
        if(this.softJobs.getOrDefault(maximumExecutionStartTime, null) != null){
            List<Job> jobs = this.softJobs.get(maximumExecutionStartTime);
            for(int i = 0; i < jobs.size(); i++){
                Job job = jobs.get(0);
                if(job.getId().equals(jobId)){
                    jobs.remove(i);
                    if(jobs.size() == 0){
                        this.softJobs.remove(maximumExecutionStartTime);
                        return;
                    }
                }
            }
        }
    }

    public void removeJob(Job job){
        this.removeJob(job.getMaximumExecutionStartTime(), job.getId());
    }

    public Job getLeastSlackJob() {
        for(Map.Entry<Long, List<Job>> jobsEntry : this.hardJobs.entrySet()){
            for(Job job : jobsEntry.getValue()){
                if(job.getExecutionStartTime() < 0){
                    return job;
                }
            }

        }
        for(Map.Entry<Long, List<Job>> jobsEntry : this.softJobs.entrySet()){
            for(Job job : jobsEntry.getValue()){
                if(job.getExecutionStartTime() < 0){
                    return job;
                }
            }
        }

        return null;
    }

    public Processor getFreeProcessor() {
        for(Processor processor : this.processors){
            if(processor.isAvailable()){
                return processor;
            }
        }
        return null;
    }

    public boolean hasFreeProcessor() {
        return this.getFreeProcessor() != null;
    }


    public void executeLeastSlackJob() {
        if(this.processors.isEmpty()){
            throw new RuntimeException("No processors have been added to the scheduler");
        }
        Processor processorCandidate = this.getFreeProcessor();
        if(processorCandidate == null){
            return;
        }
        Job jobCandidate = this.getLeastSlackJob();
        if(jobCandidate == null){
           return;
        }

        jobCandidate.setExecutionStartTime(this.getCurrentTime());
        try{
            Map reportParams = new LinkedHashMap<String, Object>();
            reportParams.put("jobId", jobCandidate.getId());
            reportParams.put("job", jobCandidate);
            this.report("executing-job", Collections.unmodifiableMap(reportParams));
            processorCandidate.execute(jobCandidate, (Job job) -> {
                this.jobCompleted(job);
            });
        } catch (Exception e){
            this.removeJob(jobCandidate);
            throw e;
        }
    }

    public void jobCompleted(Job job){
        this.removeJob(job);
        Map reportParams = new LinkedHashMap<String, Object>();
        reportParams.put("jobId", job.getId());
        reportParams.put("job", job);
        this.report("job-complete", Collections.unmodifiableMap(reportParams));
    }

    public void restartJob(Job job){
        Job newJob = this.releaseJob(job.getParentTaskName(), job.getParameters(), job.getDeadLine());
        if(newJob == null){
            Map reportParams = new LinkedHashMap<String, Object>();
            reportParams.put("jobId", job.getId());
            reportParams.put("job", job);
            this.report("job-restart-failed", Collections.unmodifiableMap(reportParams));
        } else {
            Map reportParams = new LinkedHashMap<String, Object>();
            reportParams.put("jobId", job.getId());
            reportParams.put("job", job);
            reportParams.put("newJobId", newJob.getId());
            reportParams.put("newJob", newJob);
            this.report("job-restarted", Collections.unmodifiableMap(reportParams));
        }

    }

    public Map<String, Job> getJobs() {
        Map<String, Job> jobs = new HashMap<>();
        for(Map.Entry<Long, List<Job>> jobsEntry : this.hardJobs.entrySet()){
            for(Job job : jobsEntry.getValue()){
                jobs.put(job.getId(), job);
            }

        }
        for(Map.Entry<Long, List<Job>> jobsEntry : this.softJobs.entrySet()){
            for(Job job : jobsEntry.getValue()){
                jobs.put(job.getId(), job);
            }
        }

        return jobs;
    }

    public long getEstimatedExecutionTime(String taskName, Map<String, Object> params){
        if(this.tasks.getOrDefault(taskName, null) == null){
            throw new RuntimeException("Task with name: " + taskName + " not found");
        }

        return this.tasks.get(taskName).getEstimatedExecutionTime(params);
    }

    public long getCurrentTime(){
        return System.currentTimeMillis();
    }

    public long predictSlackAvailable(long currentTime, long deadline){
        long futureUsage = 0;
        for(Map.Entry<Long, List<Job>> jobsEntry : this.hardJobs.entrySet()){
            List<Job> jobs = jobsEntry.getValue();
            Job job = jobs.get(0);
            if(job.getDeadLine() > currentTime && job.getDeadLine() - job.getEstimateExecutionTime() <= deadline){
                futureUsage += job.getEstimateExecutionTime();
            }
        }

        return this.processors.size() * (deadline - currentTime) - futureUsage;
    }

    public ScheduledExecutorService getPeriodicExecutor(){
        return this.periodicExecutor;
    }

    public void addReporter(BiConsumer<String, Map<String, Object>> reporter){
        this.reporters.add(reporter);
    }

    public void report(String eventName, Map<String, Object> eventParams){
        for(BiConsumer<String, Map<String, Object>> reporter : this.reporters){
            reporter.accept(eventName, eventParams);
        }
    }

    public class PeriodicRunnable implements Runnable{
        PeriodicTask task;
        SlaxScheduler scheduler;
        String taskName;
        ScheduledFuture future;
        PeriodicRunnable(SlaxScheduler scheduler, String taskName, PeriodicTask task ){
            this.taskName = taskName;
            this.task = task;
            this.scheduler = scheduler;
            long delay = task.getReleaseTime() - scheduler.getCurrentTime() > 0 ? task.getReleaseTime() - scheduler.getCurrentTime() : 0;
            this.future = scheduler.getPeriodicExecutor().scheduleAtFixedRate(this, delay, task.getPeriod(), TimeUnit.MILLISECONDS);

        }

        public boolean isAssociatedExecutor(Task task){
            return this.task == task;
        }

        @Override
        public void run() {
            scheduler.releaseJob(taskName, null, scheduler.getCurrentTime() + task.getPeriod());
        }

        public void cancel(){
            future.cancel(false);
        }
    }
}
