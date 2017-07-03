package com.mixer.interactive.event.connection;

import java.net.URI;

/**
 * Interactive event posted when a websocket connection to the Interactive service is closed.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ConnectionClosedEvent extends ConnectionEvent {

    /**
     * HTTP status code
     */
    private final Number code;

    /**
     * HTTP status message
     */
    private final String message;

    /**
     * Whether or not the connection was closed remotely
     */
    private final boolean closedRemotely;

    /**
     * Initializes a new <code>ConnectionClosedEvent</code>.
     *
     * @param   projectVersionID
     *          The project version ID for the Interactive integration
     * @param   interactiveHostURI
     *          <code>URI</code> for the connected Interactive service host
     * @param   code
     *          HTTP status code
     * @param   message
     *          HTTP status message
     * @param   closedRemotely
     *          <code>true</code> if the connected was closed remotely, <code>false</code> otherwise
     *
     * @see     com.mixer.interactive.ws.InteractiveWebSocketClient#onClose(int, String, boolean)
     *
     * @since   1.0.0
     */
    public ConnectionClosedEvent(Number projectVersionID, URI interactiveHostURI, Number code, String message, boolean closedRemotely) {
        super(projectVersionID, interactiveHostURI);
        this.code = code;
        this.message = message;
        this.closedRemotely = closedRemotely;
    }

    /**
     * Returns the HTTP status code returned when the connected was closed.
     *
     * @return  The HTTP status code returned when the connected was closed
     *
     * @since   1.0.0
     */
    public Number getStatusCode() {
        return code;
    }

    /**
     * Returns the HTTP status message returned when the connection was closed.
     *
     * @return  The HTTP status message returned when the connection was closed
     *
     * @since   1.0.0
     */
    public String getStatusMessage() {
        return message;
    }

    /**
     * Returns<code>true</code> if the connection was closed remotely, <code>false</code> otherwise.
     *
     * @return  <code>true</code> if the connection was closed remotely, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean wasClosedRemotely() {
        return closedRemotely;
    }
}
