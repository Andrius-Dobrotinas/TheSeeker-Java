package com.andrewd.theseeker.filesystem.tests;

import com.andrewd.theseeker.ItemFoundEventListener;
import com.andrewd.theseeker.filesystem.FileSearchEngine;
import com.andrewd.theseeker.filesystem.PlainFileVisitor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSearchEngineTests extends FileSearchEngineTestsBase {

    // TODO: Maybe the PathMatcher should use wildcards by default and not go for exact match unless specified?

    @Test
    public void MustFindOneFileWhoseNameMatchesExactly() throws IOException {
        List<Path> results = new ArrayList<>();
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new);
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
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new);
        ItemFoundEventListener itemFoundCallbackMock = Mockito.mock(ItemFoundEventListener.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);

        // Run
        searchEngine.search(locationRoot.toString(), uniqueFile1_tmp.getName());

        // Verify
        Mockito.verify(itemFoundCallbackMock, Mockito.times(1)).onItemFound(Matchers.any());
    }

    @Test
    public void MustInvokeOneItemFoundCallback_ForSeveralItems() throws IOException {
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new);
        ItemFoundEventListener itemFoundCallbackMock = Mockito.mock(ItemFoundEventListener.class);
        searchEngine.addItemFoundEventListener(itemFoundCallbackMock);

        // Run
        searchEngine.search(locationRoot.toString(), "*tmp");

        // Verify -- There are four files that end with "tmp"
        Mockito.verify(itemFoundCallbackMock, Mockito.times(4)).onItemFound(Matchers.any());
    }

    @Test
    public void MustInvokeTwoItemFoundCallbacks_ForSingleItem() throws IOException {
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new);
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
        FileSearchEngine searchEngine = new FileSearchEngine(PlainFileVisitor::new);
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
}