package com.andrewd.theseeker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qwe on 11/7/2016.
 */
public abstract class SearchEngineBase {
    private List<ItemFoundEventListener> itemFoundListeners = new ArrayList<>();

    public abstract void search(String location, String pattern);

    public void addItemFoundEventListener(ItemFoundEventListener listener) {
        itemFoundListeners.add(listener);
    }

    protected void onItemFound(Object item) {
        for(ItemFoundEventListener listener : itemFoundListeners) {
            listener.onItemFound(item);
        }
    }
}