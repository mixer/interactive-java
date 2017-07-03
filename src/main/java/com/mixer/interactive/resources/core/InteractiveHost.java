package com.mixer.interactive.resources.core;

import com.mixer.interactive.util.EndpointUtil;

import java.net.URI;

/**
 * A <code>InteractiveHost</code> represents a host running on the Interactive service that a client can connect to.
 *
 * @author      Microsoft Corporation
 *
 * @see         EndpointUtil
 *
 * @since       1.0.0
 */
public class InteractiveHost {

    /**
     * The <code>URI</code> address for the <code>InteractiveHost</code>
     */
    private final URI address;

    /**
     * Initializes a new <code>InteractiveHost</code>.
     *
     * @param   address
     *          The <code>URI</code> address for the <code>InteractiveHost</code>
     *
     * @since   1.0.0
     */
    public InteractiveHost(URI address) {
        this.address = address;
    }

    /**
     * Returns the <code>URI</code> address for the <code>InteractiveHost</code>.
     *
     * @return  The <code>URI</code> address for the <code>InteractiveHost</code>
     *
     * @since   1.0.0
     */
    public URI getAddress() {
        return address;
    }
}
