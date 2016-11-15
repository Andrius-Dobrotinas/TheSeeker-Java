package com.andrewd.theseeker.controls.console;

import com.andrewd.theseeker.SearchResultsConsumer;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Created by Andrew D on 11/13/2016.
 */
public class DemoSearchConsumer implements SearchResultsConsumer<Path> {
    private PrintStream outStream;

    public static final String STATUS_PREFIX = "STATUS: ";

    public DemoSearchConsumer(PrintStream outStream) {
        this.outStream = outStream;
    }

    @Override
    public void push(Path item) {
        outStream.println(item);
    }

    @Override
    public void pushStatus(Object status) {
        outStream.println(STATUS_PREFIX + status);
    }
}