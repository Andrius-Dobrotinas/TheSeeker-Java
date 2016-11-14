package com.andrewd.theseeker;

import com.andrewd.theseeker.async.CancellationToken;

/**
 * Created by Andrew D on 11/13/2016.
 */
public interface AsyncSearcher {
    boolean searchAsync(String location, String pattern);
    boolean isRunning();
    void stop();
    void stop(boolean blockUntilDone);
    void addFinishEventListener(Runnable finishHandler);
}
