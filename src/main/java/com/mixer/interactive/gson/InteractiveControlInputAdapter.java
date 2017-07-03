package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.resources.control.InteractiveControlInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * De-serializes <code>InteractiveControlInput</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveControlInputAdapter implements JsonDeserializer<InteractiveControlInput> {

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
    public InteractiveControlInput deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (!json.isJsonObject()) {
            LOG.fatal("Unable to parse an InteractiveControlInput");
            throw new JsonParseException("Unable to parse an InteractiveControlInput");
        }

        JsonObject jsonObject = json.getAsJsonObject();
        String controlID = jsonObject.get("controlID").getAsString();
        String event = jsonObject.get("event").getAsString();

        Map<String, JsonElement> input = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            input.put(entry.getKey(), entry.getValue());
        }

        return new InteractiveControlInput(controlID, event, input);
    }
}
