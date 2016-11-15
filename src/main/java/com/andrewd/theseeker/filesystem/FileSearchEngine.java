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
    private FileVisitorFactory fileVisitorFactory;
    private FileTreeWalker fileTreeWalker;
    private PathMatcherFactory pathMatcherFactory;
    private List<Consumer<IOException>> ioExceptionListeners = new ArrayList<>();

    public FileSearchEngine(FileVisitorFactory fileVisitorFactory, FileTreeWalker fileTreeWalker,
                            PathMatcherFactory pathMatcherFactory) {
        this.fileVisitorFactory = fileVisitorFactory;
        this.fileTreeWalker = fileTreeWalker;
        this.pathMatcherFactory = pathMatcherFactory;
    }

    @Override
    public void performSearch(String location, String pattern, CancellationToken cancellationToken) {
        PathMatcher matcher = pathMatcherFactory.apply(pattern);

        try {
            fileTreeWalker.walkFileTree(Paths.get(location),
                    fileVisitorFactory.createVisitor(matcher,
                            item -> super.onItemFound(item),
                            status -> super.onStatusUpdate(status),
                            cancellationToken,
                            this::onIOException));
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: handle exception
            // TODO: probably push status update?
        }
    }

    public void addIOExceptionEventListener(Consumer<IOException> listener) {
        ioExceptionListeners.add(listener);
    }

    private void onIOException(IOException exc) {
        for(Consumer<IOException> listener : ioExceptionListeners) {
            listener.accept(exc);
        }
    }
}