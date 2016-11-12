package com.andrewd.theseeker.async;

/**
 * Created by qwe on 11/12/2016.
 */
public class ThreadInterruptionChecker implements CancellationToken {
    public boolean isCancellationRequested() {
        return Thread.currentThread().isInterrupted();
    }
}