package com.github.stierma1;

import com.github.stierma1.processor.Processor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.github.stierma1.task.AperiodicTask;

import java.util.HashMap;

class SlaxSchedulerImplTest {

    SlaxScheduler emptyScheduler;
    Processor mockProc;
    AperiodicTask mockAperiodicTask;
    AperiodicTask helloTask;

    @BeforeEach
    void setUp() {
        mockProc = Mockito.mock(Processor.class);
        Mockito.when(mockProc.isAvailable()).thenReturn(true);
        mockAperiodicTask = Mockito.mock(AperiodicTask.class);
        emptyScheduler = new SlaxSchedulerImpl();
        //helloTask = new AperiodicTask();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addProcessor() {
        emptyScheduler.addProcessor(mockProc);
        assert emptyScheduler.getFreeProcessor() == mockProc;
    }

    @Test
    void addTask() {
        emptyScheduler.addTask("test-task", mockAperiodicTask);
    }

    @Test
    void removeTask() {
        emptyScheduler.addTask("test-task", mockAperiodicTask);
        assert emptyScheduler.removeTask("test-task");
    }

    @Test
    void releaseJob() {
    }

    @Test
    void removeJob() {
    }

    @Test
    void removeJob1() {
    }

    @Test
    void getLeastSlackJob() {
    }

    @Test
    void getFreeProcessor() {
        emptyScheduler.addProcessor(mockProc);
        assert emptyScheduler.getFreeProcessor() == mockProc;
    }

    @Test
    void hasFreeProcessor() {
        emptyScheduler.addProcessor(mockProc);
        assert emptyScheduler.hasFreeProcessor();
    }

    @Test
    void executeLeastSlackJob() {
    }

    @Test
    void jobCompleted() {
    }

    @Test
    void restartJob() {
    }

    @Test
    void getJobs() {
        assert emptyScheduler.getJobs().size() == 0;
    }

    @Test
    void getCurrentTime() {
        assert System.currentTimeMillis() - emptyScheduler.getCurrentTime() > -3;
    }

    @Test
    void predictSlackAvailable() {
        assert emptyScheduler.predictSlackAvailable(emptyScheduler.getCurrentTime(), 100000L) == 0;
    }

    @Test
    void addReporter() {
        emptyScheduler.addReporter((eventName, params) -> {
            assert eventName.equals("test");
            assert params != null;
        });
        emptyScheduler.report("test", new HashMap<String, Object>());
    }

}