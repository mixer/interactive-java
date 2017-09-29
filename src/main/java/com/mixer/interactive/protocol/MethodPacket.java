package com.mixer.interactive.protocol;

import com.google.gson.JsonElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A <code>MethodPacket</code> represents a request for the recipient (which could either be the Interactive service
 * or a client) to perform an action. The action varies depending on the <code>InteractiveMethod</code> that was the
 * subject of the request.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveMethod
 * @see         ReplyPacket
 *
 * @since       1.0.0
 */
public class MethodPacket extends InteractivePacket {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * The name of the method to call on the Interactive service
     */
    private final String method;

    /**
     * An object of named arguments to pass as parameters to the Interactive service
     */
    private final JsonElement params;

    /**
     * <code>true</code> if this request can be discarded (a reply is not required), <code>false</code> otherwise
     */
    private final boolean discard;

    /**
     * Initializes a new <code>MethodPacket</code>.
     *
     * @param   id
     *          Numeric identifier for this packet
     * @param   method
     *          The name of the method to call on the Interactive service
     * @param   params
     *          An object of named arguments to pass as parameters to the Interactive service
     *
     * @since   1.0.0
     */
    public MethodPacket(int id, InteractiveMethod method, JsonElement params) {
        this(id, method, params, null);
    }

    /**
     * Initializes a new <code>MethodPacket</code>.
     *
     * @param   id
     *          Numeric identifier for this packet
     * @param   method
     *          The name of the method to call on the Interactive service
     * @param   params
     *          An object of named arguments to pass as parameters to the Interactive service
     *
     * @since   1.0.0
     */
    public MethodPacket(int id, String method, JsonElement params) {
        this(id, method, params, null);
    }

    /**
     * Initializes a new <code>MethodPacket</code>.
     *
     * @param   id
     *          Numeric identifier for this packet
     * @param   method
     *          The name of the method to call on the Interactive service
     * @param   params
     *          An object of named arguments to pass as parameters to the Interactive service
     * @param   discard
     *          <code>true</code> if this request can be discarded (a reply is not required), <code>false</code>
     *          otherwise
     *
     * @since   1.0.0
     */
    public MethodPacket(int id, InteractiveMethod method, JsonElement params, Boolean discard) {
        this(id, method.toString(), params, discard);
    }

    /**
     * Initializes a new <code>MethodPacket</code>.
     *
     * @param   id
     *          Numeric identifier for this packet
     * @param   method
     *          The name of the method to call on the Interactive service
     * @param   params
     *          An object of named arguments to pass as parameters to the Interactive service
     * @param   discard
     *          <code>true</code> if this request can be discarded (a reply is not required), <code>false</code>
     *          otherwise
     *
     * @since   1.0.0
     */
    public MethodPacket(int id, String method, JsonElement params, Boolean discard) {
        super(id, "method");

        if (method == null) {
            LOG.fatal("Method name must not be null");
            throw new IllegalArgumentException("Method name must not be null");
        }

        this.method = method;
        this.params = params;
        this.discard = discard != null ? discard : false;
    }

    /**
     * Retrieves the <code>InteractiveMethod</code> to call on the Interactive service.
     *
     * @return  The <code>InteractiveMethod</code> to call on the Interactive service
     *
     * @since   1.0.0
     */
    public InteractiveMethod getMethod() {
        return InteractiveMethod.from(method);
    }

    /**
     * Retrieves the method name to call on the Interactive service.
     *
     * @return  The method name to call on the Interactive service
     *
     * @since   1.0.0
     */
    public String getMethodName() {
        return method;
    }

    /**
     * Retrieves the parameters for the request.
     *
     * @return  An object of named arguments to pass as parameters to the Interactive service
     *
     * @since   1.0.0
     */
    public JsonElement getRequestParameters() {
        return params;
    }

    /**
     * Returns whether or not this request can be discarded (a reply is not required).
     *
     * @return  <code>true</code> if this request can be discarded (a reply is not required), <code>false</code>
     *          otherwise
     *
     * @since   1.0.0
     */
    public boolean getDiscard() {
        return discard;
    }
}
