package com.andrewd.theseeker.tests;

import com.andrewd.theseeker.SearchEngine;
import com.andrewd.theseeker.Searcher;
import com.andrewd.theseeker.async.CancellationToken;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Andrew D. on 11/11/2016.
 */
public class SearcherTests {

    @Test
    public void MustCallSearchEngine_SearchMethodWithSuppliedArguments() throws Exception {
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);

        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.searchAsync("location", "pattern");

        // Wait until the task stops
        while(searcher.isRunning()) {}

        // Verify
        Mockito.verify(searchEngine, Mockito.times(1))
                .search(Matchers.eq("location"), Matchers.eq("pattern"), Matchers.any(CancellationToken.class));
    }

    @Test
    public void IsRunningMustReturnFalseInitially() {
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);

        Searcher searcher = new Searcher(searchEngine);

        // Verify
        Assert.assertFalse("Searcher reported that a search is running", searcher.isRunning());
    }

    @Test
    public void IsRunningMustReturnTrueAfterInitiatingSearch() throws InterruptedException {
        SearchEngine<?,?> searchEngine = new SleepingSearchEngineFake(1000, null, null);

        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.searchAsync("location", "pattern");

        // Verify
        Assert.assertTrue("Searcher didn't report that the is search running", searcher.isRunning());
    }

    @Test
    public void IsRunningMustReturnFalseAfterSearchFinishes() throws InterruptedException {
        AtomicBoolean finished = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = new SleepingSearchEngineFake(1000, null, () -> finished.set(true));

        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.searchAsync("location", "pattern");

        // Verify that isRunning returns the right value when we know that a search has finished and waited
        // for about 2 seconds to make sure the thread has exited
        while(true) {
            if (finished.get() == true) {
                // Give the task some time to shut down after having invoked the finish callback (because it is invoked on the task thread)
                Thread.sleep(2000);

                Assert.assertFalse("Searcher didn't report that the search is no longer running", searcher.isRunning());
                break;
            }
        }
    }

    @Test
    public void Stop_MustCancelSearch() {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = new CancellableSearchEngineFake(() -> started.set(true),
                () -> finished.set(true), () -> cancelled.set(true), 90000000, false); // last argument is irrelevant in this case

        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.searchAsync("location", "pattern");

        // Wait until the task confirms it has started
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        // Allow the task to do some work before cancelling it
        for(int i = 0; i < 100000; i++) { }

        // Send cancellation request
        searcher.stop();

        System.out.println("Requested Cancellation. Waiting for search to finish");
        while(finished.get() == false) { }

        Assert.assertTrue("Search task hasn't responded to cancellation request", cancelled.get());
    }


    @Test
    public void Stop_MustBlockUntilSearchFinishes() {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = new CancellableSearchEngineFake(() -> started.set(true),
                () -> finished.set(true), () -> cancelled.set(true), 90000000, true);

        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.searchAsync("location", "pattern");

        // Wait until the task confirms it has started
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        // Allow the task to do some work before cancelling it
        for(int i = 0; i < 100000; i++) { }

        // Send cancellation request
        searcher.stop(true);

        Assert.assertTrue("Searcher didn't block until search finishes", finished.get());
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
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = new CancellableSearchEngineFake(() -> started.set(true),
                () -> finished.set(true), null, 90000, true);

        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.searchAsync("location", "pattern");

        // Wait until the task confirms it has started before cancelling it because the Executor service might not run
        // the task if it is in a cancelled state by the time it gets to run it
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        searcher.stop();

        // Poll isRunning until the task reports that it has finished. Then we will have captured the last value of isRunning
        // before task finishes.
        boolean running = false;
        while (finished.get() == false) {
            running = searcher.isRunning();
        }
        System.out.println("The engine reported that it has finished");

        // Check the value of "running" that was captured right before the task reported that it has finished
        Assert.assertTrue("Searcher reported that it is not running although the task hasn't finished yet", running);
    }

    @Test
    public void MustInvokeFinishEventHandlerWhenSearchFinishes_OnCancellation() throws InterruptedException {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = new CancellableSearchEngineFake(() -> started.set(true), null, null, 2000, false);

        Searcher searcher = new Searcher(searchEngine);
        searcher.addFinishEventListener(() -> finished.set(true));

        // Run
        searcher.searchAsync("location", "pattern");

        // Wait until the task confirms it has started
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        // Cancel the task and block until it finishes
        searcher.stop(true);

        Assert.assertTrue("Finish event handler hasn't been invoked after a cancelled search", finished.get());
    }

    @Test
    public void MustInvokeFinishEventHandlerWhenSearchSuccessfullyFinishes() throws InterruptedException {
        AtomicBoolean finished = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);

        Searcher searcher = new Searcher(searchEngine);
        searcher.addFinishEventListener(() -> finished.set(true));

        // Run
        searcher.searchAsync("location", "pattern");

        // Wait until the task finishes
        while(searcher.isRunning()) { }

        Assert.assertTrue("Finish event handler hasn't been invoked after successful search", finished.get());
    }

    @Test
    public void MustInvokeFinishHandlerEvenInCaseOfSearchEngineException() throws Exception {
        AtomicBoolean finished = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);
        Searcher searcher = new Searcher(searchEngine);

        Mockito.doThrow(new Exception())
                .when(searchEngine).search(Matchers.any(), Matchers.any(), Matchers.any());

        searcher.addFinishEventListener(() -> finished.set(true));

        // Run
        searcher.searchAsync("","");

        while(searcher.isRunning()) {}

        // Verify
        Assert.assertTrue("Finish event handler hasn't been invoked on search exception!", finished.get());
    }

    @Test
    public void MustReturnTrueForNewSearchRequest() {
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);

        Searcher searcher = new Searcher(searchEngine);

        boolean searchAccepted = searcher.searchAsync("", "");

        Assert.assertTrue("Didn't return true on search accepted", searchAccepted);
    }

    @Test
    public void MustNotStartNewSearchIfAlreadySearching() {
        SearchEngine<?,?> searchEngine = new CancellableSearchEngineFake(null, null, null, 2000, false);

        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.searchAsync("","");

        boolean searchAccepted = searcher.searchAsync("","");

        // Verify
        Assert.assertFalse("Didn't return false for a subsequent request for search", searchAccepted);
    }

    @Test
    public void MustStartNewSearchIfPreviousOneIsDone() {
        SearchEngine<?,?> searchEngine = new CancellableSearchEngineFake(null, null, null, 2000, false);

        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.searchAsync("","");

        while(searcher.isRunning()) {}

        boolean searchAccepted = searcher.searchAsync("","");

        // Verify
        Assert.assertTrue("Didn't return true for a subsequent request for search even through the previous search is done", searchAccepted);
    }

    @Test
    public void MustInvokeSearchExceptionHandlerWhenSearchEngineThrowsException() throws Exception {
        String message = "My Exception";
        AtomicReference<Exception> exception = new AtomicReference<>();
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);
        Searcher searcher = new Searcher(searchEngine);
        searcher.addSearchExceptionListener(exception::set);

        Mockito.doThrow(new ArithmeticException(message))
                .when(searchEngine).search(Matchers.any(), Matchers.any(), Matchers.any());

        // Run
        searcher.searchAsync("","");

        while(searcher.isRunning()) {}

        Exception result = exception.get();

        // Verify
        Assert.assertNotEquals("Exception handler hasn't been fired", null, result);
        Assert.assertTrue("Wrong exception caught", result instanceof ArithmeticException);
        Assert.assertEquals("Wrong exception caught (wrong message)", message, result.getMessage());
    }

    @Test
    public void MustInvokeSearchExceptionHandlerWhenFinishHandlerThrowsException() throws Exception {
        AtomicReference<Exception> exception = new AtomicReference<>();
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);
        Searcher searcher = new Searcher(searchEngine);
        searcher.addSearchExceptionListener(exception::set);

        // Mockito doesn't allow throwing an exception from Runnable::run, therefore using this workaround
        searcher.addFinishEventListener(() -> { int i = 1/0;});

        // Run
        searcher.searchAsync("","");

        while(searcher.isRunning()) {}

        Exception result = exception.get();

        // Verify
        Assert.assertNotEquals("Exception handler hasn't been fired", null, result);
        Assert.assertTrue("Wrong exception caught", result instanceof ArithmeticException);
    }

    @Test
    public void IsDestroyed_MustReturnFalseInitially() {
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);
        Searcher searcher = new Searcher(searchEngine);

        // Verify
        Assert.assertFalse("Searcher is in a destroyed state!", searcher.isDestroyed());
    }

    @Test
    public void IsDestroyed_MustReturnTrueAfterDestruction() {
        SearchEngine<?,?> searchEngine = Mockito.mock(SearchEngine.class);
        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.destroy();

        // Verify
        Assert.assertTrue("Searcher is hasn't been destroyed!", searcher.isDestroyed());
    }

    @Test(expected = java.util.concurrent.RejectedExecutionException.class)
    public void MustNotAllowSearchAfterDestruction() {
        SearchEngine<?,?> searchEngine = new SleepingSearchEngineFake(1000, null, null);
        Searcher searcher = new Searcher(searchEngine);

        // Run
        searcher.destroy();
        searcher.searchAsync("","");
    }

    @Test
    public void Destroy_MustCancelCurrentTaskAndBlockUntilTaskFinishes() {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = new CancellableSearchEngineFake(() -> started.set(true), null,
                () -> cancelled.set(true), 90000000, true);

        Searcher searcher = new Searcher(searchEngine);
        searcher.addFinishEventListener(() -> finished.set(true));

        // Run
        searcher.searchAsync("location", "pattern");

        // Wait until the task confirms it has started
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        // Run
        searcher.destroy();

        Assert.assertTrue("The task hasn't processed cancellation!", cancelled.get());
        Assert.assertTrue("The task did't finish!", finished.get());
    }

    @Test
    public void Destroy_MustPutSearcherIntoDestroyedStateWhenCurrentlyRunningTaskFinishes() {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean finished = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();
        SearchEngine<?,?> searchEngine = new CancellableSearchEngineFake(() -> started.set(true), null,
                () -> cancelled.set(true), 1000000, true);

        Searcher searcher = new Searcher(searchEngine);
        searcher.addFinishEventListener(() -> finished.set(true));

        // Run
        searcher.searchAsync("location", "pattern");

        // Wait until the task confirms it has started
        while(started.get() == false) { }
        System.out.println("Search start reported by the engine");

        // Run
        searcher.destroy();

        Assert.assertTrue("Searcher hasn't been destroyed!", searcher.isDestroyed());
    }
}