package com.andrewd.theseeker;

import com.andrewd.theseeker.async.CancellationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Andrew D on 11/7/2016.
 */
public abstract class SearchEngineBase<T, S> implements SearchEngine<T, S> {
    private final List<Consumer<T>> itemFoundListeners = new ArrayList<>();
    private final List<Consumer<S>> statusUpdateEventListeners = new ArrayList<>();

    public final void search(String location, String pattern, CancellationToken cancellationToken) throws Exception {
        if (cancellationToken == null){
            throw new IllegalArgumentException("cancellationToken");
        }

        // TODO: implement separate STARTED/FINISHED listeners someday when I actually need them
        //onStatusUpdate("STARTED");
        performSearch(location, pattern, cancellationToken);
        //onStatusUpdate("FINISHED");
    }

    protected abstract void performSearch(String location, String pattern, CancellationToken cancellationToken) throws Exception;

    public void addItemFoundEventListener(Consumer<T> listener) {
        if (listener == null){
            throw new IllegalArgumentException("listener");
        }
        itemFoundListeners.add(listener);
    }

    protected void onItemFound(T item) {
        for(Consumer<T> listener : itemFoundListeners) {
            listener.accept(item);
        }
    }

    public void addStatusEventListener(Consumer<S> statusConsumer) {
        if (statusConsumer == null){
            throw new IllegalArgumentException("statusConsumer");
        }
        statusUpdateEventListeners.add(statusConsumer);
    }

    protected void onStatusUpdate(S status) {
        for(Consumer<S> listener : statusUpdateEventListeners) {
            listener.accept(status);
        }
    }
}