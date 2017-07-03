package com.mixer.interactive.test.util;

import com.google.common.eventbus.Subscribe;
import com.mixer.interactive.event.InteractiveEvent;

import java.util.Map;
import java.util.TreeMap;

public class TestEventHandler {

    private static Map<Integer, InteractiveEvent> eventMap = new TreeMap<>();

    @Subscribe
    public void onInteractiveEvent(InteractiveEvent event) {
        eventMap.put(event.getRequestID(), event);
    }

    public static Map<Integer, InteractiveEvent> getEventMap() {
        return eventMap;
    }

    public static void clear() {
        eventMap.clear();
    }
}
