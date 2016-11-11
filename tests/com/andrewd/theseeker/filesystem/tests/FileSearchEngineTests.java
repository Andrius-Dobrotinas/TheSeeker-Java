package com.andrewd.theseeker.filesystem.tests;

import com.andrewd.theseeker.ItemFoundEventListener;
import com.andrewd.theseeker.filesystem.FileSearchEngine;
import com.andrewd.theseeker.filesystem.FileTreeWalker;
import com.andrewd.theseeker.filesystem.FileVisitorFactory;
import com.andrewd.theseeker.filesystem.PlainFileVisitor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FileSearchEngineTests extends FileSearchEngineTestsBase {

    // TODO: Maybe the PathMatcher should use wildcards by default and not go for exact match unless specified?

    @Test
    public void MustFindOneFileWhoseNameMatchesExactly() throws IOException {
        List<Path> results = new ArrayList<>();
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree);
        searchEngine.addItemFoundEventListener(item -> results.add((Path)item));

        // Run
        searchEngine.search(locationRoot.toString(), uniqueFile1_tmp.getName());

        // Verify
        Assert.assertTrue("No files found", results.size() > 0);
        Assert.assertTrue("Wrong number of files found", results.size() == 1);
        Assert.assertEquals("Wrong file found", results.get(0).toFile(), uniqueFile1_tmp.getAbsoluteFile());
    }

    @Test
    public void MustInvokeSingleItemFoundCallback_ForSingleItem() throws IOException {
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree);
        ItemFoundEventListener itemFoundCallbackMock = Mockito.mock(ItemFoundEventListener.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);

        // Run
        searchEngine.search(locationRoot.toString(), uniqueFile1_tmp.getName());

        // Verify
        Mockito.verify(itemFoundCallbackMock, Mockito.times(1)).onItemFound(Matchers.any());
    }

    @Test
    public void MustInvokeOneItemFoundCallback_ForSeveralItems() throws IOException {
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree);
        ItemFoundEventListener itemFoundCallbackMock = Mockito.mock(ItemFoundEventListener.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);

        // Run
        searchEngine.search(locationRoot.toString(), "*tmp");

        // Verify -- There are four files that end with "tmp"
        Mockito.verify(itemFoundCallbackMock, Mockito.times(4)).onItemFound(Matchers.any());
    }

    @Test
    public void MustInvokeTwoItemFoundCallbacks_ForSingleItem() throws IOException {
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree);
        ItemFoundEventListener itemFoundCallbackMock = Mockito.mock(ItemFoundEventListener.class);
        ItemFoundEventListener itemFoundCallbackMock2 = Mockito.mock(ItemFoundEventListener.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock2);

        // Run
        searchEngine.search(locationRoot.toString(), uniqueFile1_tmp.getName());

        // Verify
        Mockito.verify(itemFoundCallbackMock, Mockito.times(1)).onItemFound(Matchers.any());
        Mockito.verify(itemFoundCallbackMock2, Mockito.times(1)).onItemFound(Matchers.any());
    }

    @Test
    public void MustInvokeTwoItemFoundCallbacks_ForSeveralItems() throws IOException {
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree);
        ItemFoundEventListener itemFoundCallbackMock = Mockito.mock(ItemFoundEventListener.class);
        ItemFoundEventListener itemFoundCallbackMock2 = Mockito.mock(ItemFoundEventListener.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock2);

        // Run
        searchEngine.search(locationRoot.toString(), "*tmp");

        // Verify -- There are four files that end with "tmp"
        Mockito.verify(itemFoundCallbackMock, Mockito.times(4)).onItemFound(Matchers.any());
        Mockito.verify(itemFoundCallbackMock2, Mockito.times(4)).onItemFound(Matchers.any());
    }

    @Test
    public void MustInvokeOneIOExceptionCallbackOnIOException() throws IOException {
        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        Mockito.doAnswer(x ->
                // Extract FileVisitor (the second argument) from the method and simply return exception
                ((FileVisitor<Path>)x.getArguments()[1]).visitFileFailed(file1_tmp.toPath(),
                        new IOException("File could not be access")))
                .when(walkerTexasRangerMock).walkFileTree(Matchers.any(Path.class), Matchers.<FileVisitor<Path>>any());

        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, walkerTexasRangerMock);

        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);
        searchEngine.addIOExceptionEventListener(ioExceptionConsumerMock);

        // Run
        searchEngine.search("", "");

        // Verify
        Mockito.verify(ioExceptionConsumerMock, Mockito.times(1)).accept(Matchers.any(IOException.class));
    }

    @Test
    public void MustInvokeTwoIOExceptionCallbacksOnIOException() throws IOException {
        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        Mockito.doAnswer(x ->
                // Extract FileVisitor (the second argument) from the method and simply return exception
                ((FileVisitor<Path>)x.getArguments()[1]).visitFileFailed(file1_tmp.toPath(),
                        new IOException("File could not be access")))
                .when(walkerTexasRangerMock).walkFileTree(Matchers.any(Path.class), Matchers.<FileVisitor<Path>>any());

        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, walkerTexasRangerMock);

        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);
        Consumer<IOException> ioExceptionConsumerMock2 = Mockito.mock(Consumer.class);
        searchEngine.addIOExceptionEventListener(ioExceptionConsumerMock);
        searchEngine.addIOExceptionEventListener(ioExceptionConsumerMock2);

        // Run
        searchEngine.search("", "");

        // Verify
        Mockito.verify(ioExceptionConsumerMock, Mockito.times(1)).accept(Matchers.any(IOException.class));
        Mockito.verify(ioExceptionConsumerMock2, Mockito.times(1)).accept(Matchers.any(IOException.class));
    }

    @Test
    public void MustPushStartAndFinishStatusUpdates() throws IOException {
        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        FileVisitorFactory visitorFactoryMock = Mockito.mock(FileVisitorFactory.class);
        FileSearchEngine searchEngine = new FileSearchEngine(visitorFactoryMock, walkerTexasRangerMock);
        Consumer<Object> statusConsumer = Mockito.mock(Consumer.class);
        searchEngine.addStatusEventListener(statusConsumer);

        // Run
        searchEngine.search("", "");

        // Verify
        Mockito.verify(statusConsumer, Mockito.times(1)).accept(("STARTED"));
        Mockito.verify(statusConsumer, Mockito.times(1)).accept(("FINISHED"));
        /* TODO: Started and Finished strings are temporary placeholders until
         I come up with a proper type for status updates
         */
    }
}