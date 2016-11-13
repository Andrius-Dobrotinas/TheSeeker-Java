package com.andrewd.theseeker.tests;

import com.andrewd.theseeker.SearchEngine;
import com.andrewd.theseeker.SearchResultsConsumer;
import com.andrewd.theseeker.SearchController;
import com.andrewd.theseeker.async.CancellationToken;
import com.andrewd.theseeker.async.ThreadInterruptionChecker;
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
        SearchEngine searchEngine = new SleepingSearchEngineFake(1000, () -> started.set(true), null);

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
        SearchEngine searchEngine = new SleepingSearchEngineFake(1000, null, () -> finished.set(true));

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        /* Verify that isRunning returns the right value when we know that search has finished and waited
        for about 1s to make sure the thread has exited
         */
        while(true) {
            if (finished.get() == true) {
                // Allow for the task to shut down after having invoked the finish callback (because it is invoked on the task thread)
                Thread.sleep(2000);

                Assert.assertFalse("Controller didn't report that the is search no longer running", controller.isRunning());
                break;
            }
        }
    }

    @Test
    public void SearchMustBeCancelledOnStop() {
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = new ThreadInterruptionChecker();

        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();
        SearchEngine searchEngine = new CancellableSearchEngineFake(() -> started.set(true),
                () -> finished.set(true), () -> cancelled.set(true), 90000000);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        // Wait until the task confirms it has started
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        // Allow the task to do some work before cancelling it
        for(int i = 0; i < 100000; i++) { }

        // Send cancellation request
        controller.stop();

        System.out.println("Requested Cancellation. Waiting for search to finish");
        while(finished.get() == false) { }

        Assert.assertTrue("Search task hasn't reacted to cancellation request", cancelled.get());
    }
}