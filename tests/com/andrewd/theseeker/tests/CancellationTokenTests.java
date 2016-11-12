package com.andrewd.theseeker.tests;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by Andrew D on 11/12/2016.
 */
public class CancellationTokenTests {

    @Test
    public void ThreadMustFinishUninterrupted() throws InterruptedException {
        CancellationToken token = new CancellationToken();
        AtomicBoolean ranToFinish = new AtomicBoolean();
        AtomicBoolean compliedWithCancellation = new AtomicBoolean();

        LaunchThreadAndWaitForItToFinish(token, ranToFinish, compliedWithCancellation,
                null);

        Assert.assertTrue(ranToFinish.get());
    }

    @Test
    public void ThreadMustComplyWithCancellationAndNotFinishWork() throws InterruptedException {
        CancellationToken token = new CancellationToken();
        AtomicBoolean ranToFinish = new AtomicBoolean();
        AtomicBoolean compliedWithCancellation = new AtomicBoolean();

        LaunchThreadAndWaitForItToFinish(token, ranToFinish, compliedWithCancellation,
                (taskThread) -> taskThread.interrupt());

        Assert.assertTrue(compliedWithCancellation.get());
    }

    private void LaunchThreadAndWaitForItToFinish(CancellationToken token, AtomicBoolean ranToFinish,
                                                  AtomicBoolean compliedWithCancellation,
                                                  Consumer<Thread> runWhenThreadIsRunning) throws InterruptedException {

        TestTask testTask = new TestTask(token,ranToFinish, compliedWithCancellation);

        Thread taskThread = new Thread(testTask);
        taskThread.start();

        // Make sure we wait until thread starts up
        while(taskThread.getState() == Thread.State.NEW) { }

        if (runWhenThreadIsRunning != null) {
            runWhenThreadIsRunning.accept(taskThread);
        }

        taskThread.join();
    }

    private class TestTask implements Runnable {

        CancellationToken token;
        AtomicBoolean ranToFinish;
        AtomicBoolean compliedWithCancellation;

        public TestTask(CancellationToken token, AtomicBoolean ranToFinish,
                        AtomicBoolean compliedWithCancellation) {
            this.token = token;
            this.ranToFinish = ranToFinish;
            this.compliedWithCancellation = compliedWithCancellation;
        }

        @Override
        public void run() {
            for(int i = 0; i <= 90000000; i++) {
                if (token.isCancellationRequested()) {
                    compliedWithCancellation.set(true);
                    return;
                }
            }
            ranToFinish.set(true);
        }
    }

    private class CancellationToken {
        public boolean isCancellationRequested() {
            return Thread.currentThread().isInterrupted();
        }
    }
}