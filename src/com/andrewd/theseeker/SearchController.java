package com.andrewd.theseeker;

import com.andrewd.theseeker.async.CancellationToken;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Andrew D on 11/11/2016.
 */
public class SearchController {
    private SearchEngine searchEngine;
    private SearchResultsConsumer consumer;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> task;

    // Used to determine if the search (which is running on a separate task) is actually done regardless of the
    // tasks' isDone() value which is skewed when the task is cancelled
    private volatile boolean searchIsRunning;

    public SearchController(SearchEngine searchEngine, SearchResultsConsumer consumer) {
        this.searchEngine = searchEngine;
        this.consumer = consumer;
    }

    public void searchAsync(String location, String pattern, CancellationToken cancellationToken) {
        task = executorService.submit(() -> {
            searchIsRunning = true;
            // TODO: wrap in try/finally?
            searchEngine.search(location, pattern, cancellationToken);
            searchIsRunning = false;
        });
    }

    public boolean isRunning() {
        return !(task == null || (task.isDone() && !searchIsRunning));
    }

    public void stop() {
        task.cancel(true);
    }
}