package com.mixer.interactive.test.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveConnectionException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.InteractivePacket;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket client for connecting a test participants to Interactive.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.1.0
 */
public class InteractiveTestParticipantClient extends WebSocketClient {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Random number generator
     */
    private static final Random RANDOM = new Random();

    /**
     * Json parser for determining if a received message is an array of Json objects or a single object
     */
    private static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractivePacket</code>.
     */
    private static final Type INTERACTIVE_PACKET_SET_TYPE = new TypeToken<Set<InteractivePacket>>(){}.getType();

    /**
     * A <code>CompletableFuture</code> promise holding the result of a connection attempt using this websocket client
     */
    private CompletableFuture<Boolean> connectionPromise;

    /**
     * The user id for the participant that owns this websocket client
     */
    private final int userId;

    /**
     * Constructs a new websocket client to be used for connecting a participant to Interactive.
     *
     * @param   socketUri
     *          <code>URI</code> to be used to connect this client to a specific channel that is used in Interactive
     *          testing
     * @param   userId
     *          User id for the participant
     *
     * @since   2.1.0
     */
    public InteractiveTestParticipantClient(URI socketUri, int userId) {
        super(socketUri);
        this.userId = userId;
    }

    /**
     * Constructs a new websocket client to be used for connecting a participant to Interactive.
     *
     * @param   participant
     *          Participant to be used when connecting
     * @param   token
     *          Authentication token
     *
     * @since   2.1.0
     */
    public InteractiveTestParticipantClient(InteractiveParticipant participant, String token) {
        super(TestUtils.INTERACTIVE_PARTICIPANT_URI, new Draft_6455(), ImmutableMap.<String, String>builder()
                .put("X-Protocol-Version", "2.0")
                .put("X-Auth-User", GameClient.GSON.toJson(participant))
                .put("Authorization", TestUtils.OAUTH_BEARER_TOKEN.startsWith("XBL3.0") ? token: "Bearer " + token)
                .build(), (int) TimeUnit.SECONDS.toMillis(15));
        this.userId = participant.getUserID();
    }

    /**
     * Returns the user id of the participant that owns this websocket client.
     *
     * @return  The user id of the participant that owns this websocket client
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Returns the <code>CompletableFuture</code> promise holding the result of a connection attempt using this
     * websocket client.
     *
     * @return  The <code>CompletableFuture</code> promise holding the result of a connection attempt using this
     *          websocket client.
     *
     * @since   2.1.0
     */
    public CompletableFuture<Boolean> getConnectionPromise() {
        return connectionPromise;
    }

    /**
     * Sets the <code>CompletableFuture</code> promise holding the result of a connection attempt using this
     * websocket client.
     *
     * @param   connectionPromise
     *          The <code>CompletableFuture</code> promise holding the result of a connection attempt using this
     *          websocket client.
     *
     * @return  The <code>CompletableFuture</code> promise holding the result of a connection attempt using this
     *          websocket client.
     *
     * @since   2.1.0
     */
    public CompletableFuture<Boolean> setConnectionPromise(CompletableFuture<Boolean> connectionPromise) {
        this.connectionPromise = connectionPromise;
        return this.connectionPromise;
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
        LOG.debug(String.format("PARTICIPANT[%s] - SEND[RAW]: %s", userId, GameClient.GSON.toJson(requestPacket)));
        send(GameClient.GSON.toJson(requestPacket));
    }

    /**
     * {@inheritDoc}
     *
     * @since   2.1.0
     */
    @Override
    public void send(String message) {
        LOG.debug(String.format("TEST_PARTICIPANT[%s] - SEND[RAW]: %s", userId, message));
        super.send(message);
    }

    /**
     * {@inheritDoc}
     *
     * @since   2.1.0
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOG.info(String.format("Connected test participant '%s' to '%s'", userId, getURI()));
    }

    /**
     * {@inheritDoc}
     *
     * @since   2.1.0
     */
    @Override
    public void onMessage(String message) {
        LOG.debug(String.format("TEST_PARTICIPANT[%s] - RCVD[TEXT]: %s'", userId, message));

        // Parse packets from the message
        List<InteractivePacket> packets = new ArrayList<>();
        JsonElement jsonObject = JSON_PARSER.parse(message);
        if (jsonObject.isJsonArray()) {
            Collections.addAll(packets, GameClient.GSON.fromJson(jsonObject, INTERACTIVE_PACKET_SET_TYPE));
        }
        else {
            Collections.addAll(packets, GameClient.GSON.fromJson(jsonObject, InteractivePacket.class));
        }

        // Process all parsed packets
        processReceivedPackets(packets);
    }

    /**
     * {@inheritDoc})
     *
     * @since   2.1.0
     */
    @Override
    public void onClose(int code, String reason, boolean closedRemotely) {
        LOG.debug(String.format("Test participant '%s' disconnected (code: %s, reason: %s)", userId, code, reason));
        if (connectionPromise != null && !connectionPromise.isDone()) {
            connectionPromise.completeExceptionally(new InteractiveConnectionException(getURI(), code, reason));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since   2.1.0
     */
    @Override
    public void onError(Exception ex) {
        // NO-OP
    }

    /**
     * Processes all received <code>InteractivePackets</code>.
     *
     * @param   receivedPackets
     *          <code>Collection</code> of <code>InteractivePacket</code> to be processed
     *
     * @since   2.1.0
     */
    private void processReceivedPackets(List<InteractivePacket> receivedPackets) {
        if (receivedPackets.isEmpty()) {
            return;
        }

        for (InteractivePacket packet : receivedPackets) {
            if (packet instanceof MethodPacket && ((MethodPacket) packet).getMethod() != null && ((MethodPacket) packet).getMethod() == InteractiveMethod.HELLO) {
                if (connectionPromise != null && !connectionPromise.isDone()) {
                    connectionPromise.complete(true);
                }
            }
        }
    }
}
