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
// TODO: see if I can optimize checks for cancellation
public class PlainFileVisitor implements FileVisitor<Path> {
    private PathMatcher matcher;
    private Consumer<Object> onItemFound;
    private Consumer<Object> onVisitDirectory;
    private Consumer<IOException> exceptionConsumer;
    private CancellationToken cancellationToken;

    public PlainFileVisitor(PathMatcher matcher, Consumer<Object> onItemFound, Consumer<Object> onVisitDirectory,
                            CancellationToken cancellationToken) {
        this(matcher, onItemFound, onVisitDirectory, cancellationToken, null);
    }

    public PlainFileVisitor(PathMatcher matcher, Consumer<Object> onItemFound, Consumer<Object> onVisitDirectory,
                            CancellationToken cancellationToken, Consumer<IOException> exceptionConsumer) {
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