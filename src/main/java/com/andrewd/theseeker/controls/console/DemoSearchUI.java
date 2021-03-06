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
public class DemoSearchUI implements SearchInput {
    private final AsyncSearcher searcher;
    private final InputStream inStream;
    private final PrintStream outStream;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // TODO: see if I should DI it
    private volatile boolean finished;

    public final static String EXIT_COMMAND = "exit";
    private final static String CANCEL_COMMAND = "";

    public DemoSearchUI(AsyncSearcher searcher, InputStream inStream, PrintStream outStream) {
        if (searcher == null){
            throw new IllegalArgumentException("searcher");
        }
        if (inStream == null){
            throw new IllegalArgumentException("inStream");
        }
        if (outStream == null){
            throw new IllegalArgumentException("outStream");
        }

        this.searcher = searcher;
        this.inStream = inStream;
        this.outStream = outStream;
        searcher.addFinishEventListener(() ->
                finished = true);
    }

    public void run() {
        Scanner input = new Scanner(inStream);

        while(true) {
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
                    if (checkExit(location)) return;
                } while(location.isEmpty());

                outStream.println("Enter search pattern");
                pattern = input.nextLine();
                if (checkExit(location)) return;
            }

            searcher.searchAsync(location, pattern);

            Future task = executorService.submit(() -> listenForCancellation(inStream));

            // This performs less calculations than polling searcher.isRunning()
            while(!finished) { }
            task.cancel(true);

            outStream.println("------DONE------\n");
        }
    }

    private boolean checkExit(String command) {
        if (command.equalsIgnoreCase(EXIT_COMMAND)) {
            outStream.println("Shutting down...");
            destroy();
            outStream.println("Done!");
            return true;
        }
        return false;
    }

    private void destroy() {
        executorService.shutdownNow();
        searcher.destroy();
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
                return;
            }
        }
    }
}