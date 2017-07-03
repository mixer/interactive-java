package com.mixer.interactive.event.core;

/**
 * Interactive event posted by the Interactive service when the "ready" state changes.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ReadyEvent extends InteractiveCoreEvent {

    /**
     * <code>true</code> if Interactive integration is ready for interaction, <code>false</code> otherwise
     */
    private final Boolean isReady;

    /**
     * Initializes a new <code>ReadyEvent</code>.
     *
     * @param   isReady
     *          <code>true</code> if Interactive integration is ready for interaction, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public ReadyEvent(Boolean isReady) {
        this.isReady = isReady;
    }

    /**
     * Returns <code>true</code> if Interactive integration is ready for interaction, <code>false</code> otherwise.
     *
     * @return  <code>true</code> if Interactive integration is ready for interaction, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean isReady() {
        return isReady;
    }
}
