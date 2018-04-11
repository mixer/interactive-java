package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.resources.control.*;

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
            switch (((JsonObject) json).get("kind").getAsString()) {
                case "button":
                    return context.deserialize(json, ButtonControl.class);
                case "joystick":
                    return context.deserialize(json, JoystickControl.class);
                case "label":
                    return context.deserialize(json, LabelControl.class);
                case "textbox":
                    return context.deserialize(json, TextboxControl.class);
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
        else if (src instanceof LabelControl) {
            return context.serialize(src, LabelControl.class);
        }
        else if (src instanceof TextboxControl) {
            return context.serialize(src, TextboxControl.class);
        }
        else {
            return context.serialize(src);
        }
    }
}
