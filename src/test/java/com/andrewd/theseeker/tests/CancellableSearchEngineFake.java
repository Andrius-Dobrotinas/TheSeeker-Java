package com.andrewd.theseeker.tests;

import com.andrewd.theseeker.async.CancellationToken;

/**
 * Search Engine fake that runs an iteration loop to simulate work and checks cancellation token for cancellation requests
 */
class CancellableSearchEngineFake extends SearchEngineFakeBase {
    private Runnable onCancel;
    private int numberOfCycles;
    private boolean simulateWorkBeforeCheckingCancellation;

    CancellableSearchEngineFake(Runnable beforeStart, Runnable onFinish, Runnable onCancel, int numberOfCycles,
                                boolean simulateWorkBeforeCheckingCancellation) {
        super(beforeStart, onFinish);
        this.onCancel = onCancel;
        this.numberOfCycles = numberOfCycles;
        this.simulateWorkBeforeCheckingCancellation = simulateWorkBeforeCheckingCancellation;
    }

    @Override
    protected void simulateWork(CancellationToken cancellationToken) {
        for (int i = 0; i < numberOfCycles; i++) {

            // Simulate some work and delay until checking cancellation token
            if (simulateWorkBeforeCheckingCancellation) {
                System.out.println("Task: working");
                StringBuilder s = new StringBuilder();
                for(int j = 0; j < 20000000; j++) {
                    s.append(j);
                }
            }

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