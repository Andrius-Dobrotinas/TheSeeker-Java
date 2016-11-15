package com.andrewd.theseeker;

import com.andrewd.theseeker.async.CancellationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Andrew D on 11/7/2016.
 */
public abstract class SearchEngineBase<T, S> implements SearchEngine<T, S> {
    private List<Consumer<T>> itemFoundListeners = new ArrayList<>();
    private List<Consumer<S>> statusUpdateEventListeners = new ArrayList<>();

    public final void search(String location, String pattern, CancellationToken cancellationToken) {
        //onStatusUpdate("STARTED"); TODO: implement separate STARTED/FINISHED listeners
        performSearch(location, pattern, cancellationToken);
        //onStatusUpdate("FINISHED");
    }

    protected abstract void performSearch(String location, String pattern, CancellationToken cancellationToken);

    public void addItemFoundEventListener(Consumer<T> listener) {
        itemFoundListeners.add(listener);
    }

    protected void onItemFound(T item) {
        for(Consumer<T> listener : itemFoundListeners) {
            listener.accept(item);
        }
    }

    public void addStatusEventListener(Consumer<S> statusConsumer) {
        statusUpdateEventListeners.add(statusConsumer);
    }

    protected void onStatusUpdate(S status) {
        for(Consumer<S> listener : statusUpdateEventListeners) {
            listener.accept(status);
        }
    }
}