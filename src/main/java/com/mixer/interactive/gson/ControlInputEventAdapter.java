package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.event.control.input.ControlInputEvent;
import com.mixer.interactive.event.control.input.ControlMouseDownInputEvent;
import com.mixer.interactive.event.control.input.ControlMouseUpInputEvent;
import com.mixer.interactive.event.control.input.ControlMoveInputEvent;
import com.mixer.interactive.resources.control.InteractiveControlInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * De-serializes <code>ControlInputEvents</code> into their appropriate subclasses.
 *
 * @author      Microsoft Corporation
 *
 * @see         ControlMouseDownInputEvent
 * @see         ControlMouseUpInputEvent
 * @see         ControlMoveInputEvent
 *
 * @since       1.0.0
 */
public class ControlInputEventAdapter implements JsonDeserializer<ControlInputEvent> {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ControlInputEventAdapter.class);

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public ControlInputEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        if (!json.isJsonObject()) {
            LOG.error("Unable to parse an ControlInputEvent");
            throw new JsonParseException("Unable to parse an ControlInputEvent");
        }

        JsonObject jsonObject = json.getAsJsonObject();

        String participantID = jsonObject.get("participantID").getAsString();
        String transactionID = null;
        if (jsonObject.has("transactionID")) {
            transactionID = jsonObject.get("transactionID").getAsString();
        }
        InteractiveControlInput controlInput = context.deserialize(jsonObject.get("input"), InteractiveControlInput.class);

        switch (controlInput.getEvent()) {
            case "mousedown": {
                return new ControlMouseDownInputEvent(participantID, transactionID, controlInput);
            }
            case "mouseup": {
                return new ControlMouseUpInputEvent(participantID, transactionID, controlInput);
            }
            case "move": {
                return new ControlMoveInputEvent(participantID, transactionID, controlInput);
            }
            default:
                return new ControlInputEvent(participantID, transactionID, controlInput);
        }
    }
}
