package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.resources.control.InteractiveControlType;

import java.lang.reflect.Type;

/**
 * Serializes/de-serializes <code>InteractiveControlType</code> from/to their appropriate enum value.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveControlType
 *
 * @since       1.0.0
 */
public class InteractiveControlTypeAdapter implements JsonSerializer<InteractiveControlType>, JsonDeserializer<InteractiveControlType> {

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveControlType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return json.isJsonPrimitive() ? InteractiveControlType.from(json.getAsString()) : null;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public JsonElement serialize(InteractiveControlType src, Type typeOfSrc, JsonSerializationContext context) {
        return src != null ? new JsonPrimitive(src.toString()) : JsonNull.INSTANCE;
    }
}
