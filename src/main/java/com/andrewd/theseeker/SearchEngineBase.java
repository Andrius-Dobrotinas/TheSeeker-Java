package com.andrewd.theseeker;

import com.andrewd.theseeker.async.CancellationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by qwe on 11/7/2016.
 */
public abstract class SearchEngineBase implements SearchEngine {
    private List<ItemFoundEventListener> itemFoundListeners = new ArrayList<>();
    private List<Consumer<Object>> statusUpdateEventListeners = new ArrayList<>();

    public final void search(String location, String pattern, CancellationToken cancellationToken) {
        onStatusUpdate("STARTED");
        performSearch(location, pattern, cancellationToken);
        onStatusUpdate("FINISHED");
    }

    protected abstract void performSearch(String location, String pattern, CancellationToken cancellationToken);

    public void addItemFoundEventListener(ItemFoundEventListener listener) {
        itemFoundListeners.add(listener);
    }

    protected void onItemFound(Object item) {
        for(ItemFoundEventListener listener : itemFoundListeners) {
            listener.onItemFound(item);
        }
    }

    public void addStatusEventListener(Consumer<Object> statusConsumer) {
        statusUpdateEventListeners.add(statusConsumer);
    }

    protected void onStatusUpdate(Object status) {
        for(Consumer<Object> listener : statusUpdateEventListeners) {
            listener.accept(status);
        }
    }
}