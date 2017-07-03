package com.mixer.interactive.event.connection;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Interactive event posted when a websocket connection to the Interactive service is opened.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ConnectionOpenEvent extends ConnectionEvent {

    /**
     * HTTP status code.
     */
    private final Number code;

    /**
     * HTTP status message.
     */
    private final String message;

    /**
     * Initializes a new <code>ConnectionOpenEvent</code>.
     *
     * @param   projectVersionID
     *          The project version ID for the Interactive integration
     * @param   interactiveHostURI
     *          <code>URI</code> for the connected Interactive service host
     * @param   code
     *          HTTP status code returned from the connection handshake
     * @param   message
     *          HTTP status message returned from the connection handshake
     *
     * @see     com.mixer.interactive.ws.InteractiveWebSocketClient#onOpen(ServerHandshake)
     *
     * @since   1.0.0
     */
    public ConnectionOpenEvent(Number projectVersionID, URI interactiveHostURI, Number code, String message) {
        super(projectVersionID, interactiveHostURI);
        this.code = code;
        this.message = message;
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
}
