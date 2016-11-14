package com.andrewd.theseeker.controls.console;

import com.andrewd.theseeker.controls.*;
import com.andrewd.theseeker.AsyncSearcher;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Andrew D on 11/13/2016.
 */
public class DemoSearchInput implements SearchInput {
    private AsyncSearcher searcher;
    private InputStream inStream;
    private PrintStream outStream;
    private volatile boolean finished;

    public final static String EXIT_COMMAND = "exit";
    private final static String CANCEL_COMMAND = "";

    public DemoSearchInput(AsyncSearcher searcher, InputStream inStream, PrintStream outStream) {
        this.searcher = searcher;
        this.inStream = inStream;
        this.outStream = outStream;
        searcher.addFinishEventListener(() ->
                finished = true);
    }

    public void run() {
        Scanner input = new Scanner(inStream);
        boolean exit = false;

        while(!exit) {
            outStream.println("Welcome to The Seeker console demo!");
            outStream.println("To exit, type EXIT and hit ENTER.");
            outStream.println("To stop searching, simply hit ENTER at any time.");

            finished = false;
            String location = null;
            String pattern = "";
            while(pattern.isEmpty()) {
                do {
                    outStream.println("\nEnter location to search");
                    location = input.nextLine();
                    if (isExit(location)) return;
                } while(location.isEmpty());

                outStream.println("Enter search pattern");
                pattern = input.nextLine();
                if (isExit(location)) return;
            }

            searcher.searchAsync(location, pattern);

            // Not DI'ing the executor
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future task = executorService.submit(() -> listenForCancellation(inStream));

            while(!finished) { }
            task.cancel(true);

            outStream.println("------DONE------\n");
        }
    }

    private boolean isExit(String command) {
        return command.equalsIgnoreCase(EXIT_COMMAND);
    }

    private void listenForCancellation(InputStream inStream) {
        Scanner input = new Scanner(inStream);
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if(input.nextLine().equals(CANCEL_COMMAND)) {
                outStream.println("Search cancelled!\n");
                searcher.stop();
            }
        }
    }
}