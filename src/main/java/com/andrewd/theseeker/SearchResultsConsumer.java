package com.andrewd.theseeker;

/**
 * Created by Andrew D on 11/11/2016.
 */
public interface SearchResultsConsumer<T, S> {
    void push(T item);
    void pushStatus(S data);
}