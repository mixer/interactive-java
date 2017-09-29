package com.mixer.interactive.test.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for connecting a fake participant to the local running Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class MockParticipantClient extends WebSocketClient {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Random number generator
     */
    private static final Random RANDOM = new Random();

    /**
     * The <code>InteractiveParticipant</code> for this connection
     */
    private InteractiveParticipant participant;

    /**
     * Initializes a new <code>MockParticipantClient</code>.
     *
     * @since   1.0.0
     */
    public MockParticipantClient() {
        this(generate());
    }

    /**
     * Initializes a new <code>MockParticipantClient</code>.
     *
     * @since   2.0.0
     */
    private MockParticipantClient(InteractiveParticipant participant) {
        super(TestUtils.INTERACTIVE_PARTICIPANT_URI, new Draft_6455(), ImmutableMap.<String, String>builder()
                .put("X-Protocol-Version", "2.0")
                .put("X-Auth-User", GameClient.GSON.toJson(participant))
                .put("Authorization", "Bearer " + TestUtils.OAUTH_BEARER_TOKEN)
                .build(), (int) TimeUnit.SECONDS.toMillis(15));
        this.participant = participant;
    }

    /**
     * Returns the participant associated with this client.
     *
     * @return  The <code>InteractiveParticipant</code> associated with this client
     *
     * @since   2.0.0
     */
    public InteractiveParticipant getParticipant() {
        return participant;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOG.debug(String.format("Successfully connected a mock participant ('%s') to '%s'", participant.getUsername(), getURI()));
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public void onMessage(String message) {
        LOG.debug(String.format("PARTICIPANT[%s] - RCVD[TEXT]: %s'", participant.getUserID(), message));
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOG.debug(String.format("Fake participant '%s' disconnected (code: %s, reason: %s)", participant.getUsername(), code, reason));
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
     * Supplies mock participant input to the connected game client.
     *
     * @param   controlID
     *          ID for an Interacive control that is receiving input
     * @param   event
     *          Type of input event
     *
     * @since   1.0.0
     */
    public void giveInput(String controlID, String event) {
        JsonObject params = new JsonObject();
        JsonObject input = new JsonObject();
        input.addProperty("controlID", controlID);
        input.addProperty("event", event);
        params.add("input", input);
        MethodPacket requestPacket = new MethodPacket(Math.abs(RANDOM.nextInt()), InteractiveMethod.GIVE_INPUT, params, true);
        LOG.debug(String.format("PARTICIPANT[%s] - SEND[RAW]: %s", participant.getUserID(), GameClient.GSON.toJson(requestPacket)));
        send(GameClient.GSON.toJson(requestPacket));
    }

    /**
     * Generates a new random fake participant.
     *
     * @return  A new fake participant
     *
     * @since   1.0.0
     */
    private static InteractiveParticipant generate() {
        int id = RANDOM.nextInt(100000);
        return new InteractiveParticipant("", RANDOM.nextInt(1000), "Participant" + id, RANDOM.nextInt(100), 0L,0L, false, "default");
    }
}
