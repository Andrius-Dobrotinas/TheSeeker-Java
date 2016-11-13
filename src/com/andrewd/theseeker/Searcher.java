package com.andrewd.theseeker;

import com.andrewd.theseeker.async.ThreadInterruptionChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Andrew D on 11/11/2016.
 */
public class Searcher implements AsyncSearcher {
    private SearchEngine searchEngine;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> task;
    private List<Runnable> finishEventListeners = new ArrayList<>();

    // For some reason Future.isDone() returns true before it actually finishes so I'm not using it anymore
    private boolean searchIsRunning;

    public Searcher(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    public void searchAsync(String location, String pattern) {
        searchIsRunning = true;
        task = executorService.submit(() -> {
            // TODO: wrap in try/finally?
            searchEngine.search(location, pattern, new ThreadInterruptionChecker()); // TODO: make Token DI'able via factory?
            onFinish();
            searchIsRunning = false;
        });
    }

    public boolean isRunning() {
        return searchIsRunning;
    }

    public void stop() {
        if (task != null) {
            task.cancel(true);
        }
    }

    public void stop(boolean blockUntilDone) {
        stop();
        if (blockUntilDone) {
            while(isRunning()) { }
        }
    }

    public void addFinishEventListener(Runnable finishHandler) {
        finishEventListeners.add(finishHandler);
    }

    protected void onFinish() {
        finishEventListeners.forEach(Runnable::run);
    }
}