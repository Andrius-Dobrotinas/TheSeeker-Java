package com.andrewd.theseeker;

import com.andrewd.theseeker.async.ThreadInterruptionChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Created by Andrew D on 11/11/2016.
 */
public class Searcher implements AsyncSearcher {
    private final List<Runnable> finishEventListeners = new ArrayList<>();
    private final List<Consumer<Exception>> searchExceptionListeners = new ArrayList<>();
    private final SearchEngine<?,?> searchEngine;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // TODO: see if I should DI it
    private Future<?> task;

    // Is used to determine if the search (which is running on a separate task) is actually done regardless of the
    // tasks' isDone() value which is skewed when the task is cancelled
    private volatile boolean searchIsRunning;

    public Searcher(SearchEngine<?,?> searchEngine) {
        if (searchEngine == null){
            throw new IllegalArgumentException("searchEngine");
        }
        this.searchEngine = searchEngine;
    }

    public boolean searchAsync(String location, String pattern) {
        if (isRunning()) return false;

        task = executorService.submit(() -> {
            searchIsRunning = true;
            try {
                // TODO: see if I should make Token DI'able via a factory
                searchEngine.search(location, pattern, new ThreadInterruptionChecker());
                onFinish();
            }
            catch (Exception e) {
                onSearchException(e);
            }
            try {
                onFinish();
            }
            catch (Exception e) {
                onSearchException(e);
            }
            searchIsRunning = false;
        });
        return true;
    }

    public boolean isRunning() {
        return !(task == null ||
                (task.isDone() && searchIsRunning == false));
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
        if (finishHandler == null){
            throw new IllegalArgumentException("finishHandler");
        }
        finishEventListeners.add(finishHandler);
    }

    private void onFinish() {
        finishEventListeners.forEach(Runnable::run);
    }

    public void addSearchExceptionListener(Consumer<Exception> exceptionHandler) {
        if (exceptionHandler == null){
            throw new IllegalArgumentException("exceptionHandler");
        }
        searchExceptionListeners.add(exceptionHandler);
    }

    private void onSearchException(Exception e) {
        for(Consumer<Exception> handler : searchExceptionListeners) {
            handler.accept(e);
        }
    }
}