package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.event.control.input.*;
import com.mixer.interactive.resources.control.InteractiveControlInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOG = LogManager.getLogger();

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public ControlInputEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        if (!json.isJsonObject()) {
            LOG.fatal("Unable to parse an ControlInputEvent");
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
            case "keydown": {
                return new ControlKeyDownEvent(participantID, transactionID, controlInput);
            }
            case "keyup": {
                return new ControlKeyUpEvent(participantID, transactionID, controlInput);
            }
            case "move": {
                return new ControlMoveInputEvent(participantID, transactionID, controlInput);
            }
            default:
                return new ControlInputEvent(participantID, transactionID, controlInput);
        }
    }
}
