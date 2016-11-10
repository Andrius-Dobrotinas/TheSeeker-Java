package com.andrewd.theseeker.filesystem;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

/**
 * Created by qwe on 11/7/2016.
 */
public class PlainFileVisitor implements FileVisitor<Path> {
    private PathMatcher matcher;
    private Consumer<Object> onItemFound;
    private Consumer<IOException> exceptionConsumer;

    public PlainFileVisitor(PathMatcher matcher, Consumer<Object> onItemFound) {
        this(matcher, onItemFound, null);
    }

    public PlainFileVisitor(PathMatcher matcher, Consumer<Object> onItemFound, Consumer<IOException> exceptionConsumer) {
        this.matcher = matcher;
        this.onItemFound = onItemFound;
        this.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (matcher.matches(file.getFileName())) {
            onItemFound.accept(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        if (exc != null && exceptionConsumer != null) {
            exceptionConsumer.accept(exc);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc != null && exceptionConsumer != null) {
            exceptionConsumer.accept(exc);
        }
        return FileVisitResult.CONTINUE;
    }
}