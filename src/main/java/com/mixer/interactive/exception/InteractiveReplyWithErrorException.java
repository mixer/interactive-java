package com.mixer.interactive.exception;

import com.mixer.interactive.protocol.InteractiveError;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.protocol.ReplyPacket;

/**
 * Checked exception thrown when an <code>InteractiveError</code> is returned in the reply sent by the Interactive
 * service in response to a method request.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveError
 * @see         ReplyPacket
 *
 * @since       1.0.0
 */
public class InteractiveReplyWithErrorException extends InteractiveException {

    /**
     * <code>MethodPacket</code> representing the request that was sent by the client to the Interactive service
     */
    private final transient MethodPacket request;

    /**
     * <code>InteractiveError</code> that was returned by the Interactive service
     */
    private final transient InteractiveError error;

    /**
     * Initializes a new <code>InteractiveReplyWithErrorException</code>.
     *
     * @param   request
     *          <code>MethodPacket</code> representing the request that was sent by the client to the
     *          Interactive service
     * @param   error
     *          <code>InteractiveError</code> that was returned by the Interactive service
     *
     * @since   1.0.0
     */
    public InteractiveReplyWithErrorException(MethodPacket request, InteractiveError error) {
        super(String.format("%s (error code: %s, path to error: '%s')", error.getErrorMessage(), error.getErrorCode(), error.getPath()));
        this.request = request;
        this.error = error;
    }

    /**
     * Returns the <code>MethodPacket</code> representing the request that was sent by the client to the
     * Interactive service.
     *
     * @return  <code>MethodPacket</code> representing the request that was sent by the client to the
     *          Interactive service
     *
     * @since   1.0.0
     */
    public MethodPacket getRequest() {
        return request;
    }

    /**
     * Returns the <code>InteractiveError</code> that was returned by the Interactive service.
     *
     * @return  <code>InteractiveError</code> that was returned by the Interactive service
     *
     * @since   1.0.0
     */
    public InteractiveError getError() {
        return error;
    }
}
