package com.andrewd.theseeker.tests;

import com.andrewd.theseeker.SearchEngine;
import com.andrewd.theseeker.SearchEngineBase;
import com.andrewd.theseeker.SearchResultsConsumer;
import com.andrewd.theseeker.SearchController;
import com.andrewd.theseeker.async.CancellationToken;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Andrew D. on 11/11/2016.
 */
public class SearchControllerTests {

    //TODO: maybe rename to AsyncSearchController?

    @Test
    public void MustCallSearchEngine_SearchMethodWithSuppliedArguments() {
        SearchEngine searchEngine = Mockito.mock(SearchEngine.class);
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        // Verify
        Mockito.verify(searchEngine, Mockito.times(1))
                .search(Matchers.eq("location"), Matchers.eq("pattern"), Matchers.eq(cancellationToken));
    }

    @Test
    public void MustIndicateThatSearchIsNotRunningInitially() {
        SearchEngine searchEngine = Mockito.mock(SearchEngine.class);
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Verify
        Assert.assertFalse("Controller reported search is running", controller.isRunning());
    }

    @Test
    public void StatusMustChangeToRunningOnSearchStart() throws InterruptedException {
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);

        AtomicBoolean started = new AtomicBoolean();
        SearchEngine searchEngine = new SearchEngineTestMock(1000, () -> started.set(true), null);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        // Verify that isRunning returns the right value when we know for sure that search has started
        while(true) {
            if (started.get() == true) {
                Assert.assertTrue("Controller didn't report the search running", controller.isRunning());
                break;
            }
        }
    }

    
    @Test
    public void StatusMustChangeOnBackToNotRunningWhenSearchFinishes() throws InterruptedException {
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);

        AtomicBoolean finished = new AtomicBoolean();
        SearchEngine searchEngine = new SearchEngineTestMock(1000, null, () -> finished.set(true));

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        /* Verify that isRunning returns the right value when we know that search has finished and waited
        for about 1s to make sure the thread has exited
         */
        while(true) {
            if (finished.get() == true) {
                Thread.sleep(2000);
                Assert.assertFalse("Controller didn't report the search no longer running", controller.isRunning());
                break;
            }
        }
    }

    private class SearchEngineTestMock extends SearchEngineBase {
        private int sleepFor;
        private Runnable beforeStart;
        private Runnable onFinish;

        public SearchEngineTestMock(int sleepFor) {
            this.sleepFor = sleepFor;
        }

        public SearchEngineTestMock(int sleepFor, Runnable beforeStart, Runnable onFinish) {
            this(sleepFor);
            this.beforeStart = beforeStart;
            this.onFinish = onFinish;
        }

        @Override
        protected void performSearch(String location, String pattern, CancellationToken cancellationToken) {
            if (beforeStart != null) {
                beforeStart.run();
            }
            // Keep the task busy, simulate some work
            try {
                Thread.sleep(sleepFor);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (onFinish != null) {
                onFinish.run();
            }
        }
    }
}