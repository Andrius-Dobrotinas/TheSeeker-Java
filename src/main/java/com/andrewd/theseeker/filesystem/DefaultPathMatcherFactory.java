package com.andrewd.theseeker.filesystem;

import java.nio.file.FileSystem;
import java.nio.file.PathMatcher;

/**
 * Created by Andrew D on 11/15/2016.
 */
public class DefaultPathMatcherFactory implements PathMatcherFactory {
    private final FileSystem fileSystem;
    private final String syntax;

    public static final String SYNTAX_GLOB = "glob";

    public DefaultPathMatcherFactory(FileSystem fileSystem, String syntax) {
        this.fileSystem = fileSystem;
        this.syntax = syntax;
    }

    public PathMatcher apply(String pattern) {
        return fileSystem.getPathMatcher(syntax + ":" + pattern);
    }
}