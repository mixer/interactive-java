package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.event.UndefinedInteractiveEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * De-serializes <code>UndefinedInteractiveEvent</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class UndefinedInteractiveEventAdapter implements JsonDeserializer<UndefinedInteractiveEvent> {

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
    public UndefinedInteractiveEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (!json.isJsonObject()) {
            LOG.fatal("Unable to parse an UndefinedInteractiveEvent");
            throw new JsonParseException("Unable to parse an UndefinedInteractiveEvent");
        }

        JsonObject jsonObject = json.getAsJsonObject();
        Map<String, JsonElement> parameters = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            parameters.put(entry.getKey(), entry.getValue());
        }

        return new UndefinedInteractiveEvent(parameters);
    }
}
