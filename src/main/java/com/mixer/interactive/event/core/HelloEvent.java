package com.mixer.interactive.event.core;

/**
 * Interactive event posted by the Interactive service when the client's connection is fully established (connected
 * and has authenticated).
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class HelloEvent extends InteractiveCoreEvent {

    /**
     * Initialize a new <code>HelloEvent</code>.
     *
     * @since   1.0.0
     */
    public HelloEvent() {
        // NO-OP
    }
}
