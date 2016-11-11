package com.andrewd.theseeker;

import java.util.function.Consumer;

/**
 * Created by Andrew D. on 11/11/2016.
 */
public interface SearchEngine {
    void search(String location, String pattern);
    void addItemFoundEventListener(ItemFoundEventListener listener);
    void addStatusEventListener(Consumer<Object> statusConsumer);
}