package com.andrewd.theseeker.filesystem;

import com.andrewd.theseeker.async.CancellationToken;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Consumer;

/**
 * Creates a new file visitor that uses the supplied pattern matcher to test files name and
 * invokes the supplied consumer for each matching file.
 */
public interface FileVisitorFactory {
    FileVisitor<Path> createVisitor(PathMatcher matcher, Consumer<Path> foundItemConsumer,
                                    Consumer<Path> onVisitDirectory,
                                    CancellationToken cancellationToken,
                                    Consumer<IOException> ioExceptionConsumer);
}