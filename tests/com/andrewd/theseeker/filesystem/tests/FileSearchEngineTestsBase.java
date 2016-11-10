package com.andrewd.theseeker.filesystem.tests;

import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Base class for tests with file system. Takes care of creation
 * and deletion of temp directories and files
 */
public class FileSearchEngineTestsBase {
    protected static Path locationRoot;
    protected static File locationRootPath;
    protected static File uniqueFile1_tmp;
    protected static File uniqueFile2_rar;
    protected static File file1_tmp;
    protected static Path subDirectory;
    protected static File subDirectoryPath;
    protected static File subDir__uniqueFile1_tmp;
    protected static File subDir__uniqueFile2_asd;
    protected static File subDir__file1_tmp__copy;

    @BeforeClass
    public static void CreateTempFiles() throws IOException {
        // Create a root temp folder
        locationRoot = Files.createTempDirectory("seeker");
        locationRootPath = locationRoot.toFile();

        // Create some files
        uniqueFile1_tmp = File.createTempFile("test1", ".tmp", locationRootPath);
        uniqueFile2_rar = File.createTempFile("test2", ".rar", locationRootPath);
        file1_tmp = File.createTempFile("test3", ".tmp", locationRootPath);

        // Create a subdirectory
        subDirectory = Paths.get(locationRootPath.toString(), "SubDirectory");

        // Create files in the subdirectory
        Files.createDirectory(subDirectory);
        subDirectoryPath = subDirectory.toFile();
        subDir__uniqueFile1_tmp = File.createTempFile("sub1", ".tmp", subDirectoryPath);
        subDir__uniqueFile2_asd = File.createTempFile("sub2", ".asd", subDirectoryPath);

        // Create a file with an identical name in the subdirectory
        subDir__file1_tmp__copy = new File(Paths.get(subDirectory.toString(), file1_tmp.getName()).toString());
        Files.createFile(subDir__file1_tmp__copy.toPath());

        uniqueFile1_tmp.deleteOnExit();
        uniqueFile2_rar.deleteOnExit();
        file1_tmp.deleteOnExit();
        subDirectoryPath.deleteOnExit();
        subDir__uniqueFile1_tmp.deleteOnExit();
        subDir__uniqueFile2_asd.deleteOnExit();
        subDir__file1_tmp__copy.deleteOnExit();
        locationRootPath.deleteOnExit();
    }
}