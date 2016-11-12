package com.andrewd.theseeker;

/**
 * Created by Andrew D on 11/11/2016.
 */
public interface SearchResultsConsumer {
    void push(Object item);
    void pushStatus(Object status);
}