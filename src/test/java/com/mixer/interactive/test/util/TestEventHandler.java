package com.mixer.interactive.test.util;

import com.google.common.eventbus.Subscribe;
import com.mixer.interactive.event.InteractiveEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * An event handler whose purpose is to catch all <code>InteractiveEvent</code>s that get posted as a result of testing
 * activities.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class TestEventHandler {

    /**
     * Event handler singleton
     */
    public static final TestEventHandler HANDLER = new TestEventHandler();

    /**
     * <code>Set</code> of events that are received
     */
    private Set<InteractiveEvent> eventSet = new HashSet<>();

    /**
     * Private constructor to prevent instantiation.
     *
     * @since   1.0.0
     */
    private TestEventHandler() {
        // NO-OP
    }

    /**
     * Listener method to catch <code>InteractiveEvent</code>s that are posted.
     *
     * @param   event
     *          The <code>InteractiveEvent</code> that was posted
     *
     * @since   1.0.0
     */
    @Subscribe
    public void onInteractiveEvent(InteractiveEvent event) {
        eventSet.add(event);
    }

    /**
     * Returns the <code>Set</code> of <code>InteractiveEvent</code>s that have been received.
     *
     * @return  <code>Set</code> of <code>InteractiveEvent</code>s that have been received
     *
     * @since   1.0.0
     */
    public Set<InteractiveEvent> getEvents() {
        return eventSet;
    }

    /**
     * Clears all caught <code>InteractiveEvent</code>s from the handler. Used to clear the state of the handler
     * prior to more events being posted/caught.
     *
     * @since   1.0.0
     */
    public void clear() {
        eventSet.clear();
    }
}
