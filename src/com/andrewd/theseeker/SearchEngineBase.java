package com.andrewd.theseeker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by qwe on 11/7/2016.
 */
public abstract class SearchEngineBase {
    private List<ItemFoundEventListener> itemFoundListeners = new ArrayList<>();
    private List<Consumer<Object>> statusUpdateEventListenrs = new ArrayList<>();

    public abstract void search(String location, String pattern);

    public void addItemFoundEventListener(ItemFoundEventListener listener) {
        itemFoundListeners.add(listener);
    }

    protected void onItemFound(Object item) {
        for(ItemFoundEventListener listener : itemFoundListeners) {
            listener.onItemFound(item);
        }
    }

    public void addStatusEventListener(Consumer<Object> statusConsumer) {
        statusUpdateEventListenrs.add(statusConsumer);
    }

    protected void onStatusUpdate(Object status) {
        for(Consumer<Object> listener : statusUpdateEventListenrs) {
            listener.accept(status);
        }
    }
}