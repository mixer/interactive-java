package com.mixer.interactive;

import com.google.common.eventbus.EventBus;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.*;
import com.google.gson.*;
import com.mixer.interactive.event.UndefinedInteractiveEvent;
import com.mixer.interactive.exception.InteractiveNoHostsFoundException;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.gson.*;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.InteractivePacket;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.protocol.ReplyPacket;
import com.mixer.interactive.resources.control.InteractiveCanvasSize;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.control.InteractiveControlInput;
import com.mixer.interactive.resources.control.InteractiveControlType;
import com.mixer.interactive.resources.core.BandwidthThrottle;
import com.mixer.interactive.resources.core.CompressionScheme;
import com.mixer.interactive.resources.core.InteractiveMemoryStatistic;
import com.mixer.interactive.resources.core.ThrottleState;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import com.mixer.interactive.resources.scene.InteractiveScene;
import com.mixer.interactive.resources.transaction.InteractiveTransaction;
import com.mixer.interactive.util.EndpointUtil;
import com.mixer.interactive.ws.InteractiveWebSocketClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>A <code>GameClient</code> is used to connect to a Interactive integration that is hosted on the Interactive service.
 * A client can only connect to the integration that was specified when the client was created.</p>
 *
 * <p>Once connected a client has the ability to query information on the integration from the Interactive service,
 * as well as manipulate resources in the integration by making requests to the Interactive service.</p>
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class GameClient {

    /**
     * <code>Gson</code> singleton used for serialization/deserialization.
     */
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(InteractiveMethod.class, new InteractiveMethodAdapter())
            .registerTypeAdapter(InteractivePacket.class, new InteractivePacketAdapter())
            .registerTypeAdapter(InteractiveScene.class, new InteractiveSceneAdapter())
            .registerTypeAdapter(InteractiveControl.class, new InteractiveControlAdapter())
            .registerTypeAdapter(InteractiveCanvasSize.class, new InteractiveCanvasSizeAdapter())
            .registerTypeAdapter(InteractiveControlType.class, new InteractiveControlTypeAdapter())
            .registerTypeAdapter(InteractiveControlInput.class, new InteractiveControlInputAdapter())
            .registerTypeAdapter(UndefinedInteractiveEvent.class, new UndefinedInteractiveEventAdapter())
            .serializeNulls()
            .create();

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * The default time unit for timing out unfulfilled method requests.
     */
    private static final TimeUnit DEFAULT_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * The default duration for timing out unfulfilled method requests.
     */
    private static final long DEFAULT_TIMEOUT_DURATION = 15;

    /**
     * The default size for the scheduled thread pool.
     */
    private static final int THREAD_POOL_SIZE = 10;

    /**
     * Type object used to serialize/de-serialize <code>Map</code> of <code>InteractiveMethods</code> to
     * <code>ThrottleState</code>.
     */
    private static final Type THROTTLE_STATE_MAP_TYPE = new TypeToken<Map<InteractiveMethod, ThrottleState>>(){}.getType();

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveParticipant</code>.
     */
    private static final Type PARTICIPANT_SET_TYPE = new TypeToken<Set<InteractiveParticipant>>(){}.getType();

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveGroups</code>.
     */
    private static final Type GROUP_SET_TYPE = new TypeToken<Set<InteractiveGroup>>(){}.getType();

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveScenes</code>.
     */
    private static final Type SCENE_SET_TYPE = new TypeToken<Set<InteractiveScene>>(){}.getType();

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveControls</code>.
     */
    private static final Type CONTROL_SET_TYPE = new TypeToken<Set<InteractiveControl>>(){}.getType();

    /**
     * Empty Json object for use in several method calls.
     */
    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    /**
     * Prepared Json object for "ready" method calls ('ready' call where isReady is true).
     */
    private static final JsonObject READY_JSON_OBJECT;

    /**
     * Prepared Json object for "not ready" method calls ('ready' call where isReady is false).
     */
    private static final JsonObject NOT_READY_JSON_OBJECT;

    /**
     * Secure WebSocket URI scheme
     */
    private static final String SECURE_WEBSOCKET_SCHEME = "wss";

    /**
     * TLS SSL instance
     */
    private static final String TLS_INSTANCE = "TLS";

    /**
     * Constant representing the default scene/group IDs
     */
    private static final String DEFAULT_VALUE = "default";

    /**
     * Collection of parameter key names for various method calls and
     * events
     */
    private static final String PARAM_KEY_IS_READY = "isReady";
    private static final String PARAM_KEY_COMPRESSION_SCHEME = "scheme";
    private static final String PARAM_KEY_TIME = "time";
    private static final String PARAM_KEY_PARTICIPANTS = "participants";
    private static final String PARAM_KEY_FROM = "from";
    private static final String PARAM_KEY_HAS_MORE = "hasMore";
    private static final String PARAM_KEY_THRESHOLD = "threshold";
    private static final String PARAM_KEY_GROUPS = "groups";
    private static final String PARAM_KEY_GROUP_ID = "groupID";
    private static final String PARAM_KEY_REASSIGN_GROUP_ID = "reassignGroupID";
    private static final String PARAM_KEY_SCENES = "scenes";
    private static final String PARAM_KEY_SCENE_ID = "sceneID";
    private static final String PARAM_KEY_REASSIGN_SCENE_ID = "reassignSceneID";
    private static final String PARAM_KEY_CONTROLS = "controls";
    private static final String PARAM_KEY_CONTROL_IDS = "controlIDs";
    private static final String PARAM_KEY_TRANSACTION_ID = "transactionID";

    // Initialize the prepared Json objects
    static {
        READY_JSON_OBJECT = new JsonObject();
        READY_JSON_OBJECT.addProperty(PARAM_KEY_IS_READY, true);
        NOT_READY_JSON_OBJECT = new JsonObject();
        NOT_READY_JSON_OBJECT.addProperty(PARAM_KEY_IS_READY, false);
    }

    /**
     * The version ID of the Interactive project that this game client will connect to
     */
    private final Number projectVersionId;

    /**
     * Event bus where incoming events from the Interactive service are posted to
     */
    private final EventBus eventBus;

    /**
     * Thread executor service for creating <code>ListenableFutures</code>
     */
    private final ListeningScheduledExecutorService executor;

    /**
     * WebSocket client that this game client uses to communicate with the Interactive service
     */
    private InteractiveWebSocketClient webSocketClient;

    /**
     * Initializes a new <code>GameClient</code>.
     *
     * @param   projectVersionId
     *          The project version ID for the Interactive integration the client will use
     *
     * @since   1.0.0
     */
    public GameClient(Number projectVersionId) {
        this.projectVersionId = projectVersionId;
        eventBus = new EventBus(projectVersionId.toString());
        executor = MoreExecutors.listeningDecorator(
                Executors.newScheduledThreadPool(THREAD_POOL_SIZE, new ThreadFactoryBuilder()
                        .setNameFormat("interactive-project-" + this.projectVersionId + "-thread-%d")
                        .setDaemon(true)
                        .build()
                )
        );
    }

    /**
     * Returns the project version ID for the Interactive integration the client connects to.
     *
     * @return  The project version ID for the Interactive integration the client connects to
     *
     * @since   1.0.0
     */
    public Number getProjectVersionId() {
        return projectVersionId;
    }

    /**
     * Returns the <code>EventBus</code> associated with the client.
     *
     * @return  The <code>EventBus</code> associated with the client
     *
     * @since   1.0.0
     */
    public EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Returns <code>true</code> if this client is currently connected to the Interactive service,
     * <code>false</code> otherwise.
     *
     * @return  <code>true</code> if this client is currently connected to the Interactive service,
     *          <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }

    /**
     * Connects the game client to it's associated Interactive integration on the Interactive service, using an OAuth
     * Bearer token to authenticate itself with the Interactive service.
     *
     * @param   oauthToken
     *          OAuth Bearer token
     *
     * @since   1.0.0
     */
    public void connect(String oauthToken) {
        try {
            connect(oauthToken, null, EndpointUtil.getInteractiveHost().getAddress());
        }
        catch (InteractiveNoHostsFoundException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Connects the game client to it's associated Interactive integration on the Interactive service, using an OAuth
     * Bearer token to authenticate itself with the Interactive service and the appropriate share code for the
     * integration.
     *
     * @param   oauthToken
     *          OAuth Bearer token
     * @param   shareCode
     *          The share code provided by the author of the Interactive integration
     *
     * @since   1.0.0
     */
    public void connect(String oauthToken, String shareCode) {
        try {
            connect(oauthToken, shareCode, EndpointUtil.getInteractiveHost().getAddress());
        }
        catch (InteractiveNoHostsFoundException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Connects the game client to it's associated Interactive integration on a specific Interactive service host,
     * using an OAuth Bearer token to authenticate itself with the Interactive service.
     *
     * @param   oauthToken
     *          OAuth Bearer token
     * @param   interactiveHost
     *          <code>URI</code> for an Interactive service host
     *
     * @since   1.0.0
     */
    public void connect(String oauthToken, URI interactiveHost) {
        connect(oauthToken, null, interactiveHost);
    }

    /**
     * Connects the game client to it's associated Interactive integration on a specific Interactive service host,
     * using an OAuth Bearer token to authenticate itself with the Interactive service and the appropriate share code
     * for the integration.
     *
     * @param   oauthToken
     *          An OAuth Bearer token
     * @param   shareCode
     *          The share code provided by the author of the Interactive integration
     * @param   interactiveHost
     *          <code>URI</code> for an Interactive service host
     *
     * @since   1.0.0
     */
    public void connect(String oauthToken, String shareCode, URI interactiveHost) {
        if (oauthToken != null && !oauthToken.isEmpty() && interactiveHost != null) {
            if (shareCode != null && !shareCode.isEmpty()) {
                webSocketClient = new InteractiveWebSocketClient(this, interactiveHost, oauthToken, projectVersionId, shareCode);
            }
            else {
                webSocketClient = new InteractiveWebSocketClient(this, interactiveHost, oauthToken, projectVersionId);
            }

            try {
                if (SECURE_WEBSOCKET_SCHEME.equals(webSocketClient.getURI().getScheme())) {
                    SSLContext sslContext = SSLContext.getInstance(TLS_INSTANCE);
                    sslContext.init(null, null, null);
                    webSocketClient.setSocket(sslContext.getSocketFactory().createSocket());
                }
                webSocketClient.connectBlocking();
            }
            catch (InterruptedException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
                LOG.error(e.getMessage(), e);
                webSocketClient = null;
            }
        }
    }

    /**
     * Connects the game client to it's associated Interactive integration on the Interactive service, using an OAuth
     * Bearer token to authenticate itself with the Interactive service.
     *
     * @param   oauthToken
     *          An OAuth Bearer token
     *
     * @return  A <code>ListenableFuture</code> that completes when the connection attempt is finished
     *
     * @since   1.0.0
     */
    public ListenableFuture connectAsync(String oauthToken) {
        return executor.submit(() -> connect(oauthToken));
    }

    /**
     * Connects the game client to it's associated Interactive integration on the Interactive service, using an OAuth
     * Bearer token to authenticate itself with the Interactive service and the appropriate share code for the
     * integration.
     *
     * @param   oauthToken
     *          An OAuth Bearer token
     * @param   shareCode
     *          The share code provided by the author of the Interactive integration
     *
     * @return  A <code>ListenableFuture</code> that completes when the connection attempt is finished
     *
     * @since   1.0.0
     */
    public ListenableFuture connectAsync(String oauthToken, String shareCode) {
        return executor.submit(() -> connect(oauthToken, shareCode));
    }

    /**
     * Connects the game client to it's associated Interactive integration on a specific Interactive service host,
     * using an OAuth Bearer token to authenticate itself with the Interactive service.
     *
     * @param   oauthToken
     *          An OAuth Bearer token
     * @param   interactiveHost
     *          <code>URI</code> for an Interactive service host
     *
     * @return  A <code>ListenableFuture</code> that completes when the connection attempt is finished
     *
     * @since   1.0.0
     */
    public ListenableFuture connectAsync(String oauthToken, URI interactiveHost) {
        return executor.submit(() -> connect(oauthToken, interactiveHost));
    }

    /**
     * Connects the game client to it's associated Interactive integration on a specific Interactive service host,
     * using an OAuth Bearer token to authenticate itself with the Interactive service and the appropriate share code
     * for the integration.
     *
     * @param   oauthToken
     *          An OAuth Bearer token
     * @param   shareCode
     *          The share code provided by the author of the Interactive integration
     * @param   interactiveHost
     *          <code>URI</code> for an Interactive service host
     *
     * @return  A <code>ListenableFuture</code> that completes when the connection attempt is finished
     *
     * @since   1.0.0
     */
    public ListenableFuture connectAsync(String oauthToken, String shareCode, URI interactiveHost) {
        return executor.submit(() -> connect(oauthToken, shareCode, interactiveHost));
    }

    /**
     * Disconnects the client from the Interactive service.
     *
     * @since   1.0.0
     */
    public void disconnect() {
        if (isConnected()) {
            try {
                webSocketClient.closeBlocking();
            }
            catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            finally {
                webSocketClient = null;
            }
        }
    }

    /**
     * Disconnects the client from the Interactive service.
     *
     * @return  A <code>ListenableFuture</code> that completes when the client has been disconnected from the
     *          Interactive service
     *
     * @since   1.0.0
     */
    public ListenableFuture disconnectAsync() {
        return executor.submit(this::disconnect);
    }

    /**
     * <p>Notifies the Interactive service whether or not this client is ready to have clients connect and start
     * interacting.</p>
     *
     * <p>The parameters of the request (<code>isReady: true</code> or <code>isReady: false</code>) determine whether
     * or not the Interactive integration this client is connected to is ready to have clients connect and start
     * interacting or not, respectively.</p>
     *
     * @param   isReady
     *          The value to pass along in the <code>ready</code> method call
     *
     * @since   1.0.0
     */
    public void ready(boolean isReady) {
        makeRequestNoReply(InteractiveMethod.READY, getReadyJsonObject(isReady));
    }

    /**
     * <p>Changes the compression algorithm the client uses to encode/decode messages to/from the Interactive
     * service.</p>
     *
     * <p>Developers supply a collection of preferred schemes in order of preference, from greatest to least preference,
     * from which the Interactive service will select a preferred common one. All subsequent requests and replies will
     * be sent in using the new compression scheme.</p>
     *
     * <p>If no preferred common scheme is found, the Interactive server will fall back to the
     * {@link CompressionScheme#NONE plain text} scheme.</p>
     *
     * @param   schemes
     *          An array of preferred <code>CompressionSchemes</code> in order of preference, from greatest to
     *          least preference
     *
     * @return  The new <code>CompressionScheme</code> that the client is to use to communicate with
     *          the Interactive service
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     CompressionScheme
     *
     * @since   1.0.0
     */
    public CompressionScheme setCompression(CompressionScheme ... schemes) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return setCompression(Arrays.stream(schemes).map(CompressionScheme::toString).collect(Collectors.toList()));
    }

    /**
     * <p>Changes the compression algorithm the client uses to encode/decode messages to/from the Interactive
     * service.</p>
     *
     * <p>Developers supply a collection of preferred schemes in order of preference, from greatest to least preference,
     * from which the Interactive service will select a preferred common one. All subsequent requests and replies will
     * be sent in using the new compression scheme.</p>
     *
     * <p>If no preferred common scheme is found, the Interactive server will fall back to the
     * {@link CompressionScheme#NONE plain text} scheme.</p>
     *
     * @param   schemes
     *          An <code>Collection</code> of preferred <code>CompressionSchemes</code> in order of preference, from
     *          greatest to least preference
     *
     * @return  The new <code>CompressionScheme</code> that the client is to use to communicate with
     *          the Interactive service
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     CompressionScheme
     *
     * @since   1.0.0
     */
    public CompressionScheme setCompression(Collection<String> schemes) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return setCompression(schemes.toArray(new String[0]));
    }

    /**
     * <p>Changes the compression algorithm the client uses to encode/decode messages to/from the Interactive
     * service.</p>
     *
     * <p>Developers supply a collection of preferred schemes in order of preference, from greatest to least preference,
     * from which the Interactive service will select a preferred common one. All subsequent requests and replies will
     * be sent in using the new compression scheme.</p>
     *
     * <p>If no preferred common scheme is found, the Interactive server will fall back to the
     * {@link CompressionScheme#NONE plain text} scheme.</p>
     *
     * @param   schemes
     *          An array of preferred compression schemes in order of preference, from greatest to
     *          least preference
     *
     * @return  The new <code>CompressionScheme</code> that the client is to use to communicate with
     *          the Interactive service
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     CompressionScheme
     *
     * @since   1.0.0
     */
    public CompressionScheme setCompression(String ... schemes) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        JsonObject jsonParams = new JsonObject();
        List<String> compressionSchemes = new ArrayList<>();
        for (String scheme : schemes) {
            if (CompressionScheme.from(scheme) != null && !compressionSchemes.contains(scheme)) {
                compressionSchemes.add(scheme);
            }
        }
        jsonParams.add(PARAM_KEY_COMPRESSION_SCHEME, GSON.toJsonTree(compressionSchemes));
        CompressionScheme compressionScheme = CompressionScheme.from(makeRequest(InteractiveMethod.SET_COMPRESSION, jsonParams, PARAM_KEY_COMPRESSION_SCHEME, String.class));
        webSocketClient.setCompressionScheme(compressionScheme);
        return compressionScheme;
    }

    /**
     * Retrieves the current server time from the Interactive service, given as a milliseconds UTC unix timestamp.
     *
     * @return  The Interactive service's current server time, given as a UTC unix timestamp (in milliseconds)
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @since   1.0.0
     */
    public Long getTime() throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return makeRequest(InteractiveMethod.GET_TIME, EMPTY_JSON_OBJECT, PARAM_KEY_TIME, Long.class);
    }

    /**
     * <p>Retrieves the current server time from the Interactive service, given as a milliseconds UTC unix timestamp.
     * </p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @return  A <code>ListenableFuture</code> that when complete returns the Interactive service's current server
     *          time, given as a UTC unix timestamp (in milliseconds)
     *
     * @since   1.0.0
     */
    public ListenableFuture<Long> getTimeAsync() {
        return makeRequestAsync(InteractiveMethod.GET_TIME, EMPTY_JSON_OBJECT, PARAM_KEY_TIME, Long.class);
    }

    /**
     * <p>Retrieves the current memory usage for the Interactive integration this client is connected to on the
     * Interactive service. The memory usage returned is a dump of information regarding current memory allocations, as
     * well as a breakdown of how much memory is allocated where. This information is provided for debugging purposes.
     * </p>
     *
     * @return  A <code>InteractiveMemoryStatistic</code> representing the current memory usage of the Interactive
     *          integration the client is connected to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveMemoryStatistic
     *
     * @since   1.0.0
     */
    public InteractiveMemoryStatistic getMemoryStats() throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return makeRequest(InteractiveMethod.GET_MEMORY_STATS, EMPTY_JSON_OBJECT, InteractiveMemoryStatistic.class);
    }

    /**
     * <p>Retrieves the current memory usage for the Interactive integration this client is connected to on the
     * Interactive service. The memory usage returned is a dump of information regarding current memory allocations, as
     * well as a breakdown of how much memory is allocated where. This information is provided for debugging purposes.
     * </p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>InteractiveMemoryStatistic</code>
     *          representing the current memory usage of the Interactive integration the client is connected to
     *
     * @see     InteractiveMemoryStatistic
     *
     * @since   1.0.0
     */
    public ListenableFuture<InteractiveMemoryStatistic> getMemoryStatsAsync() {
        return makeRequestAsync(InteractiveMethod.GET_MEMORY_STATS, EMPTY_JSON_OBJECT, InteractiveMemoryStatistic.class);
    }

    /**
     * Retrieves statistics on the state of throttling rules set up in {@link InteractiveMethod#SET_BANDWIDTH_THROTTLE}
     * method requests. It returns the number of sent packets (ones inserted into the bucket) and the number of rejected
     * packets for each method that has a throttle set.
     *
     * @return  A <code>Map</code> of <code>InteractiveMethods</code> and their associated
     *          <code>ThrottleState</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     BandwidthThrottle
     * @see     InteractiveMethod
     * @see     ThrottleState
     *
     * @since   1.0.0
     */
    public Map<InteractiveMethod, ThrottleState> getThrottleState() throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return makeRequest(InteractiveMethod.GET_THROTTLE_STATE, JsonNull.INSTANCE, THROTTLE_STATE_MAP_TYPE);
    }

    /**
     * <p>Retrieves statistics on the state of throttling rules set up in
     * {@link InteractiveMethod#SET_BANDWIDTH_THROTTLE} method requests. It returns the number of sent packets (ones
     * inserted into the bucket) and the number of rejected packets for each method that has a throttle set.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Map</code> of
     *          <code>InteractiveMethods</code> and their associated <code>ThrottleState</code>
     *
     * @see     BandwidthThrottle
     * @see     InteractiveMethod
     * @see     ThrottleState
     *
     * @since   1.0.0
     */
    public ListenableFuture<Map<InteractiveMethod, ThrottleState>> getThrottleStateAsync() {
        return makeRequestAsync(InteractiveMethod.GET_THROTTLE_STATE, JsonNull.INSTANCE, THROTTLE_STATE_MAP_TYPE);
    }

    /**
     * <p>Sets up throttling for certain server-to-client method calls, such as {@link InteractiveMethod#GIVE_INPUT},
     * which could become problematic in very high-traffic scenarios.</p>
     *
     * <p>The Interactive service implements a
     * <a target="_blank" href="https://en.wikipedia.org/wiki/Leaky_bucket">leaky bucket algorithm</a>; the client
     * specifies the total bucket capacity in bytes and its drain rate in bytes per second. The client supplies a map of
     * method names and their associated throttle rules. Throttling previously enabled on a method can be disabled by
     * setting it to null.</p>
     *
     * @param   throttleMap
     *          A <code>Map</code> of <code>InteractiveMethods</code> and their associated
     *          <code>BandwidthThrottle</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     BandwidthThrottle
     * @see     InteractiveMethod
     *
     * @since   1.0.0
     */
    public void setBandwidthThrottle(Map<InteractiveMethod, BandwidthThrottle> throttleMap) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (throttleMap != null) {
            makeRequest(InteractiveMethod.SET_BANDWIDTH_THROTTLE, GSON.toJsonTree(throttleMap));
        }
    }

    /**
     * <p>Sets up throttling for certain server-to-client method calls, such as {@link InteractiveMethod#GIVE_INPUT},
     * which could become problematic in very high-traffic scenarios.</p>
     *
     * <p>The Interactive service implements a
     * <a target="_blank" href="https://en.wikipedia.org/wiki/Leaky_bucket">leaky bucket algorithm</a>; the client
     * specifies the total bucket capacity in bytes and its drain rate in bytes per second. The client supplies a map of
     * method names and their associated throttle rules. Throttling previously enabled on a method can be disabled by
     * setting it to null.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   throttleMap
     *          A <code>Map</code> of <code>InteractiveMethods</code> and their associated
     *          <code>BandwidthThrottle</code>
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#SET_BANDWIDTH_THROTTLE setBandwidthThrottle} method call completes with no
     *          errors
     *
     * @see     BandwidthThrottle
     * @see     InteractiveMethod
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> setBandwidthThrottleAsync(Map<InteractiveMethod, BandwidthThrottle> throttleMap) {
        return throttleMap != null
                ? makeRequestAsync(InteractiveMethod.SET_BANDWIDTH_THROTTLE, GSON.toJsonTree(throttleMap))
                : Futures.immediateFuture(true);
    }

    /**
     * Retrieves all of the participants that are currently connected to the Interactive integration that this client is
     * connected to, in ascending order by the time they connected.
     *
     * @return  A <code>Set</code> of all <code>InteractiveParticipants</code> connected to the
     *          Interactive integration
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public Set<InteractiveParticipant> getAllParticipants() throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return getParticipants(InteractiveMethod.GET_ALL_PARTICIPANTS, 0, Comparator.comparingLong(InteractiveParticipant::getConnectedAt));
    }

    /**
     * <p>Retrieves all of the participants that are currently connected to the Interactive integration that this client
     * is connected to, in ascending order by the time they connected.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of all
     *          <code>InteractiveParticipants</code> connected to the Interactive integration
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveParticipant>> getAllParticipantsAsync() {
        return executor.submit(this::getAllParticipants);
    }

    /**
     * Retrieves all of the currently connected participants who have given input after the specified threshold time,
     * where the threshold is given as a UTC unix timestamp (in milliseconds), in ascending order by the time they last
     * gave input.
     *
     * @param   thresholdTimestamp
     *          A UTC unix timestamp (in milliseconds)
     *
     * @return  A <code>Set</code> of <code>InteractiveParticipants</code> connected to the
     *          Interactive integration that have given input since the provided threshold, in ascending order by
     *          the time they last gave input
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public Set<InteractiveParticipant> getActiveParticipants(long thresholdTimestamp) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return getParticipants(InteractiveMethod.GET_ACTIVE_PARTICIPANTS, thresholdTimestamp, Comparator.comparingLong(InteractiveParticipant::getLastInputAt));
    }

    /**
     * <p>Retrieves all of the currently connected participants who have given input after the specified threshold time,
     * where the threshold is given as a UTC unix timestamp (in milliseconds), in ascending order by the time they last
     * gave input.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   thresholdTimestamp
     *          A UTC unix timestamp (in milliseconds)
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of
     *          <code>InteractiveParticipants</code> connected to the Interactive integration
     *          that have given input since the provided threshold
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveParticipant>> getActiveParticipantsAsync(long thresholdTimestamp) {
        return executor.submit(() -> getActiveParticipants(thresholdTimestamp));
    }

    /**
     * <p>Bulk-updates a collection of participants. The Interactive service will reply with a set of updated
     * participants with their new etags.</p>
     *
     * <p>The Interactive service will either update all the participants provided, or fail in which case NONE of the
     * participants provided will be updated. In no case will the Interactive service apply updates to a subset of
     * participants. If a provided participant is not connected to the integration, the update to that participant will
     * be ignored.</p>
     *
     * @param   participants
     *          A <code>Collection</code> of <code>InteractiveParticipants</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveParticipants</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public Set<InteractiveParticipant> updateParticipants(Collection<InteractiveParticipant> participants) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return participants != null
                ? updateParticipants(participants.toArray(new InteractiveParticipant[0]))
                : Collections.emptySet();
    }

    /**
     * <p>Bulk-updates a collection of participants. The Interactive service will reply with a set of updated
     * participants with their new etags.</p>
     *
     * <p>The Interactive service will either update all the participants provided, or fail in which case NONE of the
     * participants provided will be updated. In no case will the Interactive service apply updates to a subset of
     * participants. If a provided participant is not connected to the integration, the update to that participant will
     * be ignored.</p>
     *
     * @param   participants
     *          An array of <code>InteractiveParticipants</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveParticipants</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public Set<InteractiveParticipant> updateParticipants(InteractiveParticipant ... participants) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (participants == null) {
            return Collections.emptySet();
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_PARTICIPANTS, GSON.toJsonTree(participants, InteractiveParticipant[].class));
        return makeRequest(InteractiveMethod.UPDATE_PARTICIPANTS, jsonParams, PARAM_KEY_PARTICIPANTS, PARTICIPANT_SET_TYPE);
    }

    /**
     * <p>Bulk-updates a collection of participants. The Interactive service will reply with a set of updated
     * participants with their new etags.</p>
     *
     * <p>The Interactive service will either update all the participants provided, or fail in which case NONE of the
     * participants provided will be updated. In no case will the Interactive service apply updates to a subset of
     * participants. If a provided participant is not connected to the integration, the update to that participant will
     * be ignored.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   participants
     *          A <code>Collection</code> of <code>InteractiveParticipants</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveParticipants</code>
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveParticipant>> updateParticipantsAsync(Collection<InteractiveParticipant> participants) {
        return participants != null
                ? updateParticipantsAsync(participants.toArray(new InteractiveParticipant[0]))
                : Futures.immediateFuture(Collections.emptySet());
    }

    /**
     * <p>Bulk-updates a collection of participants. The Interactive service will reply with a set of updated
     * participants with their new etags.</p>
     *
     * <p>The Interactive service will either update all the participants provided, or fail in which case NONE of the
     * participants provided will be updated. In no case will the Interactive service apply updates to a subset of
     * participants. If a provided participant is not connected to the integration, the update to that participant will
     * be ignored.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   participants
     *          An array of <code>InteractiveParticipants</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveParticipants</code>
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveParticipant>> updateParticipantsAsync(InteractiveParticipant ... participants) {
        if (participants == null) {
            return Futures.immediateFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_PARTICIPANTS, GSON.toJsonTree(participants, InteractiveParticipant[].class));
        return makeRequestAsync(InteractiveMethod.UPDATE_PARTICIPANTS, jsonParams, PARAM_KEY_PARTICIPANTS, PARTICIPANT_SET_TYPE);
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration. An initial etag for the groups may be provided.</p>
     *
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be created
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void createGroups(Collection<InteractiveGroup> groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (groups != null) {
            createGroups(groups.toArray(new InteractiveGroup[0]));
        }
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration. An initial etag for the groups may be provided.</p>
     *
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be created
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void createGroups(InteractiveGroup ... groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (groups != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.add(PARAM_KEY_GROUPS, GSON.toJsonTree(groups, InteractiveGroup[].class));
            makeRequest(InteractiveMethod.CREATE_GROUPS, jsonParams);
        }
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration. An initial etag for the groups may be provided.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_GROUPS createGroups} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createGroupsAsync(Collection<InteractiveGroup> groups) {
        return groups != null
                ? createGroupsAsync(groups.toArray(new InteractiveGroup[0]))
                : Futures.immediateFuture(true);
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration. An initial etag for the groups may be provided.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_GROUPS createGroups} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createGroupsAsync(InteractiveGroup ... groups) {
        if (groups == null) {
            return Futures.immediateFuture(true);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_GROUPS, GSON.toJsonTree(groups, InteractiveGroup[].class));
        return makeRequestAsync(InteractiveMethod.CREATE_GROUPS, jsonParams);
    }

    /**
     * Retrieves all the groups connected to the Interactive integration.
     *
     * @return  A <code>Set</code> of <code>InteractiveGroups</code> for the connected Interactive integration
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> getGroups() throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return makeRequest(InteractiveMethod.GET_GROUPS, EMPTY_JSON_OBJECT, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * <p>Retrieves all the groups connected to the Interactive integration.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of
     *          <code>InteractiveGroups</code> for the connected Interactive integration
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveGroup>> getGroupsAsync() {
        return makeRequestAsync(InteractiveMethod.GET_GROUPS, EMPTY_JSON_OBJECT, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups with
     * their new etags.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
     *
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveGroups</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> updateGroups(Collection<InteractiveGroup> groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return groups != null
                ? updateGroups(groups.toArray(new InteractiveGroup[0]))
                : Collections.emptySet();
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups with
     * their new etags.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
     *
     * @param   groups
     *          An array of <code>InteractiveGroup InteractiveGroups</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveGroups</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> updateGroups(InteractiveGroup ... groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (groups == null) {
            return Collections.emptySet();
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_GROUPS, GSON.toJsonTree(groups, InteractiveGroup[].class));
        return makeRequest(InteractiveMethod.UPDATE_GROUPS, jsonParams, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups with
     * their new etags.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveGroup>> updateGroupsAsync(Collection<InteractiveGroup> groups) {
        return groups != null
                ? updateGroupsAsync(groups.toArray(new InteractiveGroup[0]))
                : Futures.immediateFuture(Collections.emptySet());
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups with
     * their new etags.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveGroup>> updateGroupsAsync(InteractiveGroup ... groups) {
        if (groups == null) {
            return Futures.immediateFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_GROUPS, GSON.toJsonTree(groups, InteractiveGroup[].class));
        return makeRequestAsync(InteractiveMethod.UPDATE_GROUPS, jsonParams, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * Removes a group from the Interactive integration, reassigning any participants who were in that group to the
     * default group. The server MAY not return an error if the group to remove does not exist.
     *
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void deleteGroup(String groupID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        deleteGroup(groupID, DEFAULT_VALUE);
    }

    /**
     * Removes a group from the Interactive integration, reassigning any participants who were in that group to the
     * default group. The server MAY not return an error if the group to remove does not exist.
     *
     * @param   group
     *          An <code>InteractiveGroup</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void deleteGroup(InteractiveGroup group) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (group != null) {
            deleteGroup(group.getGroupID());
        }
    }

    /**
     * Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.
     *
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroupID
     *          Identifier for the <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void deleteGroup(String groupID, String reassignGroupID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (groupID != null && reassignGroupID != null) {
            JsonObject jsonGroup = new JsonObject();
            jsonGroup.addProperty(PARAM_KEY_GROUP_ID, groupID);
            jsonGroup.addProperty(PARAM_KEY_REASSIGN_GROUP_ID, reassignGroupID);
            makeRequest(InteractiveMethod.DELETE_GROUP, jsonGroup);
        }
    }

    /**
     * Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.
     *
     * @param   group
     *          An <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroupID
     *          Identifier for the <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void deleteGroup(InteractiveGroup group, String reassignGroupID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (group != null) {
            deleteGroup(group.getGroupID(), reassignGroupID);
        }
    }

    /**
     * Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.
     *
     * @param   group
     *          An <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroup
     *          The <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void deleteGroup(InteractiveGroup group, InteractiveGroup reassignGroup) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (group != null && reassignGroup != null) {
            deleteGroup(group.getGroupID(), reassignGroup.getGroupID());
        }
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to the
     * default group. The server MAY not return an error if the group to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteGroupsAsync(String groupID) {
        return deleteGroupsAsync(groupID, DEFAULT_VALUE);
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to the
     * default group. The server MAY not return an error if the group to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   group
     *          An <code>InteractiveGroup</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteGroupsAsync(InteractiveGroup group) {
        return group != null
                ? deleteGroupsAsync(group.getGroupID())
                : Futures.immediateFuture(false);
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroupID
     *          Identifier for the <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteGroupsAsync(String groupID, String reassignGroupID) {
        if (groupID == null || reassignGroupID == null) {
            return Futures.immediateFuture(false);
        }

        JsonObject jsonGroup = new JsonObject();
        jsonGroup.addProperty(PARAM_KEY_GROUP_ID, groupID);
        jsonGroup.addProperty(PARAM_KEY_REASSIGN_GROUP_ID, reassignGroupID);
        return makeRequestAsync(InteractiveMethod.DELETE_GROUP, jsonGroup);
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   group
     *          An <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroupID
     *          Identifier for the <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteGroupsAsync(InteractiveGroup group, String reassignGroupID) {
        return group != null
                ? deleteGroupsAsync(group.getGroupID(), reassignGroupID)
                : Futures.immediateFuture(false);
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   group
     *          An <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroup
     *          The <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteGroupsAsync(InteractiveGroup group, InteractiveGroup reassignGroup) {
        return (group != null && reassignGroup != null)
                ? deleteGroupsAsync(group.getGroupID(), reassignGroup.getGroupID())
                : Futures.immediateFuture(false);
    }

    /**
     * <p>Creates one or more new scenes. Scene IDs MUST be unique and not already exist in the Interactive integration.
     * Initial etags for the scenes may be provided.</p>
     *
     * <p>Scene objects may also optionally include controls to be set on the scene initially, rather than requiring
     * further {@link InteractiveMethod#CREATE_CONTROLS} calls. If an initial set of controls are provided, they
     * MUST be fully-qualified, tagged control objects; the etags provided will be used as their initial values.</p>
     *
     * <p>The Interactive service will either create all scenes and controls, or fail in which case NONE of the
     * scenes and controls provided will be created. In no case will the Interactive service create a subset of scenes
     * and controls.</p>
     *
     * @param   scenes
     *          A <code>Collection</code> of <code>InteractiveScenes</code> to be created
     *
     * @return  A <code>Set</code> of newly created <code>InteractiveScenes</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveScene> createScenes(Collection<InteractiveScene> scenes) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return (scenes != null) ? createScenes(scenes.toArray(new InteractiveScene[0])) : Collections.emptySet();
    }

    /**
     * <p>Creates one or more new scenes. Scene IDs MUST be unique and not already exist in the Interactive integration.
     * Initial etags for the scenes may be provided.</p>
     *
     * <p>Scene objects may also optionally include controls to be set on the scene initially, rather than requiring
     * further {@link InteractiveMethod#CREATE_CONTROLS} calls. If an initial set of controls are provided, they
     * MUST be fully-qualified, tagged control objects; the etags provided will be used as their initial values.</p>
     *
     * <p>The Interactive service will either create all scenes and controls, or fail in which case NONE of the
     * scenes and controls provided will be created. In no case will the Interactive service create a subset of scenes
     * and controls.</p>
     *
     * @param   scenes
     *          An array of <code>InteractiveScenes</code> to be created
     *
     * @return  A <code>Set</code> of newly created <code>InteractiveScenes</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveScene> createScenes(InteractiveScene ... scenes) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scenes != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.add(PARAM_KEY_SCENES, GSON.toJsonTree(scenes, InteractiveScene[].class));
            return makeRequest(InteractiveMethod.CREATE_SCENES, jsonParams, PARAM_KEY_SCENES, SCENE_SET_TYPE);
        }
        else {
            return Collections.emptySet();
        }
    }

    /**
     * <p>Creates one or more new scenes. Scene IDs MUST be unique and not already exist in the Interactive integration.
     * Initial etags for the scenes may be provided.</p>
     *
     * <p>Scene objects may also optionally include controls to be set on the scene initially, rather than requiring
     * further {@link InteractiveMethod#CREATE_CONTROLS} calls. If an initial set of controls are provided, they
     * MUST be fully-qualified, tagged control objects; the etags provided will be used as their initial values.</p>
     *
     * <p>The Interactive service will either create all scenes and controls, or fail in which case NONE of the
     * scenes and controls provided will be created. In no case will the Interactive service create a subset of scenes
     * and controls.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scenes
     *          A <code>Collection</code> of <code>InteractiveScenes</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of newly created
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveScene>> createScenesAsync(Collection<InteractiveScene> scenes) {
        return (scenes != null) ? createScenesAsync(scenes.toArray(new InteractiveScene[0])) : Futures.immediateFuture(Collections.emptySet());
    }

    /**
     * <p>Creates one or more new scenes. Scene IDs MUST be unique and not already exist in the Interactive integration.
     * Initial etags for the scenes may be provided.</p>
     *
     * <p>Scene objects may also optionally include controls to be set on the scene initially, rather than requiring
     * further {@link InteractiveMethod#CREATE_CONTROLS} calls. If an initial set of controls are provided, they
     * MUST be fully-qualified, tagged control objects; the etags provided will be used as their initial values.</p>
     *
     * <p>The Interactive service will either create all scenes and controls, or fail in which case NONE of the
     * scenes and controls provided will be created. In no case will the Interactive service create a subset of scenes
     * and controls.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scenes
     *          An array of <code>InteractiveScenes</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of newly created
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveScene>> createScenesAsync(InteractiveScene ... scenes) {
        if (scenes != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.add(PARAM_KEY_SCENES, GSON.toJsonTree(scenes, InteractiveScene[].class));
            return makeRequestAsync(InteractiveMethod.CREATE_SCENES, jsonParams, PARAM_KEY_SCENES, SCENE_SET_TYPE);
        }
        else {
            return Futures.immediateFuture(Collections.emptySet());
        }
    }

    /**
     * <p>Retrieves all the scenes for the Interactive integration.</p>
     *
     * @return  A <code>Set</code> of <code>InteractiveScenes</code> for the currently connected Interactive
     *          integration
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveScene> getScenes() throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return makeRequest(InteractiveMethod.GET_SCENES, EMPTY_JSON_OBJECT, PARAM_KEY_SCENES, SCENE_SET_TYPE);
    }

    /**
     * <p>Retrieves all the scenes for the Interactive integration.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of
     *          <code>InteractiveScenes</code> for the currently connected Interactive integration
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveScene>> getScenesAsync() {
        return makeRequestAsync(InteractiveMethod.GET_SCENES, EMPTY_JSON_OBJECT, PARAM_KEY_SCENES, SCENE_SET_TYPE);
    }

    /**
     * Removes a scene from the Interactive integration, reassigning any groups who were on that scene to the default
     * one. The server MAY not return an error if the scene to remove does not exist.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteScene(String sceneID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        deleteScene(sceneID, DEFAULT_VALUE);
    }

    /**
     * Removes a scene from the Interactive integration, reassigning any groups who were on that scene to a different
     * one. The server MAY not return an error if the scene to remove does not exist.
     *
     * @param   scene
     *          An <code>InteractiveScene</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteScene(InteractiveScene scene) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        deleteScene(scene != null ? scene.getSceneID() : null);
    }

    /**
     * Removes a scene from the Interactive integration, reassigning any groups who were on that scene to a different
     * one. The server MAY not return an error if the scene to remove does not exist.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> to be deleted
     * @param   reassignSceneID
     *          Identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> will be
     *          reassigned to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteScene(String sceneID, String reassignSceneID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && reassignSceneID != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
            jsonParams.addProperty(PARAM_KEY_REASSIGN_SCENE_ID, reassignSceneID);
            makeRequest(InteractiveMethod.DELETE_SCENE, jsonParams);
        }
    }

    /**
     * Removes a scene from the Interactive integration, reassigning any groups who were on that scene to a different
     * one. The server MAY not return an error if the scene to remove does not exist.
     *
     * @param   scene
     *          An <code>InteractiveScene</code> to be deleted
     * @param   reassignSceneID
     *          Identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> will be
     *          reassigned to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteScene(InteractiveScene scene, String reassignSceneID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scene != null) {
            deleteScene(scene.getSceneID(), reassignSceneID);
        }
    }

    /**
     * Removes a scene from the Interactive integration, reassigning any groups who were on that scene to a different
     * one. The server MAY not return an error if the scene to remove does not exist.
     *
     * @param   scene
     *          An <code>InteractiveScene</code> to be deleted
     * @param   reassignScene
     *          The <code>InteractiveScene</code> groups will be reassigned to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteScene(InteractiveScene scene, InteractiveScene reassignScene) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scene != null && reassignScene != null) {
            deleteScene(scene.getSceneID(), reassignScene.getSceneID());
        }
    }

    /**
     * <p>Removes a scene from the Interactive integration, reassigning any groups who were on that scene to the default
     * one. The server MAY not return an error if the scene to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteSceneAsync(String sceneID) {
        return deleteSceneAsync(sceneID, DEFAULT_VALUE);
    }

    /**
     * <p>Removes a scene from the Interactive integration, reassigning any groups who were on that scene to the default
     * one. The server MAY not return an error if the scene to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          An <code>InteractiveScene</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteSceneAsync(InteractiveScene scene) {
        if (scene != null) {
            return deleteSceneAsync(scene.getSceneID(), DEFAULT_VALUE);
        }
        else {
            return Futures.immediateFuture(false);
        }
    }

    /**
     * <p>Removes a scene from the Interactive integration, reassigning any groups who were on that scene to a different
     * one. The server MAY not return an error if the scene to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> to be deleted
     * @param   reassignSceneID
     *          Identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> will be
     *          reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteSceneAsync(String sceneID, String reassignSceneID) {
        if (sceneID != null && reassignSceneID != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
            jsonParams.addProperty(PARAM_KEY_REASSIGN_SCENE_ID, reassignSceneID);
            return makeRequestAsync(InteractiveMethod.DELETE_SCENE, jsonParams);
        }
        else {
            return Futures.immediateFuture(false);
        }
    }

    /**
     * <p>Removes a scene from the Interactive integration, reassigning any groups who were on that scene to a different
     * one. The server MAY not return an error if the scene to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          An <code>InteractiveScene</code> to be deleted
     * @param   reassignSceneID
     *          Identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> will be
     *          reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteSceneAsync(InteractiveScene scene, String reassignSceneID) {
        if (scene != null) {
            return deleteSceneAsync(scene.getSceneID(), reassignSceneID);
        }
        else {
            return Futures.immediateFuture(false);
        }
    }

    /**
     * <p>Removes a scene from the Interactive integration, reassigning any groups who were on that scene to a different
     * one. The server MAY not return an error if the scene to remove does not exist.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          An <code>InteractiveScene</code> to be deleted
     * @param   reassignScene
     *          The <code>InteractiveScene</code> groups will be reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteSceneAsync(InteractiveScene scene, InteractiveScene reassignScene) {
        if (scene != null && reassignScene != null) {
            return deleteSceneAsync(scene.getSceneID(), reassignScene.getSceneID());
        }
        else {
            return Futures.immediateFuture(false);
        }
    }

    /**
     * <p>Updates scenes that already exist. The Interactive service will reply with a set of updated scenes with
     * their new etags.</p>
     *
     * <p>The Interactive service will either update all the scenes provided, or fail in which case NONE of the
     * scenes provided will be updated. In no case will the Interactive service apply updates to a subset of
     * scenes.</p>
     *
     * @param   scenes
     *          A <code>Collection</code> of <code>InteractiveScenes</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveScenes</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveScene> updateScenes(Collection<InteractiveScene> scenes) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return (scenes != null) ? updateScenes(scenes.toArray(new InteractiveScene[0])) : Collections.emptySet();
    }

    /**
     * <p>Updates scenes that already exist. The Interactive service will reply with a set of updated scenes with
     * their new etags.</p>
     *
     * <p>The Interactive service will either update all the scenes provided, or fail in which case NONE of the
     * scenes provided will be updated. In no case will the Interactive service apply updates to a subset of
     * scenes.</p>
     *
     * @param   scenes
     *          An array of <code>InteractiveScenes</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveScenes</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveScene> updateScenes(InteractiveScene ... scenes) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scenes != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.add(PARAM_KEY_SCENES, GSON.toJsonTree(scenes, InteractiveScene[].class));
            return makeRequest(InteractiveMethod.UPDATE_SCENES, jsonParams, PARAM_KEY_SCENES, SCENE_SET_TYPE);
        }
        else {
            return Collections.emptySet();
        }
    }

    /**
     * <p>Updates scenes that already exist. The Interactive service will reply with a set of updated scenes with
     * their new etags.</p>
     *
     * <p>The Interactive service will either update all the scenes provided, or fail in which case NONE of the
     * scenes provided will be updated. In no case will the Interactive service apply updates to a subset of
     * scenes.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scenes
     *
     *          A <code>Collection</code> of <code>InteractiveScenes</code> to be updated
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveScene>> updateScenesAsync(Collection<InteractiveScene> scenes) {
        return (scenes != null) ? updateScenesAsync(scenes.toArray(new InteractiveScene[0])) : Futures.immediateFuture(Collections.emptySet());
    }

    /**
     * <p>Updates scenes that already exist. The Interactive service will reply with a set of updated scenes with
     * their new etags.</p>
     *
     * <p>The Interactive service will either update all the scenes provided, or fail in which case NONE of the
     * scenes provided will be updated. In no case will the Interactive service apply updates to a subset of
     * scenes.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scenes
     *          An array of <code>InteractiveScenes</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveScene>> updateScenesAsync(InteractiveScene ... scenes) {
        if (scenes != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.add(PARAM_KEY_SCENES, GSON.toJsonTree(scenes, InteractiveScene[].class));
            return makeRequestAsync(InteractiveMethod.UPDATE_SCENES, jsonParams, PARAM_KEY_SCENES, SCENE_SET_TYPE);
        }
        else {
            return Futures.immediateFuture(Collections.emptySet());
        }
    }

    /**
     * Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object in
     * this method; the etags provided will be used as their initial values.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be created
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void createControls(String sceneID, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        createControls(sceneID, controls.toArray(new InteractiveControl[0]));
    }

    /**
     * Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object in
     * this method; the etags provided will be used as their initial values.
     *
     * @param   scene
     *          The <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be created
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void createControls(InteractiveScene scene, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scene != null) {
            createControls(scene.getSceneID(), controls);
        }
    }

    /**
     * Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object in
     * this method; the etags provided will be used as their initial values.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be created
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void createControls(String sceneID, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && controls != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
            jsonParams.add(PARAM_KEY_CONTROLS, GSON.toJsonTree(controls, InteractiveControl[].class));
            makeRequest(InteractiveMethod.CREATE_CONTROLS, jsonParams);
        }
    }

    /**
     * Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object in
     * this method; the etags provided will be used as their initial values.
     *
     * @param   scene
     *          The <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be created
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void createControls(InteractiveScene scene, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scene != null) {
            createControls(scene.getSceneID(), controls);
        }
    }

    /**
     * <p>Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object
     * in this method; the etags provided will be used as their initial values.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_CONTROLS createControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createControlsAsync(String sceneID, Collection<InteractiveControl> controls) {
        return (sceneID != null && controls != null) ? createControlsAsync(sceneID, controls.toArray(new InteractiveControl[0])) : Futures.immediateFuture(false);
    }

    /**
     * <p>Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object
     * in this method; the etags provided will be used as their initial values.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          The <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_CONTROLS createControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createControlsAsync(InteractiveScene scene, Collection<InteractiveControl> controls) {
        return (scene != null ? createControlsAsync(scene.getSceneID(), controls) : Futures.immediateFuture(false));
    }

    /**
     * <p>Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object
     * in this method; the etags provided will be used as their initial values.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_CONTROLS createControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createControlsAsync(String sceneID, InteractiveControl ... controls) {
        if (sceneID == null || controls == null) {
            return Futures.immediateFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROLS, GSON.toJsonTree(controls, InteractiveControl[].class));
        return makeRequestAsync(InteractiveMethod.CREATE_CONTROLS, jsonParams);
    }

    /**
     * <p>Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object
     * in this method; the etags provided will be used as their initial values.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          The <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_CONTROLS createControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createControlsAsync(InteractiveScene scene, InteractiveControl ... controls) {
        return (scene != null ? createControlsAsync(scene.getSceneID(), controls) : Futures.immediateFuture(false));
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(String sceneID, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && controls != null) {
            deleteControls(sceneID, controls.toArray(new InteractiveControl[0]));
        }
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   scene
     *          The <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(InteractiveScene scene, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scene != null) {
            deleteControls(scene.getSceneID(), controls);
        }
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(String sceneID, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && controls != null) {
            deleteControls(sceneID, Arrays.stream(controls)
                    .map(InteractiveControl::getControlID)
                    .collect(Collectors.toSet())
                    .toArray(new String[0]));
        }
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   scene
     *          The <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          An array of <code>InteractiveControl</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(InteractiveScene scene, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scene != null) {
            deleteControls(scene.getSceneID(), controls);
        }
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controlIDs
     *          An array of identifiers for <code>InteractiveControls</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(String sceneID, String ... controlIDs) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && controlIDs != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
            jsonParams.add(PARAM_KEY_CONTROL_IDS, GSON.toJsonTree(controlIDs, String[].class));
            makeRequest(InteractiveMethod.DELETE_CONTROLS, jsonParams);
        }
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   scene
     *          The <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controlIDs
     *          An array of identifiers for <code>InteractiveControls</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(InteractiveScene scene, String ... controlIDs) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (scene != null) {
            deleteControls(scene.getSceneID(), controlIDs);
        }
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS deleteControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteControlsAsync(String sceneID, Collection<InteractiveControl> controls) {
        return (sceneID != null && controls != null) ? deleteControlsAsync(sceneID, controls.toArray(new InteractiveControl[0])) : Futures.immediateFuture(false);
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          The <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS deleteControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteControlsAsync(InteractiveScene scene, Collection<InteractiveControl> controls) {
        return deleteControlsAsync(scene != null ? scene.getSceneID() : null, controls);
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS deleteControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteControlsAsync(String sceneID, InteractiveControl ... controls) {
        if (sceneID == null || controls == null) {
            return Futures.immediateFuture(false);
        }

        return deleteControlsAsync(sceneID, Arrays.stream(controls)
                .map(InteractiveControl::getControlID)
                .collect(Collectors.toSet())
                .toArray(new String[0]));
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          The <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS deleteControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteControlsAsync(InteractiveScene scene, InteractiveControl ... controls) {
        return deleteControlsAsync(scene != null ? scene.getSceneID() : null, controls);
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controlIDs
     *          An array of identifiers for <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS deleteControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteControlsAsync(String sceneID, String ... controlIDs) {
        if (sceneID == null || controlIDs == null) {
            return Futures.immediateFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROL_IDS, GSON.toJsonTree(controlIDs, String[].class));
        return makeRequestAsync(InteractiveMethod.DELETE_CONTROLS, jsonParams);
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveControls</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveControl> updateControls(String sceneID, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return (sceneID != null && controls != null) ? updateControls(sceneID, controls.toArray(new InteractiveControl[0])) : Collections.emptySet();
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * @param   scene
     *          An <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveControls</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveControl> updateControls(InteractiveScene scene, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return updateControls(scene != null ? scene.getSceneID() : null, controls);
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveControls</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveControl> updateControls(String sceneID, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID == null || controls == null) {
            return Collections.emptySet();
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROLS, GSON.toJsonTree(controls, InteractiveControl[].class));
        return makeRequest(InteractiveMethod.UPDATE_CONTROLS, jsonParams, PARAM_KEY_CONTROLS, CONTROL_SET_TYPE);
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * @param   scene
     *          An <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveControls</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveControl> updateControls(InteractiveScene scene, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return updateControls(scene != null ? scene.getSceneID() : null, controls);
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveControls</code>
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveControl>> updateControlsAsync(String sceneID, Collection<InteractiveControl> controls) {
        return (sceneID != null && controls != null) ? updateControlsAsync(sceneID, controls.toArray(new InteractiveControl[0])) : Futures.immediateFuture(Collections.emptySet());
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          An <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveControls</code>
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveControl>> updateControlsAsync(InteractiveScene scene, Collection<InteractiveControl> controls) {
        return updateControlsAsync(scene != null ? scene.getSceneID() : null, controls);
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveControls</code>
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveControl>> updateControlsAsync(String sceneID, InteractiveControl ... controls) {
        if (sceneID == null || controls == null) {
            return Futures.immediateFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROLS, GSON.toJsonTree(controls, InteractiveControl[].class));
        return makeRequestAsync(InteractiveMethod.UPDATE_CONTROLS, jsonParams, PARAM_KEY_CONTROLS, CONTROL_SET_TYPE);
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   scene
     *          An <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveControls</code>
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveControl>> updateControlsAsync(InteractiveScene scene, InteractiveControl ... controls) {
        return updateControlsAsync(scene != null ? scene.getSceneID() : null, controls);
    }

    /**
     * Attempt to complete a spark transaction from the participant that initiated the transaction. The Interactive
     * service makes a best-effort to validate the charge before it’s created, blocking obviously invalid ones outright,
     * but when possible the client SHOULD await a successful reply before effecting any associated action.
     *
     * @param   transactionID
     *          Identifier for a Spark transaction
     *
     * @return  <code>true</code> if the transaction completed successfully, <code>false</code> otherwise
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControlInput
     * @see     InteractiveTransaction
     *
     * @since   1.0.0
     */
    public Boolean capture(String transactionID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (transactionID == null) {
            return false;
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_TRANSACTION_ID, transactionID);
        return makeRequest(InteractiveMethod.CAPTURE, jsonParams);
    }

    /**
     * <p>Attempt to complete a spark transaction from the participant that initiated the transaction. The Interactive
     * service makes a best-effort to validate the charge before it’s created, blocking obviously invalid ones outright,
     * but when possible the client SHOULD await a successful reply before effecting any associated action.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   transactionID
     *          Identifier for a Spark transaction
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CAPTURE capture} method call completes with no errors
     *
     * @see     InteractiveControlInput
     * @see     InteractiveTransaction
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> captureAsync(String transactionID) {
        if (transactionID == null) {
            return Futures.immediateFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_TRANSACTION_ID, transactionID);
        return makeRequestAsync(InteractiveMethod.CAPTURE, jsonParams);
    }

    /**
     * Claims and returns the next available packet id.
     *
     * @return  The next available packet id
     *
     * @see     InteractiveWebSocketClient#getNextPacketId()
     *
     * @since   1.0.0
     */
    private synchronized int getNextPacketId() {
        return (webSocketClient != null) ? webSocketClient.getNextPacketId() : 0;
    }

    /**
     * Returns the appropriate prepared Json object for the parameters of a {@link InteractiveMethod#READY} call.
     *
     * @param   isReady
     *          Boolean flag
     *
     * @return  If <code>true</code> returns the prepared "ready" Json object,
     *          otherwise returns the "not ready" prepared Json object
     *
     * @since   1.0.0
     */
    private JsonObject getReadyJsonObject(boolean isReady) {
        return isReady ? READY_JSON_OBJECT : NOT_READY_JSON_OBJECT;
    }

    /**
     * Retrieves all of the currently connected participants who meet the conditions of the specified method call and
     * the initial marker, sorted in the specified ordering. Only <code>getAllParticipants</code> and
     * <code>getActiveParticipants</code> calls are supported; all other method calls will throw an
     * <code>IllegalArgumentException.</code>
     *
     * @param   method
     *          The method request to send to the Interactive service
     * @param   initialMarker
     *          Initial marker to base the first request to the Interactive service off of
     * @param   comparator
     *          Comparator indicating the sort preference for the returned participants
     *
     * @return  A <code>Set</code> of <code>InteractiveParticipants</code> connected to the Interactive integration that
     *          meet the conditions of the specified method call and the initial marker, sorted in the specified
     *          ordering
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    private Set<InteractiveParticipant> getParticipants(InteractiveMethod method, long initialMarker, Comparator<InteractiveParticipant> comparator) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {

        if (method != InteractiveMethod.GET_ALL_PARTICIPANTS && method != InteractiveMethod.GET_ACTIVE_PARTICIPANTS) {
            LOG.fatal("Illegal method specified (may only be one of 'getAllParticipants' or 'getActiveParticipants')");
            throw new IllegalArgumentException("Illegal method specified (may only be one of 'getAllParticipants' or 'getActiveParticipants')");
        }

        Set<InteractiveParticipant> participants = new TreeSet<>(comparator);
        boolean hasMore = true;
        long marker = initialMarker;
        String property = (method == InteractiveMethod.GET_ALL_PARTICIPANTS ? PARAM_KEY_FROM : PARAM_KEY_THRESHOLD);

        while (hasMore) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(property, marker);
            MethodPacket requestPacket = new MethodPacket(getNextPacketId(), method, jsonObject);
            ReplyPacket replyPacket = send(requestPacket);
            if (replyPacket.hasError()) {
                throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
            }

            if (replyPacket.getResult().isJsonObject()) {
                JsonObject jsonResultObject = (JsonObject) replyPacket.getResult();
                InteractiveParticipant[] partialParticipants = GSON.fromJson(jsonResultObject.get(PARAM_KEY_PARTICIPANTS), InteractiveParticipant[].class);
                if (partialParticipants != null) {
                    Collections.addAll(participants, partialParticipants);
                    if (method == InteractiveMethod.GET_ALL_PARTICIPANTS) {
                        marker = partialParticipants[partialParticipants.length - 1].getConnectedAt();
                    }
                    else {
                        marker = partialParticipants[partialParticipants.length - 1].getLastInputAt();
                    }
                }
                hasMore = jsonResultObject.get(PARAM_KEY_HAS_MORE).getAsBoolean();
            }
        }

        return participants;
    }

    /**
     * Prepares and sends a request to the Interactive service.
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     *
     * @return <code>true</code> if the request was successful (no errors were returned), <code>false</code> otherwise
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private boolean makeRequest(InteractiveMethod method, JsonElement params) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return !(send(new MethodPacket(getNextPacketId(), method, params))).hasError();
    }

    /**
     * Prepares and sends a request to the Interactive service, returning the parsed reply.
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     * @param   type
     *          Type of object to be parsed from reply
     * @param   <T>
     *          Class of object to be parsed from the reply
     *
     * @return  A <code>T</code> object parsed from the <code>ReplyPacket</code> sent back from the Interactive service for
     *          this request
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private <T> T makeRequest(InteractiveMethod method, JsonElement params, Type type) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return send(new MethodPacket(getNextPacketId(), method, params)).getResultAs(type);
    }

    /**
     * Prepares and sends a request to the Interactive service, returning the parsed reply.
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     * @param   memberName
     *          Member name of parameter to be parsed from reply
     * @param   type
     *          Type of object to be parsed from reply
     * @param   <T>
     *          Class of object to be parsed from the reply
     *
     * @return  A <code>T</code> object parsed from the <code>ReplyPacket</code> sent back from the Interactive service for this
     *          request if it completes with no errors
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private <T> T makeRequest(InteractiveMethod method, JsonElement params, String memberName, Type type) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        ReplyPacket replyPacket = send(new MethodPacket(getNextPacketId(), method, params));
        return replyPacket.getResult().isJsonObject()
                ? GSON.fromJson(((JsonObject) replyPacket.getResult()).get(memberName),type)
                : null;
    }

    /**
     * Prepares and sends a request to the Interactive service, expecting no reply.
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private void makeRequestNoReply(InteractiveMethod method, JsonElement params) {
        sendAsync(new MethodPacket(getNextPacketId(), method, params, true));
    }

    /**
     * <p>Prepares and sends a request to the Interactive service.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} for this request
     *          if it completes with no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private ListenableFuture<Boolean> makeRequestAsync(InteractiveMethod method, JsonElement params) {
        MethodPacket requestPacket = new MethodPacket(getNextPacketId(), method, params);

        return Futures.transform(sendAsync(requestPacket), (AsyncFunction<ReplyPacket, Boolean>) replyPacket -> {
            if (replyPacket == null) {
                throw new InteractiveRequestNoReplyException(requestPacket);
            }
            if (replyPacket.hasError()) {
                throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
            }

            return Futures.immediateFuture(true);
        });
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     * @param   type
     *          Type of object to be parsed from reply
     * @param   <T>
     *          Class of object to be parsed from the reply
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>T</code> object parsed from the
     *          <code>ReplyPacket</code> sent back from the Interactive service for this request if it completes with
     *          no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private <T> ListenableFuture<T> makeRequestAsync(InteractiveMethod method, JsonElement params, Type type) {
        MethodPacket requestPacket = new MethodPacket(getNextPacketId(), method, params);
        return Futures.transform(sendAsync(requestPacket), (AsyncFunction<ReplyPacket, T>) replyPacket -> {
            if (replyPacket == null) {
                throw new InteractiveRequestNoReplyException(requestPacket);
            }
            if (replyPacket.hasError()) {
                throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
            }

            return Futures.immediateFuture(replyPacket.getResultAs(type));
        });
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     * @param   memberName
     *          Member name of parameter to be parsed from reply
     * @param   type
     *          Type of object to be parsed from reply
     * @param   <T>
     *          Class of object to be parsed from the reply
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>T</code> object parsed from the
     *          <code>ReplyPacket</code> sent back from the Interactive service for this request if it completes with
     *          no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private <T> ListenableFuture<T> makeRequestAsync(InteractiveMethod method, JsonElement params, String memberName, Type type) {
        MethodPacket requestPacket = new MethodPacket(getNextPacketId(), method, params);
        return Futures.transform(sendAsync(requestPacket), (AsyncFunction<ReplyPacket, T>) replyPacket -> {
            if (replyPacket == null) {
                throw new InteractiveRequestNoReplyException(requestPacket);
            }
            if (replyPacket.hasError()) {
                throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
            }

            return replyPacket.getResult().isJsonObject()
                    ? Futures.immediateFuture(GSON.fromJson(((JsonObject) replyPacket.getResult()).get(memberName), type))
                    : Futures.immediateFuture(null);
        });
    }

    /**
     * Sends a request to the Interactive service, returning the reply.
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     *
     * @return  The <code>ReplyPacket</code> sent back from the Interactive service for the provided method request
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private ReplyPacket send(MethodPacket requestPacket) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return send(requestPacket, DEFAULT_TIMEOUT_TIME_UNIT, DEFAULT_TIMEOUT_DURATION);
    }

    /**
     * Sends a request to the Interactive service, returning the reply. If a reply is not received within the specified
     * timeout duration, throws an <code>InteractiveRequestNoReplyException</code> exception.
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     * @param   timeoutUnit
     *          A <code>TimeUnit</code> indicating the units to be used in the timeout
     * @param   timeoutDuration
     *          Duration before request is considered timed out (no reply)
     *
     * @return  The <code>ReplyPacket</code> sent back from the Interactive service for the provided method request
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private ReplyPacket send(MethodPacket requestPacket, TimeUnit timeoutUnit, long timeoutDuration) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {

        ReplyPacket replyPacket = null;
        try {
            replyPacket = sendAsync(requestPacket, timeoutUnit, timeoutDuration).get();
        }
        catch (InterruptedException | ExecutionException e) {
            // NO-OP
        }

        if (replyPacket == null) {
            throw new InteractiveRequestNoReplyException(requestPacket);
        }
        if (replyPacket.hasError()) {
            throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
        }

        return replyPacket;
    }

    /**
     * Sends a request to the Interactive service, returning the reply. If a reply is not received within the specified
     * timeout duration, throws an <code>InteractiveRequestNoReplyException</code> exception.
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     *
     * @return  A <code>ListenableFuture</code> that when complete returns the <code>ReplyPacket</code> sent back from
     *          the Interactive service for the provided method request
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private ListenableFuture<ReplyPacket> sendAsync(MethodPacket requestPacket) {
        return sendAsync(requestPacket, DEFAULT_TIMEOUT_TIME_UNIT, DEFAULT_TIMEOUT_DURATION);
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     * @param   timeoutUnit
     *          A <code>TimeUnit</code> indicating the units to be used in the timeout
     * @param   timeoutDuration
     *          Duration before request is considered timed out (no reply)
     *
     * @return  A <code>ListenableFuture</code> that when complete returns the <code>ReplyPacket</code> sent back from
     *          the Interactive service for the provided method request
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    private ListenableFuture<ReplyPacket> sendAsync(MethodPacket requestPacket, TimeUnit timeoutUnit, long timeoutDuration) {

        if (webSocketClient == null) {
            return Futures.immediateFailedFuture(new InteractiveRequestNoReplyException(requestPacket));
        }

        String packetString = GSON.toJson(requestPacket);
        if (requestPacket.getDiscard()) {
            webSocketClient.send(packetString);
            return Futures.immediateFuture(null);
        }
        else {
            SettableFuture<ReplyPacket> sendRequest = SettableFuture.create();
            webSocketClient.getWaitingPromisesMap().put(requestPacket.getPacketID(), sendRequest);
            executor.schedule((Runnable) () -> sendRequest.setException(new InteractiveRequestNoReplyException(requestPacket)), timeoutDuration, timeoutUnit);
            webSocketClient.send(packetString);
            return sendRequest;
        }
    }
}