package com.andrewd.theseeker.filesystem;

import com.andrewd.theseeker.SearchEngineBase;
import com.andrewd.theseeker.async.CancellationToken;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Andrew D on 11/7/2016.
 */
public class FileSearchEngine extends SearchEngineBase {
    private FileVisitorFactory fileVisitorFactory;
    private FileTreeWalker fileTreeWalker;
    private List<Consumer<IOException>> ioExceptionListeners = new ArrayList<>();

    public FileSearchEngine(FileVisitorFactory fileVisitorFactory, FileTreeWalker fileTreeWalker) {
        this.fileVisitorFactory = fileVisitorFactory;
        this.fileTreeWalker = fileTreeWalker;
    }

    @Override
    public void performSearch(String location, String pattern, CancellationToken cancellationToken) {
        // TODO get this PathMatcher instantiation in order (inject it)
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

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

    protected void onIOException(IOException exc) {
        for(Consumer<IOException> listener : ioExceptionListeners) {
            listener.accept(exc);
        }
    }
}