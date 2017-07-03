package com.mixer.interactive.exception;

/**
 * The class <code>InteractiveException</code> is the superclass of all <code>Exception</code >classes relating to
 * interactions with the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveException extends Exception {

    /**
     * Initializes a new <code>InteractiveException</code>.
     *
     * @since   1.0.0
     */
    public InteractiveException() {
        super();
    }

    /**
     * Initializes a new <code>InteractiveException</code>.
     *
     * @param   message
     *          Exception message
     *
     * @since   1.0.0
     */
    public InteractiveException(String message) {
        super(message);
    }
}
