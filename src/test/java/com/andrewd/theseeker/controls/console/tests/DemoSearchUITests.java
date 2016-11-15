package com.andrewd.theseeker.controls.console.tests;

import com.andrewd.theseeker.AsyncSearcher;
import com.andrewd.theseeker.SearchEngine;
import com.andrewd.theseeker.Searcher;
import com.andrewd.theseeker.controls.SearchInput;
import com.andrewd.theseeker.controls.console.DemoSearchUI;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Created by Andrew D on 11/14/2016.
 */
public class DemoSearchUITests {

    @Test
    public void MustCallSearchEngine_SearchMethod() throws IOException {
        SearchEngine<Path> searchEngine = Mockito.mock(SearchEngine.class);
        AsyncSearcher searcher = new Searcher(searchEngine);

        InputStreamFake inStream = new InputStreamFake("c:\nasd\n" + DemoSearchUI.EXIT_COMMAND + "\n");
        PrintStream outStream = Mockito.mock(PrintStream.class);

        SearchInput searchInput = new DemoSearchUI(searcher, inStream, outStream);

        // Run
        searchInput.run();

        // Verify
        Mockito.verify(searchEngine, Mockito.times(1)).search(Matchers.any(), Matchers.any(), Matchers.any());
    }
}