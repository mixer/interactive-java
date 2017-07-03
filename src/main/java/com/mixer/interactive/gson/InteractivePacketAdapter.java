package com.mixer.interactive.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mixer.interactive.protocol.InteractivePacket;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.protocol.ReplyPacket;

import java.lang.reflect.Type;

/**
 * De-serializes <code>InteractivePackets</code> into their appropriate subclasses.
 *
 * @author      Microsoft Corporation
 *
 * @see         MethodPacket
 * @see         ReplyPacket
 *
 * @since       1.0.0
 */
public class InteractivePacketAdapter implements JsonDeserializer<InteractivePacket> {

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public InteractivePacket deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json.isJsonObject() && ((JsonObject) json).has("type")) {
            if (((JsonObject) json).get("type").getAsString().equals("method")) {
                return context.deserialize(json, MethodPacket.class);
            }
            else if (((JsonObject) json).get("type").getAsString().equals("reply")) {
                return context.deserialize(json, ReplyPacket.class);
            }
        }
        return null;
    }
}
