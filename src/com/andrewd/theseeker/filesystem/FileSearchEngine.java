package com.andrewd.theseeker.filesystem;

import com.andrewd.theseeker.SearchEngineBase;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

/**
 * Created by qwe on 11/7/2016.
 */
public class FileSearchEngine extends SearchEngineBase {
    private FileVisitorFactory fileVisitorFactory;

    public FileSearchEngine(FileVisitorFactory fileVisitorFactory) {
        this.fileVisitorFactory = fileVisitorFactory;
    }

    @Override
    public void search(String location, String pattern) {
        // TODO get this PathMatcher instantiation in order
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);

        try {
            Files.walkFileTree(Paths.get(location),
                    fileVisitorFactory.createVisitor(matcher, item -> super.onItemFound(item)));
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }
}