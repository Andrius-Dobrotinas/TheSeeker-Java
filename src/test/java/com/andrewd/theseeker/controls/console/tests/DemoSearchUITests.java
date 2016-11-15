package com.andrewd.theseeker.controls.console.tests;

import com.andrewd.theseeker.AsyncSearcher;
import com.andrewd.theseeker.SearchEngine;
import com.andrewd.theseeker.Searcher;
import com.andrewd.theseeker.controls.SearchInput;
import com.andrewd.theseeker.controls.console.DemoSearchUI;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrew D on 11/14/2016.
 */
public class DemoSearchUITests {

    @Test
    public void MustPerformSearch() throws IOException {
        List<Object> results = new ArrayList<>();
        SearchEngine searchEngine = Mockito.mock(SearchEngine.class);
        AsyncSearcher searcher = new Searcher(searchEngine);

        Mockito.doAnswer(x -> {
            for(int i = 0; i < 10; i++) {
                results.add(i);
            }
            return null;
        }).when(searchEngine).search(Matchers.any(), Matchers.any(), Matchers.any());

        InputStreamFake inStream = new InputStreamFake("c:\nasd\n" + DemoSearchUI.EXIT_COMMAND + "\n");
        PrintStream outStream = Mockito.mock(PrintStream.class);

        SearchInput searchInput = new DemoSearchUI(searcher, inStream, outStream);

        // Run
        searchInput.run();

        // Verify that all items have been found
        Assert.assertEquals("No items found", 10, results.size());
    }
}