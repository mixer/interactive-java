package com.mixer.interactive.test.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.MethodPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for connecting a fake participant to the local running Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class FakeParticipantClient extends WebSocketClient {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Random number generator
     */
    private static final Random RANDOM = new Random();

    /**
     * The <code>FakeParticipant</code> for this connection
     */
    private FakeParticipant fakeParticipant;

    /**
     * Initializes a new <code>FakeParticipantClient</code>.
     *
     * @since   1.0.0
     */
    public FakeParticipantClient() {
        this(0, FakeParticipant.generate());
    }

    /**
     * Initializes a new <code>FakeParticipantClient</code>.
     *
     * @param   channelID
     *          Channel id to connect to
     * @since   1.0.0
     */
    public FakeParticipantClient(int channelID, FakeParticipant fakeParticipant) {
        super(URI.create("ws://127.0.0.1:3000/participant?channel=" + channelID), new Draft_17(), ImmutableMap.<String, String>builder()
                .put("X-Protocol-Version", "2.0")
                .put("X-Auth-User", fakeParticipant.toJson())
                .build(), (int) TimeUnit.SECONDS.toMillis(15));
        this.fakeParticipant = fakeParticipant;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOG.debug("Successfully connected a fake participant ('{}') to '{}'", fakeParticipant, getURI());
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public void onMessage(String message) {
        // NO-OP
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOG.debug("Fake participant '{}' disconnected (code: {}, reason: {})", fakeParticipant, code, reason);
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public void onError(Exception ex) {
        // NO-OP
    }

    /**
     * Supplies fake participant input to the connected game client.
     *
     * @param   participantID
     *          ID of the participant sending the fake input
     * @param   transactionID
     *          Transaction ID for fake input involving a Spark cost. If <code>null</code> it is not sent
     * @param   controlID
     *          ID for an Interacive control that is receiving input
     * @param   event
     *          Type of input event
     * @param   inputProperties
     *          Map of input properties to pass along in the fake input
     *
     * @since   1.0.0
     */
    public void giveInput(String participantID, String transactionID, String controlID, String event, Map<String, JsonElement> inputProperties) {
        JsonObject params = new JsonObject();
        params.addProperty("participantID", participantID);
        if (transactionID != null) {
            params.addProperty("transactionID", transactionID);
        }
        JsonObject input = new JsonObject();
        input.addProperty("controlID", controlID);
        input.addProperty("event", event);
        for (Map.Entry<String, JsonElement> entry : inputProperties.entrySet()) {
            input.add(entry.getKey(), entry.getValue());
        }
        params.add("input", input);
        MethodPacket requestPacket = new MethodPacket(RANDOM.nextInt(), InteractiveMethod.GIVE_INPUT, params, true);
        send(GameClient.GSON.toJson(requestPacket));
    }
}
