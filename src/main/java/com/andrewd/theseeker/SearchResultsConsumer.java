package com.andrewd.theseeker;

/**
 * Created by Andrew D on 11/11/2016.
 */
public interface SearchResultsConsumer<T> {
    void push(T item);
    void pushStatus(Object status);
}