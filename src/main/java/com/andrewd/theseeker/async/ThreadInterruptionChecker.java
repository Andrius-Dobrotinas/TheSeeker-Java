package com.andrewd.theseeker.async;

/**
 * Created by Andrew D on 11/12/2016.
 */
public class ThreadInterruptionChecker implements CancellationToken {
    public boolean isCancellationRequested() {
        return Thread.currentThread().isInterrupted();
    }
}