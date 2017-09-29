package com.mixer.interactive.gson;

import com.google.gson.*;
import com.mixer.interactive.event.control.ControlDeleteEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * De-serializes a <code>ControlDeleteEvent</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.0.0
 */
public class ControlDeleteEventAdapter implements JsonDeserializer<ControlDeleteEvent> {

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
    public ControlDeleteEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!json.isJsonObject()) {
            LOG.fatal("Unable to parse an ControlDeleteEvent");
            throw new JsonParseException("Unable to parse an ControlDeleteEvent");
        }

        JsonObject jsonObject = json.getAsJsonObject();
        String sceneID = jsonObject.get("sceneID").getAsString();
        Set<String> controlIds = new HashSet<>();
        JsonArray controlsArray = jsonObject.getAsJsonArray("controls");
        for (JsonElement jsonElement : controlsArray) {
            controlIds.add(((JsonObject) jsonElement).get("controlID").getAsString());
        }

        return new ControlDeleteEvent(sceneID, controlIds);
    }
}
