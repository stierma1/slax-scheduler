package com.github.stierma1.job;

import com.github.stierma1.SlaxScheduler;

import java.util.Map;
import java.util.function.BiConsumer;

public class HardJob extends AbstractJob {

    public HardJob(String parentName, long spawnTime, long deadline, long estimatedExecutionTime, Map<String, Object> params, BiConsumer<Map<String, Object>, SlaxScheduler> delegate, SlaxScheduler scheduler){
        super(parentName, spawnTime,  deadline,  estimatedExecutionTime, params, delegate, scheduler);
    }

    @Override
    public boolean hasHardDeadline() {
        return true;
    }

    public StringBuilder toJson(){
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"class:\":").append("\"HardJob\",");
        sb.append("\"parentName:\"").append(this.parentName).append(",");
        sb.append("\"spawnTime:\"").append(this.spawnTime).append(",");
        sb.append("\"deadline:\"").append(this.deadline).append(",");
        sb.append("\"estimatedExecutionTime:\"").append(this.estimatedExecutionTime);
        sb.append("}");

        return sb;
    }

}
