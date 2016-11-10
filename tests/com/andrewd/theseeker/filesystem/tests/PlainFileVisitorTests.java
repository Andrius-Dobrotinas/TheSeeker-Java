package com.andrewd.theseeker.filesystem.tests;

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

/**
 * Created by qwe on 11/8/2016.
 */
public class PlainFileVisitorTests extends FileSearchEngineTestsBase {

    @Test
    public void MustFindOneFile() throws IOException {
        List<Path> results = new ArrayList<>();

        PathMatcher matcherMock = Mockito.mock(PathMatcher.class);
        Mockito.when(matcherMock.matches(Matchers.any(Path.class)))
                // Returns True if file name ends with "rar"
                .thenAnswer(x -> ((Path)x.getArguments()[0]).getFileName().toString().endsWith("rar"));
        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);

        FileVisitor<Path> fileVisitor = new PlainFileVisitor(matcherMock,
                x -> results.add((Path)x), ioExceptionConsumerMock);

        // Run
        Files.walkFileTree(locationRoot, fileVisitor);

        // Verify
        Assert.assertTrue("No files found", results.size() > 0);
        Assert.assertTrue("Wrong number of files found", results.size() == 1);
        Assert.assertEquals("Wrong file found", results.get(0).toFile(), uniqueFile2_rar.getAbsoluteFile());
    }

    @Test
    public void MustFindOneFileInSubdirectory() throws IOException {
        List<Path> results = new ArrayList<>();

        PathMatcher matcherMock = Mockito.mock(PathMatcher.class);
        Mockito.when(matcherMock.matches(Matchers.any(Path.class)))
                // Returns True if file name ends with "asd"
                .thenAnswer(x -> ((Path)x.getArguments()[0]).getFileName().toString().endsWith("asd"));
        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);

        FileVisitor<Path> fileVisitor = new PlainFileVisitor(matcherMock,
                x -> results.add((Path)x), ioExceptionConsumerMock);

        // Run
        Files.walkFileTree(locationRoot, fileVisitor);

        // Verify
        Assert.assertTrue("No files found", results.size() > 0);
        Assert.assertTrue("Wrong number of files found", results.size() == 1);
        Assert.assertEquals("Wrong file found", results.get(0).toFile(), subDir__uniqueFile2_asd.getAbsoluteFile());
    }

    @Test
    public void MustFindTwoFilesWithTheSameName_ExactNameMatch() throws IOException {
        List<Path> results = new ArrayList<>();

        PathMatcher matcherMock = Mockito.mock(PathMatcher.class);
        Mockito.when(matcherMock.matches(Matchers.any(Path.class)))
                // There are two files with the same name. One of them is referenced by "subDir__file1_tmp__copy"
                .thenAnswer(x -> ((Path)x.getArguments()[0]).getFileName().toString().equals(subDir__file1_tmp__copy.getName()));
        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);

        FileVisitor<Path> fileVisitor = new PlainFileVisitor(matcherMock,
                x -> results.add((Path)x), ioExceptionConsumerMock);

        // Run
        Files.walkFileTree(locationRoot, fileVisitor);

        // Verify
        Assert.assertTrue("No files found", results.size() > 0);
        Assert.assertTrue("Wrong number of files found", results.size() == 2);

        Assert.assertTrue("Search results don't contain the file that was supposed to be found",
                results.stream().anyMatch(x -> x.toString().equals(file1_tmp.toString())));
        Assert.assertTrue("Search results don't contain the file that was supposed to be found",
                results.stream().anyMatch(x -> x.toString().equals(subDir__file1_tmp__copy.toString())));
    }

    @Test
    public void MustInvoke1ItemFoundCallback_WithCorrespondingFile() throws IOException {
        List<Path> results = new ArrayList<>();

        PathMatcher matcherMock = Mockito.mock(PathMatcher.class);
        Mockito.when(matcherMock.matches(Matchers.any(Path.class)))
                // Returns True if file name ends with "rar"
                .thenAnswer(x -> ((Path)x.getArguments()[0]).getFileName().toString().endsWith("rar"));
        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);

        FileVisitor<Path> fileVisitor = new PlainFileVisitor(matcherMock,
                x -> results.add((Path)x), ioExceptionConsumerMock);

        // Run
        Files.walkFileTree(locationRoot, fileVisitor);

        // Verify
        Assert.assertTrue("No files found", results.size() > 0);
        Assert.assertTrue("Wrong number of files found", results.size() == 1);

        // Verify that the file returned by the callback is the one that was supposed to be found
        Assert.assertEquals("Wrong file found", uniqueFile2_rar.getAbsoluteFile(), results.get(0).toFile());
    }

    @Test
    public void MustInvokeItemFoundCallback4Times_WithCorrespondingFiles() throws IOException {
        List<Path> results = new ArrayList<>();

        PathMatcher matcherMock = Mockito.mock(PathMatcher.class);
        Mockito.when(matcherMock.matches(Matchers.any(Path.class)))
                .thenAnswer(x -> ((Path)x.getArguments()[0]).getFileName().toString().endsWith("tmp"));
        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);

        FileVisitor<Path> fileVisitor = new PlainFileVisitor(matcherMock,
                x -> results.add((Path)x), ioExceptionConsumerMock);

        // Run
        Files.walkFileTree(locationRoot, fileVisitor);

        // Verify
        Assert.assertTrue("No files found", results.size() > 0);
        Assert.assertTrue("Wrong number of files found", results.size() == 4);

        // Verify that files returned by the callbacks are those were supposed to be found
        Assert.assertTrue("Search results don't contain the file that was supposed to be found",
                results.stream().anyMatch(x -> x.toString().equals(uniqueFile1_tmp.toString())));
        Assert.assertTrue("Search results don't contain the file that was supposed to be found",
                results.stream().anyMatch(x -> x.toString().equals(file1_tmp.toString())));
        Assert.assertTrue("Search results don't contain the file that was supposed to be found",
                results.stream().anyMatch(x -> x.toString().equals(subDir__uniqueFile1_tmp.toString())));
        Assert.assertTrue("Search results don't contain the file that was supposed to be found",
                results.stream().anyMatch(x -> x.toString().equals(subDir__file1_tmp__copy.toString())));
    }

    @Test
    public void MustInvokeIOExceptionCallback_On_VisitFileFailed() throws IOException {
        PathMatcher matcherMock = Mockito.mock(PathMatcher.class);
        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);
        Consumer<Object> itemFoundCounsumer = Mockito.mock(Consumer.class);

        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        Mockito.doAnswer(x ->
                ((FileVisitor<Path>)x.getArguments()[1]).visitFileFailed(file1_tmp.toPath(),
                        new IOException("File could not be access")))
                .when(walkerTexasRangerMock).walkFileTree(Matchers.any(Path.class), Matchers.<FileVisitor<Path>>any());


        FileVisitor<Path> fileVisitor = new PlainFileVisitor(matcherMock, itemFoundCounsumer, ioExceptionConsumerMock);

        // Run
        walkerTexasRangerMock.walkFileTree(Paths.get(""), fileVisitor);

        // Verify
        Mockito.verify(ioExceptionConsumerMock, Mockito.times(1)).accept(Matchers.any(IOException.class));
    }

    @Test
    public void MustInvokeIOExceptionCallback_On_postVisitDirectory() throws IOException {
        PathMatcher matcherMock = Mockito.mock(PathMatcher.class);
        Consumer<IOException> ioExceptionConsumerMock = Mockito.mock(Consumer.class);
        Consumer<Object> itemFoundCounsumer = Mockito.mock(Consumer.class);

        FileTreeWalker walkerTexasRangerMock = Mockito.mock(FileTreeWalker.class);
        Mockito.doAnswer(x ->
                ((FileVisitor<Path>)x.getArguments()[1]).postVisitDirectory(file1_tmp.toPath(),
                        new IOException("File could not be access")))
                .when(walkerTexasRangerMock).walkFileTree(Matchers.any(Path.class), Matchers.<FileVisitor<Path>>any());


        FileVisitor<Path> fileVisitor = new PlainFileVisitor(matcherMock, itemFoundCounsumer, ioExceptionConsumerMock);

        // Run
        walkerTexasRangerMock.walkFileTree(Paths.get(""), fileVisitor);

        // Verify
        Mockito.verify(ioExceptionConsumerMock, Mockito.times(1)).accept(Matchers.any(IOException.class));
    }

    // TODO: test status updates (search location)
}