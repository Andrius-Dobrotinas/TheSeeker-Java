package com.andrewd.theseeker.filesystem;

import com.andrewd.theseeker.SearchEngineBase;
import com.andrewd.theseeker.async.CancellationToken;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Andrew D on 11/7/2016.
 */
public class FileSearchEngine extends SearchEngineBase<Path, Path> {
    private final FileVisitorFactory fileVisitorFactory;
    private final FileTreeWalker fileTreeWalker;
    private final PathMatcherFactory pathMatcherFactory;
    private final List<Consumer<IOException>> ioExceptionListeners = new ArrayList<>();

    public FileSearchEngine(FileVisitorFactory fileVisitorFactory, FileTreeWalker fileTreeWalker,
                            PathMatcherFactory pathMatcherFactory) {
        if (fileVisitorFactory == null){
            throw new IllegalArgumentException("fileVisitorFactory");
        }
        if (fileTreeWalker == null){
            throw new IllegalArgumentException("fileTreeWalker");
        }
        if (pathMatcherFactory == null){
            throw new IllegalArgumentException("pathMatcherFactory");
        }
        this.fileVisitorFactory = fileVisitorFactory;
        this.fileTreeWalker = fileTreeWalker;
        this.pathMatcherFactory = pathMatcherFactory;
    }

    @Override
    public void performSearch(String location, String pattern, CancellationToken cancellationToken) throws Exception {
        PathMatcher matcher = pathMatcherFactory.apply(pattern);
        fileTreeWalker.walkFileTree(Paths.get(location),
                fileVisitorFactory.createVisitor(matcher,
                        item -> super.onItemFound(item),
                        status -> super.onStatusUpdate(status),
                        cancellationToken,
                        this::onIOException));
    }

    public void addIOExceptionEventListener(Consumer<IOException> exceptionHandler) {
        if (exceptionHandler == null){
            throw new IllegalArgumentException("exceptionHandler");
        }
        ioExceptionListeners.add(exceptionHandler);
    }

    private void onIOException(IOException exc) {
        for(Consumer<IOException> listener : ioExceptionListeners) {
            listener.accept(exc);
        }
    }
}