package com.andrewd.theseeker.filesystem.tests;

import com.andrewd.theseeker.async.CancellationToken;
import com.andrewd.theseeker.filesystem.*;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.andrewd.theseeker.filesystem.DefaultPathMatcherFactory.SYNTAX_GLOB;

public class FileSearchEngineTests extends FileSearchEngineTestsBase {

    // TODO: Maybe the PathMatcher should use wildcards by default and not go for exact match unless specified?

    private static PathMatcherFactory getPathMatcher() {
        return new DefaultPathMatcherFactory(FileSystems.getDefault(), SYNTAX_GLOB);
    }

    @Test
    public void MustFindOneFileWhoseNameMatchesExactly() throws Exception {
        List<Path> results = new ArrayList<>();
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree, getPathMatcher());
        searchEngine.addItemFoundEventListener(results::add);

        // Run
        searchEngine.search(locationRoot.toString(), uniqueFile1_tmp.getName(), cancellationToken);

        // Verify
        Assert.assertTrue("No files found", results.size() > 0);
        Assert.assertTrue("Wrong number of files found", results.size() == 1);
        Assert.assertEquals("Wrong file found", results.get(0).toFile(), uniqueFile1_tmp.getAbsoluteFile());
    }

    @Test
    public void MustInvokeSingleItemFoundCallback_ForSingleItem() throws Exception {
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree, getPathMatcher());
        Consumer<Path> itemFoundCallbackMock = Mockito.mock(Consumer.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);

        // Run
        searchEngine.search(locationRoot.toString(), uniqueFile1_tmp.getName(), cancellationToken);

        // Verify
        Mockito.verify(itemFoundCallbackMock, Mockito.times(1)).accept(Matchers.any());
    }

    @Test
    public void MustInvokeOneItemFoundCallback_ForSeveralItems() throws Exception {
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree, getPathMatcher());
        Consumer<Path> itemFoundCallbackMock = Mockito.mock(Consumer.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);

        // Run
        searchEngine.search(locationRoot.toString(), "*tmp", cancellationToken);

        // Verify -- There are four files that end with "tmp"
        Mockito.verify(itemFoundCallbackMock, Mockito.times(4)).accept(Matchers.any());
    }

    @Test
    public void MustInvokeTwoItemFoundCallbacks_ForSingleItem() throws Exception {
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree, getPathMatcher());
        Consumer<Path> itemFoundCallbackMock = Mockito.mock(Consumer.class);
        Consumer<Path> itemFoundCallbackMock2 = Mockito.mock(Consumer.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock2);

        // Run
        searchEngine.search(locationRoot.toString(), uniqueFile1_tmp.getName(), cancellationToken);

        // Verify
        Mockito.verify(itemFoundCallbackMock, Mockito.times(1)).accept(Matchers.any());
        Mockito.verify(itemFoundCallbackMock2, Mockito.times(1)).accept(Matchers.any());
    }

    @Test
    public void MustInvokeTwoItemFoundCallbacks_ForSeveralItems() throws Exception {
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, Files::walkFileTree, getPathMatcher());
        Consumer<Path> itemFoundCallbackMock = Mockito.mock(Consumer.class);
        Consumer<Path> itemFoundCallbackMock2 = Mockito.mock(Consumer.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock2);

        // Run
        searchEngine.search(locationRoot.toString(), "*tmp", cancellationToken);

        // Verify -- There are four files that end with "tmp"
        Mockito.verify(itemFoundCallbackMock, Mockito.times(4)).accept(Matchers.any());
        Mockito.verify(itemFoundCallbackMock2, Mockito.times(4)).accept(Matchers.any());
    }

    @Test
    public void MustInvokeOneIOExceptionCallbackOnIOException() throws Exception {
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);
        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        Mockito.doAnswer(x ->
                // Extract FileVisitor (the second argument) from the method and simply return exception
                ((FileVisitor<Path>)x.getArguments()[1]).visitFileFailed(file1_tmp.toPath(),
                        new IOException("File could not be access")))
                .when(walkerTexasRangerMock).walkFileTree(Matchers.any(Path.class), Matchers.<FileVisitor<Path>>any());

        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, walkerTexasRangerMock, getPathMatcher());

        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);
        searchEngine.addIOExceptionEventListener(ioExceptionConsumerMock);

        // Run
        searchEngine.search("", "", cancellationToken);

        // Verify
        Mockito.verify(ioExceptionConsumerMock, Mockito.times(1)).accept(Matchers.any(IOException.class));
    }

    @Test
    public void MustInvokeTwoIOExceptionCallbacksOnIOException() throws Exception {
        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        Mockito.doAnswer(x ->
                // Extract FileVisitor (the second argument) from the method and simply return exception
                ((FileVisitor<Path>)x.getArguments()[1]).visitFileFailed(file1_tmp.toPath(),
                        new IOException("File could not be accessed")))
                .when(walkerTexasRangerMock).walkFileTree(Matchers.any(Path.class), Matchers.<FileVisitor<Path>>any());

        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new, walkerTexasRangerMock, getPathMatcher());

        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);
        Consumer<IOException> ioExceptionConsumerMock2 = Mockito.mock(Consumer.class);
        searchEngine.addIOExceptionEventListener(ioExceptionConsumerMock);
        searchEngine.addIOExceptionEventListener(ioExceptionConsumerMock2);
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);

        // Run
        searchEngine.search("", "", cancellationToken);

        // Verify
        Mockito.verify(ioExceptionConsumerMock, Mockito.times(1)).accept(Matchers.any(IOException.class));
        Mockito.verify(ioExceptionConsumerMock2, Mockito.times(1)).accept(Matchers.any(IOException.class));
    }

    /*@Test
    public void MustPushStartAndFinishStatusUpdates() throws IOException {
        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        FileVisitorFactory visitorFactoryMock = Mockito.mock(FileVisitorFactory.class);
        FileSearchEngine searchEngine = new FileSearchEngine(visitorFactoryMock, walkerTexasRangerMock, getPathMatcher());
        Consumer<Path> statusConsumer = Mockito.mock(Consumer.class);
        searchEngine.addStatusEventListener(statusConsumer);

        // Run
        searchEngine.search("", "", null);

        // Verify
        Mockito.verify(statusConsumer, Mockito.times(1)).accept(("STARTED"));
        Mockito.verify(statusConsumer, Mockito.times(1)).accept(("FINISHED"));
        // TODO: Started and Finished strings are temporary placeholders until I come up with a proper type for status updates
    }*/

    @Test
    public void MustPassCancellationTokenToFileVisitor() throws Exception {
        CancellationToken cancellationToken = Mockito.mock(CancellationToken.class);
        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        FileVisitorFactory visitorFactoryMock = Mockito.mock(FileVisitorFactory.class);
        FileSearchEngine searchEngine = new FileSearchEngine(visitorFactoryMock, walkerTexasRangerMock, getPathMatcher());

        // Run
        searchEngine.search("", "", cancellationToken);

        // Verify
        Mockito.verify(visitorFactoryMock, Mockito.times(1)).createVisitor(Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.eq(cancellationToken), Matchers.any());
    }
}