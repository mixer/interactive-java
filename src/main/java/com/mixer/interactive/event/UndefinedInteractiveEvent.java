package com.mixer.interactive.event;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>A <code>UndefinedInteractiveEvent</code> exists for cases where a method request is received from the Interactive
 * service, but there does not yet exist a clearly defined event for it within the client. This event contains a
 * <code>Map</code> containing name/value pairs representing the raw parameters that were passed from the service.</p>
 *
 * <p>Developers can subscribe to these events and manually inspect the raw parameters in order to parse out data as
 * needed.</p>
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class UndefinedInteractiveEvent extends InteractiveEvent {

    /**
     * The raw parameters for the undefined event
     */
    private final Map<String, JsonElement> parameters = new HashMap<>();

    /**
     * Initializes a new <code>UndefinedInteractiveEvent</code>.
     *
     * @param   parameters
     *          A <code>Map</code> containing the raw parameters for the undefined event
     *
     * @since   1.0.0
     */
    public UndefinedInteractiveEvent(Map<String, JsonElement> parameters) {
        this.parameters.putAll(parameters);
    }

    /**
     * Returns a <code>Map</code> containing raw parameters for the undefined event.
     *
     * @return  A <code>Map</code> containing raw parameters for the undefined event
     *
     * @since   1.0.0
     */
    public Map<String, JsonElement> getParameters() {
        return parameters;
    }
}
