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
    public void IsRunningMustReturnFalseInitially() {
        SearchEngine searchEngine = Mockito.mock(SearchEngine.class);
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Verify
        Assert.assertFalse("Controller reported that a search is running", controller.isRunning());
    }

    @Test
    public void IsRunningMustReturnTrueAfterSearchStarts() throws InterruptedException {
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);

        AtomicBoolean started = new AtomicBoolean();
        SearchEngine searchEngine = new SleepingSearchEngineFake(1000, () -> started.set(true), null);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        // Verify that isRunning returns the right value when we know that a search has started for sure
        while(true) {
            if (started.get() == true) {
                Assert.assertTrue("Controller didn't report that the search running", controller.isRunning());
                break;
            }
        }
    }

    @Test
    public void IsRunningMustReturnFalseAfterSearchFinishes() throws InterruptedException {
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);

        AtomicBoolean finished = new AtomicBoolean();
        SearchEngine searchEngine = new SleepingSearchEngineFake(1000, null, () -> finished.set(true));

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        // Verify that isRunning returns the right value when we know that a search has finished and waited
        // for about 2 seconds to make sure the thread has exited
        while(true) {
            if (finished.get() == true) {
                // Give the task some time to shut down after having invoked the finish callback (because it is invoked on the task thread)
                Thread.sleep(2000);

                Assert.assertFalse("Controller didn't report that the search is no longer running", controller.isRunning());
                break;
            }
        }
    }

    @Test
    public void Stop_MustCancelSearch() {
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = new ThreadInterruptionChecker();

        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();
        SearchEngine searchEngine = new CancellableSearchEngineFake(() -> started.set(true),
                () -> finished.set(true), () -> cancelled.set(true), 90000000, false); // last argument is irrelavant in this case

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

        Assert.assertTrue("Search task hasn't responded to cancellation request", cancelled.get());
    }


    @Test
    public void Stop_MustBlockUntilSearchFinishes() {
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = new ThreadInterruptionChecker();

        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();
        SearchEngine searchEngine = new CancellableSearchEngineFake(() -> started.set(true),
                () -> finished.set(true), () -> cancelled.set(true), 90000000, true);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        // Wait until the task confirms it has started
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        // Allow the task to do some work before cancelling it
        for(int i = 0; i < 100000; i++) { }

        // Send cancellation request
        controller.stop(true);

        Assert.assertTrue("Controller didn't block until search finishes", finished.get());
    }

    /**
     * This test might fail sometimes because it attempts to check if the last value of isRunning before the task
     * reports it has finished work is right ("true"). In very rare instances a race condition occurs and the test fails.
     * If this proves to be a problem in the future, I could just check for a value of isRunning right after Stop is
     * called because that is the problem I am trying to solve in the first place. However, making sure that isRunning
     * keeps returning "true" right until the task finishes is important from the outside point of view.
     */
    @Test
    public void OnCancellationMustReportThatItIsStillRunningUntilSearchTaskActuallyReturns() {
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);
        CancellationToken cancellationToken = new ThreadInterruptionChecker();

        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        SearchEngine searchEngine = new CancellableSearchEngineFake(() -> started.set(true),
                () -> finished.set(true), null, 90000, true);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("location", "pattern", cancellationToken);

        // Wait until the task confirms it has started before cancelling it because the Executor service might not run
        // the task if it is in a cancelled state by the time it gets to run it
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        controller.stop();

        // Poll isRunning until the task reports that it has finished. Then we will have captured the last value of isRunning
        // before task finishes.
        boolean running = false;
        while (finished.get() == false) {
            running = controller.isRunning();
        }
        System.out.println("The engine reported that it has finished");

        // Check the value of "running" that was captured right before the task reported that it has finished
        Assert.assertTrue("Controller reported that it is not running although the task hasn't finished yet", running);
    }
}