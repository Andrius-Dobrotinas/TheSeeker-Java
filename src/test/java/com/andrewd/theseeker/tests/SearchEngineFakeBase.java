package com.andrewd.theseeker.tests;

import com.andrewd.theseeker.SearchEngineBase;
import com.andrewd.theseeker.async.CancellationToken;

/**
 * Search Engine fake that fires event handlers on search start (before simulation of work) and on search finish (after simulation of work)
 */
abstract class SearchEngineFakeBase extends SearchEngineBase<Object, Object> {
    private Runnable beforeStart;
    private Runnable onFinish;

    SearchEngineFakeBase(Runnable beforeStart, Runnable onFinish) {
        this.beforeStart = beforeStart;
        this.onFinish = onFinish;
    }

    protected abstract void simulateWork(CancellationToken cancellationToken);

    @Override
    protected void performSearch(String location, String pattern, CancellationToken cancellationToken) {
        if (beforeStart != null) {
            beforeStart.run();
        }
        // Keep the task busy, simulate some work
        simulateWork(cancellationToken);

        if (onFinish != null) {
            onFinish.run();
        }
    }
}