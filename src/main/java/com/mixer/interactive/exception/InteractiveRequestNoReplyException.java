package com.mixer.interactive.exception;

import com.mixer.interactive.protocol.MethodPacket;

/**
 * Checked exception thrown when no reply to a method request is received from the Interactive service before the
 * request timed out.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveRequestNoReplyException extends InteractiveException {

    /**
     * <code>MethodPacket</code> representing the request that was sent by the client to the Interactive service
     */
    private final transient MethodPacket request;

    /**
     * Initializes a new <code>InteractiveRequestNoReplyException</code>.
     *
     * @param   request
     *          <code>MethodPacket</code> representing the request that was sent by the client to the
     *          Interactive service
     *
     * @since   1.0.0
     */
    public InteractiveRequestNoReplyException(MethodPacket request) {
        this(String.format("No reply was received for request id=%s before the request timed out", request.getPacketID()), request);
    }

    /**
     * Initializes a new <code>InteractiveRequestNoReplyException</code>.
     *
     * @param   message
     *          Exception message
     * @param   request
     *          <code>MethodPacket</code> representing the request that was sent by the client to the
     *          Interactive service
     *
     * @since   1.0.0
     */
    public InteractiveRequestNoReplyException(String message, MethodPacket request) {
        super(message);
        this.request = request;
    }

    /**
     * Returns the <code>MethodPacket</code> representing the request that was sent by the client to the
     * Interactive service
     *
     * @return  <code>MethodPacket</code> representing the request that was sent by the client to the
     *          Interactive service
     *
     * @since   1.0.0
     */
    public MethodPacket getRequest() {
        return request;
    }
}
