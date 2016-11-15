package com.andrewd.theseeker;

import com.andrewd.theseeker.async.CancellationToken;

import java.util.function.Consumer;

/**
 * Created by Andrew D. on 11/11/2016.
 */
public interface SearchEngine<T> {
    void search(String location, String pattern, CancellationToken cancellationToken);
    void addItemFoundEventListener(Consumer<T> listener);
    void addStatusEventListener(Consumer<Object> statusConsumer);
}