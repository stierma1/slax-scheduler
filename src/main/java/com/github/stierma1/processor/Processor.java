package com.github.stierma1.processor;

import com.github.stierma1.job.Job;

import java.util.function.Consumer;

public interface Processor {
    boolean isAvailable();
    void execute(Job job, Consumer<Job> completionCallback);
}
