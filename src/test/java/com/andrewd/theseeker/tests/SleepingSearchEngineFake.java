package com.andrewd.theseeker.tests;

import com.andrewd.theseeker.async.CancellationToken;

/**
 * Search Engine fake that sleeps for a specified amount of time to simulate work and can't be used for cancellation testing
 */
class SleepingSearchEngineFake extends SearchEngineFakeBase {
    private int sleepFor;

    SleepingSearchEngineFake(int sleepFor, Runnable beforeStart, Runnable onFinish) {
        super(beforeStart, onFinish);
        this.sleepFor = sleepFor;
    }

    @Override
    protected void simulateWork(CancellationToken cancellationToken) {
        try {
            Thread.sleep(sleepFor);
        } catch (InterruptedException e) {
            System.out.println("Task shouldn't have been interrupted");
            e.printStackTrace();
        }
    }
}