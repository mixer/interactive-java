package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.protocol.InteractiveMethod;

import java.lang.reflect.Type;

/**
 * Serializes/de-serializes <code>InteractiveMethods</code> from/to their appropriate enum value.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveMethod
 *
 * @since       1.0.0
 */
public class InteractiveMethodAdapter implements JsonSerializer<InteractiveMethod>, JsonDeserializer<InteractiveMethod> {

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveMethod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json.isJsonPrimitive()) {
            return InteractiveMethod.from(json.getAsString());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public JsonElement serialize(InteractiveMethod src, Type typeOfSrc, JsonSerializationContext context) {
        if (src != null) {
            return new JsonPrimitive(src.toString());
        }
        return JsonNull.INSTANCE;
    }
}
