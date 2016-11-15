package com.andrewd.theseeker.filesystem;

import java.nio.file.PathMatcher;
import java.util.function.Function;

/**
 * Created by Andrew D on 11/15/2016.
 */
public interface PathMatcherFactory extends Function<String, PathMatcher> {

}