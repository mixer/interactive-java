package com.mixer.interactive.event.connection;

import java.net.URI;

/**
 * Interactive event posted when there is an error involving the websocket connection to the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ConnectionErrorEvent extends ConnectionEvent {

    /**
     * The <code>Exception</code> returned from the <code>InteractiveWebSocketClient</code>
     */
    public final Exception exception;

    /**
     * Initializes a new <code>ConnectionErrorEvent</code>.
     *
     * @param   projectVersionID
     *          The project version ID for the Interactive integration
     * @param   interactiveHostURI
     *          <code>URI</code> for the connected Interactive service host
     * @param   exception
     *          The <code>Exception</code> returned from the <code>InteractiveWebSocketClient</code>
     *
     * @see     com.mixer.interactive.ws.InteractiveWebSocketClient#onError(Exception)
     *
     * @since   1.0.0
     */
    public ConnectionErrorEvent(Number projectVersionID, URI interactiveHostURI, Exception exception) {
        super(projectVersionID, interactiveHostURI);
        this.exception = exception;
    }

    /**
     * Returns the <code>Exception</code> returned from the <code>InteractiveWebSocketClient</code>.
     *
     * @return  The <code>Exception</code> returned from the <code>InteractiveWebSocketClient</code>
     *
     * @since   1.0.0
     */
    public Exception getException() {
        return exception;
    }
}
