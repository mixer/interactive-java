package com.mixer.interactive.protocol;

import com.google.gson.JsonElement;
import com.mixer.interactive.GameClient;

import java.lang.reflect.Type;

/**
 * A <code>ReplyPacket</code> represents a reply to a request to perform an action.
 *
 * @author      Microsoft Corporation
 *
 * @see         MethodPacket
 *
 * @since       1.0.0
 */
public class ReplyPacket extends InteractivePacket {

    /**
     * Unstructured result of the request (may be <code>null</code>)
     */
    private final JsonElement result;

    /**
     * <code>InteractiveError</code> that may be returned by the Interactive service (may be <code>null</code>)
     */
    private final InteractiveError error;

    /**
     * Initializes a new <code>ReplyPacket</code>.
     *
     * @param   id
     *          Numeric identifier for this packet (should match the method packet identifier that resulted in
     *          this reply)
     * @param   result
     *          Unstructured result of the request returned by the Interactive service
     * @param   error
     *          <code>InteractiveError</code> that may be returned by the Interactive service
     *
     * @since   1.0.0
     */
    public ReplyPacket(int id, JsonElement result, InteractiveError error) {
        super(id, "reply");
        this.result = result;
        this.error = error;
    }

    /**
     * Returns the result that was returned by the Interactive service for the request.
     *
     * @return  <code>true</code> if a result object was returned from the Interactive service,
     *          <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean hasResult() {
        return result != null;
    }

    /**
     * Returns the result that was returned by the Interactive service for the request.
     *
     * @return  The result that was returned by the Interactive service for the request
     *
     * @since   1.0.0
     */
    public JsonElement getResult() {
        return result;
    }

    /**
     * Returns the result of the request, parsed as an object of type <code>T</code>.
     *
     * @param   type
     *          Type of object to be parsed from reply
     * @param   <T>
     *          Class of object to be parsed from the reply
     *
     * @return  The result of the request, parsed as an object of type <code>T</code>
     *
     * @since   1.0.0
     */
    public <T> T getResultAs(Type type) {
        return GameClient.GSON.fromJson(result, type);
    }

    /**
     * Returns the result of the request, parsed as an object of type <code>T</code>.
     *
     * @param   clazz
     *          Class of object to be parsed from the reply
     * @param   <T>
     *          Class of object to be parsed from the reply
     *
     * @return  The result of the request, parsed as an object of type <code>T</code>
     *
     * @since   1.0.0
     */
    public <T> T getResultAs(Class<T> clazz) {
        return GameClient.GSON.fromJson(result, clazz);
    }

    /**
     * Returns <code>true</code> if there was an error, <code>false</code> otherwise.
     *
     * @return  <code>true</code> if there was an error, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean hasError() {
        return error != null;
    }

    /**
     * Returns the <code>InteractiveError</code> returned from the Interactive service if there was an error.
     *
     * @return  The <code>InteractiveError</code> returned from the Interactive service if there was an error
     *
     * @since   1.0.0
     */
    public InteractiveError getError() {
        return error;
    }
}
