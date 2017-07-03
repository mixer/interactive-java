package com.mixer.interactive.event;

/**
 * The abstract class <code>InteractiveEvent</code> is the superclass of all classes relating to events sent
 * from the Interactive service to the <code>GameClient</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public abstract class InteractiveEvent {

    /**
     * Unique numeric identifier for the packet the resulted in this event
     */
    private int id;

    /**
     * Returns the identifier for the packet the resulted in this event.
     *
     * @return  Identifier for the packet the resulted in this event
     *
     * @since   1.0.0
     */
    public int getRequestID() {
        return id;
    }

    /**
     * Sets the identifier for the packet the resulted in this event.
     *
     * @param   id
     *          Identifier for the packet the resulted in this event
     *
     * @since   1.0.0
     */
    public void setRequestID(int id) {
        this.id = id;
    }
}
