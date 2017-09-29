package com.mixer.interactive.resources.control;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * A <code>InteractiveControlInput</code> represents an polymorphic input event (such as a <code>mousedown</code> event
 * for a button control, or a <code>move</code> event for a joystick control).
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveControlInput {

    /**
     * Identifier for the control that received input
     */
    private final String controlID;

    /**
     * Type of input event
     */
    private final String event;

    /**
     * The parameters for the control input
     */
    private final Map<String, JsonElement> input = new HashMap<>();

    /**
     * Initializes a new <code>InteractiveControlInput</code>.
     *
     * @param   controlID
     *          Identifier for the control that received input
     * @param   event
     *          Type of input event
     * @param   input
     *          A <code>Map</code> of raw input parameters
     *
     * @since   1.0.0
     */
    public InteractiveControlInput(String controlID, String event, Map<String, JsonElement> input) {
        this.controlID = controlID;
        this.event = event;
        if (input != null) {
            for (Map.Entry<String, JsonElement> entry : input.entrySet()) {
                this.input.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Returns the identifier for the control that received input.
     *
     * @return  Identifier for the control that received input
     *
     * @since   1.0.0
     */
    public String getControlID() {
        return controlID;
    }

    /**
     * Returns the type of input event.
     *
     * @return  Type of input event
     *
     * @since   1.0.0
     */
    public String getEvent() {
        return event;
    }

    /**
     * Returns the raw input parameters.
     *
     * @return  A <code>Map</code> of raw input parameters
     *
     * @since   1.0.0
     */
    public Map<String, JsonElement> getRawInput() {
        return input;
    }
}
