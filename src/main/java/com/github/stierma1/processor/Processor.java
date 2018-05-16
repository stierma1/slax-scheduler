package com.github.stierma1.processor;

import com.github.stierma1.job.Job;

import java.util.function.Consumer;

public interface Processor {
    String getId();
    boolean isAvailable();
    Job getCurrentJob();
    void execute(Job job, Consumer<Job> completionCallback);
}
