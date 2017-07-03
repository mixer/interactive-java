package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.resources.control.ButtonControl;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.control.JoystickControl;

import java.lang.reflect.Type;

/**
 * De-serializes <code>InteractiveControls</code> into their appropriate subclasses.
 *
 * @author      Microsoft Corporation
 *
 * @see         ButtonControl
 * @see         JoystickControl
 *
 * @since       1.0.0
 */
public class InteractiveControlAdapter implements JsonSerializer<InteractiveControl>, JsonDeserializer<InteractiveControl> {

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveControl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json.isJsonObject() && ((JsonObject) json).has("kind")) {
            if (((JsonObject) json).get("kind").getAsString().equals("button")) {
                return context.deserialize(json, ButtonControl.class);
            }
            else if (((JsonObject) json).get("kind").getAsString().equals("joystick")) {
                return context.deserialize(json, JoystickControl.class);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public JsonElement serialize(InteractiveControl src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }

        if (src instanceof ButtonControl) {
            return context.serialize(src, ButtonControl.class);
        }
        else if (src instanceof JoystickControl) {
            return context.serialize(src, JoystickControl.class);
        }
        else {
            return context.serialize(src);
        }
    }
}
