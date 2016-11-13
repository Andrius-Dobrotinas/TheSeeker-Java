package com.andrewd.theseeker.tests;

import com.andrewd.theseeker.async.CancellationToken;

/**
 * Search Engine fake that runs an iteration loop to simulate work and checks cancellation token for cancellation requests
 */
class CancellableSearchEngineFake extends SearchEngineFakeBase {
    private Runnable onCancel;
    private int numberOfCycles;

    CancellableSearchEngineFake(Runnable beforeStart, Runnable onFinish, Runnable onCancel, int numberOfCycles) {
        super(beforeStart, onFinish);
        this.onCancel = onCancel;
        this.numberOfCycles = numberOfCycles;
    }

    @Override
    protected void simulateWork(CancellationToken cancellationToken) {
        for (int i = 0; i < numberOfCycles; i++) {
            // Simulate some work
            System.out.println("Task: working");
            for(int j = 0; j < 20000; j++) { }

            if (cancellationToken.isCancellationRequested()) {
                if (onCancel != null) {
                    System.out.println("Task: cancelling");
                    onCancel.run();
                }
                break;
            }
        }
    }
}