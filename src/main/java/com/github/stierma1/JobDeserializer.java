package com.github.stierma1;

import com.github.stierma1.job.HardJob;
import com.github.stierma1.job.Job;
import com.github.stierma1.job.SoftJob;

import java.util.Map;
import java.util.function.BiConsumer;

public class JobDeserializer {
    public JobDeserializer(){

    }

    public Job fromJson(Map<String, Object> json, Map<String, Object> params, BiConsumer<Map<String, Object>, SlaxScheduler> delegate, SlaxScheduler scheduler){
        if(json.get("class").equals("HardJob")){
            return new HardJob((String)json.get("parentName"), (long)json.get("spawnTime"), (long)json.get("deadline"),(long)json.get("estimatedExecutionTime"), params, delegate, scheduler);
        } else if(json.get("class").equals("SoftJob")) {
            return new SoftJob((String)json.get("parentName"), (long)json.get("spawnTime"), (long)json.get("deadline"),(long)json.get("estimatedExecutionTime"), params, delegate, scheduler);
        } else{
            return extendedFromJson(json, params, delegate, scheduler);
        }
    }

    public Job extendedFromJson(Map<String, Object> json, Map<String, Object> params, BiConsumer<Map<String, Object>, SlaxScheduler> delegate, SlaxScheduler scheduler){
        throw new RuntimeException("Unable to deserialize job of class: " + json.getOrDefault("class", "undefined"));
    }
}
