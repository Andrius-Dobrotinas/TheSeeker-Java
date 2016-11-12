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
    private boolean running;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> task;

    public SearchController(SearchEngine searchEngine, SearchResultsConsumer consumer) {
        this.searchEngine = searchEngine;
        this.consumer = consumer;
    }

    public void searchAsync(String location, String pattern, CancellationToken cancellationToken) {
        running = true;
        task = executorService.submit(() -> {
            // TODO: wrap in try/finally?
            searchEngine.search(location, pattern, cancellationToken);
            running = false;
        });
    }

    /*public boolean isRunning() {
        return running;
    }

    public void stop() {
        if (!(task.isCancelled() || task.isDone())) {
            task.cancel(true);
        }
    }*/
}