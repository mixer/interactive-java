package com.mixer.interactive.event.connection;

import java.net.URI;

/**
 * Interactive event posted when a connection to the Interactive service has been opened and authenticated.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.1.0
 */
public class ConnectionEstablishedEvent extends ConnectionEvent {

    /**
     * Initializes a new <code>ConnectionEstablishedEvent</code>.
     *
     * @param   projectVersionID
     *          The project version ID for the Interactive integration
     * @param   interactiveHostURI
     *          <code>URI</code> for the connected Interactive service host
     *
     * @since   2.1.0
     */
    public ConnectionEstablishedEvent(Number projectVersionID, URI interactiveHostURI) {
        super(projectVersionID, interactiveHostURI);
    }
}
