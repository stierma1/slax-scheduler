package com.github.stierma1;


import com.github.stierma1.task.AperiodicTask;
import com.github.stierma1.task.PeriodicTask;
import com.github.stierma1.task.SporadicTask;
import com.github.stierma1.task.Task;


import java.util.Map;
import java.util.function.BiConsumer;

public class TaskDeserializer {
    public TaskDeserializer(){
        
    }

    public Task fromJson(Map<String, Object> json, BiConsumer<Map<String, Object>, SlaxScheduler> delegate){
        if(json.get("class").equals("SporadicTask")){
            return new SporadicTask((long)json.get("estimatedExecutionTime"), delegate);
        } else if(json.get("class").equals("AperiodicTask")) {
            return new AperiodicTask((long) json.get("estimatedExecutionTime"), delegate);
        } else if(json.get("class").equals("PeriodicTask")){
            return new PeriodicTask((long) json.get("releaseTime"), (long) json.get("period"), (long) json.get("estimatedExecutionTime"), delegate);
        } else{
            return extendedFromJson(json, delegate);
        }
    }

    public Task extendedFromJson(Map<String, Object> json, BiConsumer<Map<String, Object>, SlaxScheduler> delegate) {
        throw new RuntimeException("Unable to deserialize task of class: " + json.getOrDefault("class", "undefined"));
    }
}
