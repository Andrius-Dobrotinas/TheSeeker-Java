package com.andrewd.theseeker.filesystem;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Path;

// TODO: if I run into problems with autowiring, I could just create a class for this

/**
 * Created by qwe on 11/10/2016.
 */
@FunctionalInterface
public interface FileTreeWalker {
    void walkFileTree(Path start, FileVisitor<? super Path> visitor) throws IOException;
}