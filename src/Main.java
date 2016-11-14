import com.andrewd.theseeker.*;
import com.andrewd.theseeker.filesystem.FileSearchEngine;
import com.andrewd.theseeker.filesystem.PlainFileVisitor;
import com.andrewd.theseeker.controls.SearchInput;
import com.andrewd.theseeker.controls.console.DemoSearchInput;
import com.andrewd.theseeker.controls.console.DemoSearchConsumer;

import java.io.IOException;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args) throws IOException {
        java.io.PrintStream outStream = System.out;
        SearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree);
        SearchResultsConsumer consumer = new DemoSearchConsumer(outStream);
        AsyncSearcher searcher = new Searcher(searchEngine);

        searchEngine.addItemFoundEventListener(consumer::push);
        searchEngine.addStatusEventListener(consumer::pushStatus);

        SearchInput searchInput = new DemoSearchInput(searcher, System.in, outStream);
        searchInput.run();
    }
}