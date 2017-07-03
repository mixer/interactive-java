package com.mixer.interactive.exception;

import com.mixer.interactive.util.EndpointUtil;

/**
 * Checked exception thrown when no Interactive hosts are found when querying the host discovery endpoint.
 *
 * @author      Microsoft Corporation
 *
 * @see         EndpointUtil
 *
 * @since       1.0.0
 */
public class InteractiveNoHostsFoundException extends InteractiveException {

    /**
     * Initializes a new <code>InteractiveNoHostsFoundException</code>
     *
     * @since   1.0.0
     */
    public InteractiveNoHostsFoundException() {
        super("No hosts for the Interactive service could be found");
    }
}
