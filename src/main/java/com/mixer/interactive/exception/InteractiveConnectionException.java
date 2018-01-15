package com.mixer.interactive.exception;

import java.net.URI;

/**
 * Checked exception thrown when a websocket connection to the Mixer Interactive service fails to connect.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.1.0
 */
public class InteractiveConnectionException extends InteractiveException {

    /**
     * Error code returned when the websocket connection closed
     */
    private int errorCode;

    /**
     * Reason returned when the websocket connection closed
     */
    private String reason;

    /**
     * Initializes a new <code>InteractiveConnectionException</code>.
     *
     * @param   message
     *          Exception message
     *
     * @since   2.1.0
     */
    public InteractiveConnectionException(String message) {
        super(message);
    }

    /**
     * Initializes a new <code>InteractiveConnectionException</code>.
     *
     * @param   errorCode
     *          Error code returned when the websocket connection closed
     * @param   reason
     *          Reason returned when the websocket connection closed
     *
     * @since   2.1.0
     */
    public InteractiveConnectionException(URI host, int errorCode, String reason) {
        this(String.format("Unable to connect to Interactive host '%s' (reason: %s, error code: %s)", host, reason, errorCode));
        this.errorCode = errorCode;
        this.reason = reason;
    }

    /**
     * Returns the error code returned when the websocket connection closed.
     *
     * @return  Error code returned when the websocket connection closed
     *
     * @since   2.1.0
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the reason returned when the websocket connection closed.
     *
     * @return  Reason returned when the websocket connection closed
     *
     * @since   2.1.0
     */
    public String getReason() {
        return reason;
    }
}
