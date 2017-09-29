package com.mixer.interactive.protocol;

/**
 * A <code>InteractiveError</code> represents the error object that could be returned as part of a
 * <code>ReplyPacket</code>. In the event an error is returned, a <code>InteractiveReplyWithErrorException</code> is
 * thrown, which will contain this object.
 *
 * @author      Microsoft Corporation
 *
 * @see         com.mixer.interactive.exception.InteractiveReplyWithErrorException
 *
 * @since       1.0.0
 */
public class InteractiveError {

    /**
     * Interactive service error code
     */
    private final int code;

    /**
     * Error message
     */
    private final String message;

    /**
     * Path to the property which caused the error
     */
    private final String path;

    /**
     * Initializes a new <code>InteractiveError</code>.
     *
     * @param   code
     *          Interactive service error code
     * @param   message
     *          Error message
     * @param   path
     *          Path to the property which caused the error
     *
     * @since   1.0.0
     */
    public InteractiveError(int code, String message, String path) {
        this.code = code;
        this.message = message;
        this.path = path;
    }

    /**
     * Returns the Interactive service error code. Refer to the Interactive 2.0 protocol
     * specification for a list of all possible error codes the Interactive service may respond with.
     *
     * @return  The Interactive service error code
     *
     * @since   1.0.0
     */
    public int getErrorCode() {
        return code;
    }

    /**
     * Returns the error message.
     *
     * @return  The error message
     *
     * @since   1.0.0
     */
    public String getErrorMessage() {
        return message;
    }

    /**
     * Returns the path to the property which caused the error. This will be expressed in dot
     * notation relative to the reply params. This may return null if no path was provided by
     * the Interactive service for this error.
     *
     * @return  The path to the property which caused the error
     *
     * @since   1.0.0
     */
    public String getPath() {
        return path;
    }
}
