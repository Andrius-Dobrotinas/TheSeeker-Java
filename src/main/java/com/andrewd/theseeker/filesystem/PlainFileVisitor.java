package com.andrewd.theseeker.filesystem;

import com.andrewd.theseeker.async.CancellationToken;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

/**
 * Created by Andrew D on 11/7/2016.
 */
public class PlainFileVisitor implements FileVisitor<Path> {
    private final PathMatcher matcher;
    private final Consumer<Path> onItemFound;
    private final Consumer<Path> onVisitDirectory;
    private final Consumer<IOException> exceptionConsumer;
    private final CancellationToken cancellationToken;

    public PlainFileVisitor(PathMatcher matcher, Consumer<Path> onItemFound, Consumer<Path> onVisitDirectory,
                            CancellationToken cancellationToken) {
        this(matcher, onItemFound, onVisitDirectory, cancellationToken, null);
    }

    public PlainFileVisitor(PathMatcher matcher, Consumer<Path> onItemFound, Consumer<Path> onVisitDirectory,
                            CancellationToken cancellationToken, Consumer<IOException> exceptionConsumer) {
        if (matcher == null){
            throw new IllegalArgumentException("matcher");
        }
        if (onItemFound == null){
            throw new IllegalArgumentException("onItemFound");
        }
        if (onVisitDirectory == null){
            throw new IllegalArgumentException("onVisitDirectory");
        }
        if (cancellationToken == null){
            throw new IllegalArgumentException("cancellationToken");
        }

        this.matcher = matcher;
        this.onItemFound = onItemFound;
        this.onVisitDirectory = onVisitDirectory;
        this.exceptionConsumer = exceptionConsumer;
        this.cancellationToken = cancellationToken;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (cancellationToken.isCancellationRequested()) {
            return FileVisitResult.TERMINATE;
        }
        onVisitDirectory.accept(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (cancellationToken.isCancellationRequested()) {
            return FileVisitResult.TERMINATE;
        }
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