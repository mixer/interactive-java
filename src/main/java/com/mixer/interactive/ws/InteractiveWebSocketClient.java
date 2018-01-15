package com.mixer.interactive.ws;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.event.InteractiveEvent;
import com.mixer.interactive.event.UndefinedInteractiveEvent;
import com.mixer.interactive.event.connection.ConnectionClosedEvent;
import com.mixer.interactive.event.connection.ConnectionErrorEvent;
import com.mixer.interactive.event.connection.ConnectionOpenEvent;
import com.mixer.interactive.event.control.ControlCreateEvent;
import com.mixer.interactive.event.control.ControlDeleteEvent;
import com.mixer.interactive.event.control.ControlUpdateEvent;
import com.mixer.interactive.event.control.input.ControlInputEvent;
import com.mixer.interactive.event.core.HelloEvent;
import com.mixer.interactive.event.core.MemoryWarningEvent;
import com.mixer.interactive.event.core.ReadyEvent;
import com.mixer.interactive.event.core.SetCompressionEvent;
import com.mixer.interactive.event.group.GroupCreateEvent;
import com.mixer.interactive.event.group.GroupDeleteEvent;
import com.mixer.interactive.event.group.GroupUpdateEvent;
import com.mixer.interactive.event.participant.ParticipantJoinEvent;
import com.mixer.interactive.event.participant.ParticipantLeaveEvent;
import com.mixer.interactive.event.participant.ParticipantUpdateEvent;
import com.mixer.interactive.event.scene.SceneCreateEvent;
import com.mixer.interactive.event.scene.SceneDeleteEvent;
import com.mixer.interactive.event.scene.SceneUpdateEvent;
import com.mixer.interactive.exception.InteractiveConnectionException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.InteractivePacket;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.protocol.ReplyPacket;
import com.mixer.interactive.resources.core.CompressionScheme;
import com.mixer.interactive.util.compression.CompressionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A websocket client designed specifically for use with the Interactive service. Messages received are posted to the
 * <code>GameClient</code> that owns this connection. Messages sent/received to/from the Interactive service are sent
 * in the current compression scheme set for the client.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveWebSocketClient extends WebSocketClient {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractivePacket</code>.
     */
    private static final Type INTERACTIVE_PACKET_SET_TYPE = new TypeToken<Set<InteractivePacket>>(){}.getType();

    /**
     * Json parser for determining if a received message is an array of Json objects or a single object
     */
    private static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * <code>ConcurrentMap</code> of waiting <code>CompletableFuture</code> promises and the IDs for the packets that
     * made the request
     */
    private final ConcurrentMap<Integer, CompletableFuture<ReplyPacket>> waitingFuturesMap = new ConcurrentSkipListMap<>();

    /**
     * The <code>GameClient</code> that owns this websocket client
     */
    private final GameClient gameClient;

    /**
     * The <code>CompressionScheme</code> this <code>InteractiveWebSocketClient</code> is using
     */
    private CompressionScheme compressionScheme = CompressionScheme.NONE;

    /**
     * The next available packet id
     */
    private AtomicInteger nextPacketId = new AtomicInteger(0);

    /**
     * The last packet sequence number seen from the Interactive service. Read and updated internally as packets are
     * sent and retrieved.
     */
    private AtomicInteger lastSequenceNumber = new AtomicInteger();

    /**
     * A <code>CompletableFuture</code> promise holding the result of a connection attempt using this websocket client
     */
    private CompletableFuture<Boolean> connectionPromise;

    /**
     * Initialize a new <code>InteractiveWebSocketClient</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this websocket client
     * @param   uri
     *          The <code>URI</code> address for the <code>InteractiveHost</code> to connect to
     * @param   token
     *          Authorization token. Can be either an OAuth Bearer token or a xtoken
     * @param   projectVersionId
     *          The project version ID for the Interactive integration to connect to
     *
     * @since   1.0.0
     */
    public InteractiveWebSocketClient(GameClient gameClient, URI uri, String token, Number projectVersionId) {
        this(gameClient, uri, ImmutableMap.<String, String>builder()
                .put("X-Protocol-Version", "2.0")
                .put("X-Interactive-Version", String.valueOf(projectVersionId))
                .put("Authorization", token.startsWith("XBL3.0") ? token : "Bearer " + token)
                .build());
    }

    /**
     * Initialize a new <code>InteractiveWebSocketClient</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this websocket client
     * @param   uri
     *          The <code>URI</code> address for the <code>InteractiveHost</code> to connect to
     * @param   token
     *          Authorization token. Can be either an OAuth Bearer token or a xtoken
     * @param   projectVersionId
     *          The project version ID for the Interactive integration to connect to
     * @param   shareCode
     *          The share code provided by the author of the Interactive integration
     *
     * @since   1.0.0
     */
    public InteractiveWebSocketClient(GameClient gameClient, URI uri, String token, Number projectVersionId, String shareCode) {
        this(gameClient, uri, ImmutableMap.<String, String>builder()
                .put("X-Protocol-Version", "2.0")
                .put("X-Interactive-Version", String.valueOf(projectVersionId))
                .put("X-Interactive-Sharecode", shareCode)
                .put("Authorization", token.startsWith("XBL3.0") ? token : "Bearer " + token)
                .build());
    }

    /**
     * Initialize a new <code>InteractiveWebSocketClient</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this websocket client
     * @param   uri
     *          The <code>URI</code> address for the <code>InteractiveHost</code> to connect to
     * @param   httpHeaders
     *          <code>Map</code> of HTTP headers
     *
     * @since   1.0.0
     */
    private InteractiveWebSocketClient(GameClient gameClient, URI uri, Map<String, String> httpHeaders) {
        super(uri, new Draft_6455(), httpHeaders, (int) TimeUnit.SECONDS.toMillis(15));
        this.gameClient = gameClient;
    }

    /**
     * Retrieves the <code>ConcurrentMap</code> of waiting <code>CompletableFuture</code> promises and the IDs for the
     * packets that made the request.
     *
     * @return  <code>ConcurrentMap</code> of waiting <code>CompletableFuture</code> promises and the IDs for the
     *          packets that made the request
     *
     * @since   2.0.0
     */
    public ConcurrentMap<Integer, CompletableFuture<ReplyPacket>> getWaitingFuturesMap() {
        return waitingFuturesMap;
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
     * Retrieves the next available packet id, then increments the internal counter to reflect that the previous id
     * has now been allocated.
     *
     * @return  The next available packet id
     *
     * @since   1.0.0
     */
    public int claimNextPacketId() {
        return nextPacketId.getAndIncrement();
    }

    /**
     * Returns the sequence number that the socket has last seen from the service.
     *
     * @return  The next last packet sequence number
     *
     * @since   1.0.0
     */
    public int getLastSequenceNumber() { return this.lastSequenceNumber.get(); }

    /**
     * Retrieves the <code>CompressionScheme</code> this <code>InteractiveWebSocketClient</code> is using.
     *
     * @return  The <code>CompressionScheme</code> this <code>InteractiveWebSocketClient</code> is using
     *
     * @since   1.0.0
     */
    public CompressionScheme getCompressionScheme() {
        return compressionScheme;
    }

    /**
     * Sets the <code>CompressionScheme</code> that this <code>InteractiveWebSocketClient</code> will use.
     *
     * @param   compressionScheme
     *          The compression scheme that this <code>InteractiveWebSocketClient</code> will use
     *
     * @since   1.0.0
     */
    public void setCompressionScheme(String compressionScheme) {
        setCompressionScheme(CompressionScheme.from(compressionScheme));
    }

    /**
     * Sets the <code>CompressionScheme</code> that this <code>InteractiveWebSocketClient</code> will use.
     *
     * @param   compressionScheme
     *          <code>CompressionScheme</code> that this <code>InteractiveWebSocketClient</code> will use
     *
     * @since   1.0.0
     */
    public void setCompressionScheme(CompressionScheme compressionScheme) {
        this.compressionScheme = compressionScheme;
    }

    /**
     * {@inheritDoc}
     *
     * @param   serverHandshake
     *          The handshake returned by the Interactive service
     *
     * @see     WebSocketClient#onOpen(ServerHandshake)
     *
     * @since   1.0.0
     */
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOG.info(String.format("Connected to Interactive integration (project version '%s') on host '%s'", gameClient.getProjectVersionId(), getURI()));
        gameClient.getEventBus().post(new ConnectionOpenEvent(gameClient.getProjectVersionId(), getURI(), serverHandshake.getHttpStatus(), serverHandshake.getHttpStatusMessage()));
    }

    /**
     * {@inheritDoc}
     *
     * @param   message
     *          The message to send to the Interactive service
     *
     * @see     WebSocketClient#send(String)
     *
     * @since   1.0.0
     */
    @Override
    public void send(String message) {
        LOG.debug(String.format("PROJECT_ID[%s] - SEND[RAW]: %s", gameClient.getProjectVersionId(), message));
        super.send(message);
    }

    /**
     * {@inheritDoc}
     *
     * @param   bytes
     *          <code>ByteBuffer</code> containing message received from the Interactive service (as an array of bytes)
     *
     * @see     WebSocketClient#onMessage(ByteBuffer)
     *
     * @since   1.0.0
     */
    @Override
    public void onMessage(ByteBuffer bytes) {
        LOG.debug(String.format("PROJECT_ID[%s] - RCVD[bytes]: '%s'", gameClient.getProjectVersionId(), Arrays.toString(bytes.array())));
        try {
            String message = CompressionUtil.decode(compressionScheme, bytes.array());
            LOG.debug(String.format("PROJECT_ID[%s] - RCVD[%s]: %s", gameClient.getProjectVersionId(), compressionScheme, message));
            onMessage(message);
        }
        catch (IOException e) {
            LOG.error(String.format("PROJECT_ID[%s] - RCVD[exception]: %s", gameClient.getProjectVersionId(), e.getMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param   message
     *          The message received from the Interactive service
     *
     * @see     WebSocketClient#onMessage(String)
     *
     * @since   1.0.0
     */
    @Override
    public void onMessage(String message) {
        LOG.debug(String.format("PROJECT_ID[%s] - RCVD[TEXT]: %s", gameClient.getProjectVersionId(), message));

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
     * {@inheritDoc}
     *
     * @param   code
     *          HTTP status code
     * @param   reason
     *          HTTP status reason
     * @param   closedRemotely
     *          <code>true</code> if the connection was closed remotely, <code>false</code> otherwise
     *
     * @see     WebSocketClient#onClose(int, String, boolean)
     *
     * @since   1.0.0
     */
    @Override
    public void onClose(int code, String reason, boolean closedRemotely) {
        LOG.info(String.format("Connection to the Interactive host '%s' closed (project version id: %s, code: %s, reason: '%s')", getURI(), gameClient.getProjectVersionId(), code, reason));
        if (connectionPromise != null && !connectionPromise.isDone()) {
            connectionPromise.completeExceptionally(new InteractiveConnectionException(getURI(), code, reason));
        }
        gameClient.getEventBus().post(new ConnectionClosedEvent(gameClient.getProjectVersionId(), getURI(), code, reason, closedRemotely));
    }

    /**
     * {@inheritDoc}
     *
     * @param   ex
     *          The <code>Exception</code> thrown when the <code>WebSocketClient</code> connection errored
     *
     * @see     WebSocketClient#onError(Exception)
     *
     * @since   1.0.0
     */
    @Override
    public void onError(Exception ex) {
        LOG.error("Connection to the Interactive service encountered an error", ex);
        gameClient.getEventBus().post(new ConnectionErrorEvent(gameClient.getProjectVersionId(), getURI(), ex));
    }

    /**
     * Processes all received <code>InteractivePacket</code>. If the packet is a <code>MethodPackets</code>) then the
     * <code>InteractiveEvent</code> (if any) is posted to the <code>GameClient</code>'s <code>EventBus</code>. If the
     * packet is a <code>ReplyPacket</code> then the promise waiting for it is fulfilled and released from the promises
     * map.
     *
     * @param   receivedPackets
     *          <code>Collection</code> of <code>InteractivePacket</code> to be processed
     *
     * @since   1.0.0
     */
    private void processReceivedPackets(List<InteractivePacket> receivedPackets) {
        if (receivedPackets.isEmpty()) {
            return;
        }

        receivedPackets.sort(Comparator.comparingInt(InteractivePacket::getSequenceNumber));

        for (InteractivePacket packet : receivedPackets) {
            if (packet instanceof MethodPacket) {
                InteractiveEvent interactiveEvent = getEventFromPacket((MethodPacket) packet);
                if (interactiveEvent != null) {
                    interactiveEvent.setRequestID(packet.getPacketID());
                    if (interactiveEvent instanceof SetCompressionEvent) {
                        SetCompressionEvent compressionEvent = (SetCompressionEvent) interactiveEvent;
                        if (!compressionEvent.getCompressionSchemes().isEmpty()) {
                            setCompressionScheme(compressionEvent.getCompressionSchemes().iterator().next());
                        }
                    }
                    gameClient.getEventBus().post(interactiveEvent);
                }
            }
            else if (packet instanceof ReplyPacket && getWaitingFuturesMap().containsKey(packet.getPacketID())) {
                CompletableFuture<ReplyPacket> sendRequest = getWaitingFuturesMap().remove(packet.getPacketID());
                if (!sendRequest.isDone()) {
                    sendRequest.complete((ReplyPacket) packet);
                }
            }
        }

        lastSequenceNumber.set(receivedPackets.get(receivedPackets.size() - 1).getSequenceNumber());
    }

    /**
     * Attempts to parse an <code>InteractiveEvent</code> from the packet received from the Interactive service.
     *
     * @param   methodPacket
     *          <code>MethodPacket</code> received from the Interactive service
     *
     * @return  An <code>InteractiveEvent</code> parsed from the packet received from the Interactive service,
     *          <code>null</code> if it was unable to parse an <code>InteractiveEvent</code>
     *
     * @since   1.0.0
     */
    private InteractiveEvent getEventFromPacket(MethodPacket methodPacket) {
        if (methodPacket != null) {
            InteractiveMethod method = methodPacket.getMethod();
            if (method != null) {
                switch (method) {
                    case HELLO: {
                        if (connectionPromise != null && !connectionPromise.isDone()) {
                            connectionPromise.complete(true);
                        }
                        return new HelloEvent();
                    }
                    case ON_READY:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), ReadyEvent.class);
                    case SET_COMPRESSION:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), SetCompressionEvent.class);
                    case ISSUE_MEMORY_WARNING:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), MemoryWarningEvent.class);
                    case ON_PARTICIPANT_JOIN:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), ParticipantJoinEvent.class);
                    case ON_PARTICIPANT_LEAVE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), ParticipantLeaveEvent.class);
                    case ON_PARTICIPANT_UPDATE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), ParticipantUpdateEvent.class);
                    case ON_GROUP_CREATE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), GroupCreateEvent.class);
                    case ON_GROUP_DELETE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), GroupDeleteEvent.class);
                    case ON_GROUP_UPDATE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), GroupUpdateEvent.class);
                    case ON_SCENE_CREATE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), SceneCreateEvent.class);
                    case ON_SCENE_DELETE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), SceneDeleteEvent.class);
                    case ON_SCENE_UPDATE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), SceneUpdateEvent.class);
                    case ON_CONTROL_CREATE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), ControlCreateEvent.class);
                    case ON_CONTROL_DELETE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), ControlDeleteEvent.class);
                    case ON_CONTROL_UPDATE:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), ControlUpdateEvent.class);
                    case GIVE_INPUT:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), ControlInputEvent.class);
                    default:
                        return GameClient.GSON.fromJson(methodPacket.getRequestParameters(), UndefinedInteractiveEvent.class);
                }
            }
        }
        return null;
    }
}
