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

    /*@Test
    public void MustPerformSearchOnASeparateThread1() {
        SearchEngine searchEngine = new SearchEngineTestMock();
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("", "");

        // Verify
        Assert.assertTrue(controller.isRunning());

        // Run
        controller.stop();

        // Verify
        Assert.assertFalse(controller.isRunning());
    }*/

    /*@Test
    public void MustPerformSearchOnASeparateThread() {
        SearchEngine searchEngine = new SearchEngineTestMock();
        SearchResultsConsumer resultsConsumerMock = Mockito.mock(SearchResultsConsumer.class);

        SearchController controller = new SearchController(searchEngine, resultsConsumerMock);

        // Run
        controller.searchAsync("", "");

        // Verify
    }*/

    private class SearchEngineTestMock extends SearchEngineBase {

        @Override
        protected void performSearch(String location, String pattern, CancellationToken cancellationToken) {
            // Keep the task busy, simulate some work
            String temp = "empty";
            for(int i = 0; i < 1000000; i++) {
                temp += temp;
            }
        }
    }
}