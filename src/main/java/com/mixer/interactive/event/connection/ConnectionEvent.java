package com.mixer.interactive.event.connection;

import com.mixer.interactive.event.InteractiveEvent;

import java.net.URI;

/**
 * The class <code>InteractiveEvent</code> is the superclass of all classes relating to websocket connection events
 * involving the Interactive service and the <code>GameClient</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ConnectionEvent extends InteractiveEvent {

    /**
     * The project version ID for the Interactive integration
     */
    private final Number projectVersionID;

    /**
     * <code>URI</code> for the connected Interactive service host
     */
    private final URI interactiveHostURI;

    /**
     * Initializes a new <code>ConnectionEvent</code>.
     *
     * @param   projectVersionID
     *          The project version ID for the Interactive integration
     * @param   interactiveHostURI
     *          <code>URI</code> for the connected Interactive service host
     *
     * @since   1.0.0
     */
    ConnectionEvent(Number projectVersionID, URI interactiveHostURI) {
        this.projectVersionID = projectVersionID;
        this.interactiveHostURI = interactiveHostURI;
    }

    /**
     * Returns the project version ID for the Interactive integration.
     *
     * @return  The project version ID for the Interactive integration.
     *
     * @since   1.0.0
     */
    public Number getProjectVersionID() {
        return projectVersionID;
    }

    /**
     * Returns the <code>URI</code> for the connected Interactive service host
     *
     * @return  The <code>URI</code> for the connected Interactive service host
     *
     * @since   1.0.0
     */
    public URI getInteractiveHostURI() {
        return interactiveHostURI;
    }
}
