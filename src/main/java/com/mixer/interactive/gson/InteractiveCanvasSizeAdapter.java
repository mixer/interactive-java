package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.resources.control.InteractiveCanvasSize;

import java.lang.reflect.Type;

/**
 * Serializes/de-serializes <code>InteractiveCanvasSize</code> from/to their appropriate enum value.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveCanvasSize
 *
 * @since       1.0.0
 */
public class InteractiveCanvasSizeAdapter implements JsonSerializer<InteractiveCanvasSize>, JsonDeserializer<InteractiveCanvasSize> {

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveCanvasSize deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return json.isJsonPrimitive() ? InteractiveCanvasSize.from(json.getAsString()) : null;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public JsonElement serialize(InteractiveCanvasSize src, Type typeOfSrc, JsonSerializationContext context) {
        return src != null ? new JsonPrimitive(src.toString()) : JsonNull.INSTANCE;
    }
}
