package com.mixer.interactive;

import com.google.common.eventbus.EventBus;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.*;
import com.mixer.interactive.event.UndefinedInteractiveEvent;
import com.mixer.interactive.event.connection.ConnectionEstablishedEvent;
import com.mixer.interactive.event.control.ControlDeleteEvent;
import com.mixer.interactive.event.control.input.ControlInputEvent;
import com.mixer.interactive.exception.InteractiveConnectionException;
import com.mixer.interactive.exception.InteractiveNoHostsFoundException;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.gson.*;
import com.mixer.interactive.manager.StateManager;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.InteractivePacket;
import com.mixer.interactive.resources.control.InteractiveCanvasSize;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.control.InteractiveControlInput;
import com.mixer.interactive.resources.control.InteractiveControlType;
import com.mixer.interactive.resources.core.*;
import com.mixer.interactive.resources.scene.InteractiveScene;
import com.mixer.interactive.services.*;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
            .registerTypeAdapter(InteractivePacket.class, new InteractivePacketAdapter())
            .registerTypeAdapter(InteractiveScene.class, new InteractiveSceneAdapter())
            .registerTypeAdapter(InteractiveControl.class, new InteractiveControlAdapter())
            .registerTypeAdapter(InteractiveCanvasSize.class, new InteractiveCanvasSizeAdapter())
            .registerTypeAdapter(InteractiveControlType.class, new InteractiveControlTypeAdapter())
            .registerTypeAdapter(InteractiveControlInput.class, new InteractiveControlInputAdapter())
            .registerTypeAdapter(UndefinedInteractiveEvent.class, new UndefinedInteractiveEventAdapter())
            .registerTypeAdapter(ControlInputEvent.class, new ControlInputEventAdapter())
            .registerTypeAdapter(ControlDeleteEvent.class, new ControlDeleteEventAdapter())
            .serializeNulls()
            .create();

    /**
     * Collection of service providers
     */
    public static final Class<RemoteProcedureCallServiceProvider> RPC_SERVICE_PROVIDER = RemoteProcedureCallServiceProvider.class;
    public static final Class<ParticipantServiceProvider> PARTICIPANT_SERVICE_PROVIDER = ParticipantServiceProvider.class;
    public static final Class<GroupServiceProvider> GROUP_SERVICE_PROVIDER = GroupServiceProvider.class;
    public static final Class<SceneServiceProvider> SCENE_SERVICE_PROVIDER = SceneServiceProvider.class;
    public static final Class<ControlServiceProvider> CONTROL_SERVICE_PROVIDER = ControlServiceProvider.class;
    public static final Class<TransactionServiceProvider> TRANSACTION_SERVICE_PROVIDER = TransactionServiceProvider.class;

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

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
     * The default duration for timing out connection attempts.
     */
    private static final int CONNECTION_TIMEOUT_DURATION = 5;

    /**
     * The default time unit for timing out connection attempts.
     */
    private static final TimeUnit CONNECTION_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * Parameter key name for <code>ready</code> method
     */
    private static final String PARAM_KEY_IS_READY = "isReady";

    /**
     * Parameter key name for <code>setCompression</code> method
     */
    private static final String PARAM_KEY_COMPRESSION_SCHEME = "scheme";

    /**
     * Parameter key name for <code>getTime</code> method
     */
    private static final String PARAM_KEY_TIME = "time";

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
     * The OAuth client id for the developer using this game client
     */
    private final String clientId;

    /**
     * Service manager containing all server providers for the client
     */
    private final ServiceManager<AbstractServiceProvider> serviceManager;

    /**
     * Event bus where incoming events from the Interactive service are posted to
     */
    private final EventBus eventBus;

    /**
     * Thread executor service for creating <code>CompletableFutures</code>
     */
    private final ScheduledExecutorService executor;

    /**
     * WebSocket client that this game client uses to communicate with the Interactive service
     */
    private InteractiveWebSocketClient webSocketClient;

    /**
     * Manages the state cache for the game client
     */
    private StateManager stateManager;

    /**
     * Initializes a new <code>GameClient</code>.
     *
     * @param   projectVersionId
     *          The project version ID for the Interactive integration the client will use
     * @param   clientId
     *          The OAuth client id for the developer using this game client
     *
     * @since   1.0.0
     */
    public GameClient(Number projectVersionId, String clientId) {
        this(projectVersionId, clientId, true);
    }

    /**
     * Initializes a new <code>GameClient</code>.
     *
     * @param   projectVersionId
     *          The project version ID for the Interactive integration the client will use
     * @param   clientId
     *          The OAuth client id for the developer using this game client
     * @param   useStateManager
     *          Whether or not to use built in caching for the game client
     *
     * @since   2.1.0
     */
    public GameClient(Number projectVersionId, String clientId, boolean useStateManager) {
        this.projectVersionId = projectVersionId;
        this.clientId = clientId;

        serviceManager = new ServiceManager<>(this);
        registerServiceProviders();

        stateManager = new StateManager(this);
        eventBus = new EventBus(projectVersionId.toString());
        if (useStateManager) {
            eventBus.register(stateManager);
        }
        executor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE, new ThreadFactoryBuilder()
                .setNameFormat("interactive-project-" + this.projectVersionId + "-thread-%d")
                .setDaemon(true)
                .build());
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
     * Returns the <code>ServiceManager</code> associated with the client.
     *
     * @return  The <code>ServiceManager</code> associated with the client
     *
     * @since   2.1.0
     */
    public ServiceManager<AbstractServiceProvider> getServiceManager() {
        return serviceManager;
    }

    /**
     * Gets the StateManager for this game client.
     *
     * @return  The StateManager for this game client
     *
     * @since   2.1.0
     */
    public StateManager getStateManager() {
        return stateManager;
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
     * Returns the web socket client that this game client uses to communicate with the Interactive service.
     *
     * @return  The web socket client that this game client uses to communicate with the Interactive service.
     *
     * @since   1.0.0
     */
    public InteractiveWebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    /**
     * Returns the thread executor service used for creating <code>CompletableFutures</code>.
     *
     * @return  The thread executor service used for creating <code>CompletableFutures</code>.
     *
     * @since   1.0.0
     */
    public ScheduledExecutorService getExecutorService() {
        return executor;
    }

    /**
     * Retrieves the service provider specified by the provided class from the service manager. If there does not exist
     * a service provider instance for the provided class, <code>null</code> is returned.
     *
     * @param   clazz
     *          Class of service provider to be used
     * @param   <T>
     *          Type of service provider to be used
     *
     * @return  The service provider instance from the service manager. If there does not exist
     *          a service provider instance for the provided class, <code>null</code> is returned.
     *
     * @since   1.0.0
     */
    public <T extends AbstractServiceProvider> T using(Class<T> clazz) {
        return getServiceManager().get(clazz);
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
     * Connects the game client to it's associated Interactive integration on the Interactive service, using either an
     * OAuth Bearer token or an xtoken to authenticate itself with the Interactive service.
     *
     * @param   token
     *          Authentication token
     *
     * @return  A <code>CompletableFuture</code> that completes when the connection attempt is finished
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> connect(String token) {
        return connect(token, null);
    }

    /**
     * Connects the game client to it's associated Interactive integration on the Interactive service, using either an
     * OAuth Bearer token or an xtoken to authenticate itself with the Interactive service and the appropriate share
     * code for the integration.
     *
     * Connections to Interactive hosts are attempted in the order of hosts that are returned from the
     * <code>interactive/hosts</code> endpoint. If no connection succeeds, then the future returns an
     * <code>InteractiveConnectionException</code>.
     *
     * @param   authToken
     *          Authentication token
     * @param   shareCode
     *          The share code provided by the author of the Interactive integration
     *
     * @return  A <code>CompletableFuture</code> that completes when the connection attempt is finished
     *
     * @since   2.1.0
     */
    public CompletableFuture<Boolean> connect(String authToken, String shareCode) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        ArrayList<InteractiveHost> hosts = new ArrayList<>();
        try {
            hosts.addAll(EndpointUtil.getInteractiveHosts(clientId));
        } catch (InteractiveNoHostsFoundException e) {
            result.completeExceptionally(e);
        }
        Iterator<InteractiveHost> hostIterator = hosts.iterator();
        if (hostIterator.hasNext()) {
            connectTo(authToken, shareCode, hostIterator.next().getAddress()).whenCompleteAsync((r, t) -> {
                if (t == null) {
                    result.complete(r);
                }
                else {
                    while (hostIterator.hasNext()) {
                        try {
                            r = connectTo(authToken, shareCode, hostIterator.next().getAddress()).join();
                            result.complete(r);
                            return;
                        }
                        catch (Throwable next) {
                            t.addSuppressed(next);
                        }
                    }
                    result.completeExceptionally(t);
                }
            });
        }
        else {
            result.completeExceptionally(new InteractiveNoHostsFoundException());
        }
        return result;
    }

    /**
     * Connects the game client to it's associated Interactive integration on a specific Interactive service host,
     * using either an OAuth Bearer token or a xtoken to authenticate itself with the Interactive service.
     *
     * @param   authToken
     *          Authentication token
     * @param   interactiveHost
     *          <code>URI</code> for an Interactive service host
     *
     * @return  A <code>CompletableFuture</code> that completes when the connection attempt is finished
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> connectTo(String authToken, URI interactiveHost) {
        return connectTo(authToken, null, interactiveHost);
    }

    /**
     * Connects the game client to it's associated Interactive integration on a specific Interactive service host,
     * using either an OAuth Bearer token or a xtoken to authenticate itself with the Interactive service and the
     * appropriate share code for the integration.
     *
     * @param   authToken
     *          Authentication token
     * @param   shareCode
     *          The share code provided by the author of the Interactive integration
     * @param   interactiveHost
     *          <code>URI</code> for an Interactive service host
     *
     * @return  A <code>CompletableFuture</code> that completes when the connection attempt is finished
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> connectTo(String authToken, String shareCode, URI interactiveHost) {
        CompletableFuture<Boolean> connectionPromise = new CompletableFuture<>();
        try {
            connect(authToken, shareCode, interactiveHost);
        }
        catch (InteractiveNoHostsFoundException e) {
            connectionPromise.completeExceptionally(e);
            return connectionPromise;
        }

        this.getExecutorService().schedule(() -> {
            if (webSocketClient != null && webSocketClient.getConnectionPromise() != null) {
                if (webSocketClient.isOpen()) {
                    webSocketClient.getConnectionPromise().complete(true);
                    eventBus.post(new ConnectionEstablishedEvent(projectVersionId, interactiveHost));
                }
                else {
                    webSocketClient.getConnectionPromise().completeExceptionally(new InteractiveConnectionException(String.format("Connection attempt to host '%s' timed out after %s %s", interactiveHost, CONNECTION_TIMEOUT_DURATION, CONNECTION_TIMEOUT_UNIT.name().toLowerCase())));
                }
            }
        }, CONNECTION_TIMEOUT_DURATION, CONNECTION_TIMEOUT_UNIT);

        return webSocketClient.getConnectionPromise();
    }

    /**
     * Connects the game client to it's associated Interactive integration on a specific Interactive service host,
     * using either an OAuth Bearer token or xtoken to authenticate itself with the Interactive service and the
     * appropriate share code for the integration.
     *
     * @param   authToken
     *          Authentication token
     * @param   shareCode
     *          The share code provided by the author of the Interactive integration
     * @param   hostURI
     *          <code>URI</code> for an Interactive service host
     *
     * @since   1.0.0
     */
    private void connect(String authToken, String shareCode, URI hostURI) throws InteractiveNoHostsFoundException {
        URI interactiveHost = hostURI != null ? hostURI : EndpointUtil.getInteractiveHost(clientId).getAddress();
        String token = authToken != null ? authToken : "";

        if (shareCode != null && !shareCode.isEmpty()) {
            webSocketClient = new InteractiveWebSocketClient(this, interactiveHost, token, projectVersionId, shareCode);
        }
        else {
            webSocketClient = new InteractiveWebSocketClient(this, interactiveHost, token, projectVersionId);
        }

        try {
            if (SECURE_WEBSOCKET_SCHEME.equals(webSocketClient.getURI().getScheme())) {
                SSLContext sslContext = SSLContext.getInstance(TLS_INSTANCE);
                sslContext.init(null, null, null);
                webSocketClient.setSocket(sslContext.getSocketFactory().createSocket());
            }
            webSocketClient.setConnectionPromise(new CompletableFuture<>());
            webSocketClient.connect();
        }
        catch (NoSuchAlgorithmException | IOException | KeyManagementException e) {
            LOG.error(e.getMessage(), e);
            webSocketClient = null;
        }
    }

    /**
     * Disconnects the client from the Interactive service.
     *
     * @return  A <code>CompletableFuture</code> that completes when the client has been disconnected from the
     *          Interactive service
     *
     * @since   1.0.0
     */
    public CompletableFuture<Void> disconnect() {
        return CompletableFuture.runAsync(() -> {
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
        });
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
        using(RPC_SERVICE_PROVIDER).makeRequestNoReply(InteractiveMethod.READY, isReady ? READY_JSON_OBJECT : NOT_READY_JSON_OBJECT);
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
     * @see     CompressionScheme
     *
     * @since   1.0.0
     */
    public CompletableFuture<CompressionScheme> setCompression(CompressionScheme ... schemes) {
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
     *          An array of preferred compression schemes in order of preference, from greatest to
     *          least preference
     *
     * @return  The new <code>CompressionScheme</code> that the client is to use to communicate with
     *          the Interactive service
     *
     * @see     CompressionScheme
     *
     * @since   1.0.0
     */
    public CompletableFuture<CompressionScheme> setCompression(String ... schemes) {
        return setCompression(Arrays.asList(schemes));
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
     * @see     CompressionScheme
     *
     * @since   1.0.0
     */
    public CompletableFuture<CompressionScheme> setCompression(Collection<String> schemes) {
        JsonObject jsonParams = new JsonObject();
        List<String> compressionSchemes = new ArrayList<>();
        for (String scheme : schemes) {
            if (CompressionScheme.from(scheme) != null && !compressionSchemes.contains(scheme)) {
                compressionSchemes.add(scheme);
            }
        }
        jsonParams.add(PARAM_KEY_COMPRESSION_SCHEME, GSON.toJsonTree(compressionSchemes));

        CompletableFuture<String> future = using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.SET_COMPRESSION, jsonParams, PARAM_KEY_COMPRESSION_SCHEME, String.class);
        CompletableFuture<CompressionScheme> compressionFuture = future.thenApply(CompressionScheme::from);
        compressionFuture.thenAcceptAsync(compressionScheme -> webSocketClient.setCompressionScheme(compressionScheme));
        return compressionFuture;
    }

    /**
     * <p>Retrieves the current server time from the Interactive service, given as a milliseconds UTC unix timestamp.
     * </p>
     *
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>CompletableFuture</code>.</p>
     *
     * @return  A <code>CompletableFuture</code> that when complete returns the Interactive service's current server
     *          time, given as a UTC unix timestamp (in milliseconds)
     *
     * @since   1.0.0
     */
    public CompletableFuture<Long> getTime() {
        return using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.GET_TIME, EMPTY_JSON_OBJECT, PARAM_KEY_TIME, Long.class);
    }

    /**
     * <p>Retrieves the current memory usage for the Interactive integration this client is connected to on the
     * Interactive service. The memory usage returned is a dump of information regarding current memory allocations, as
     * well as a breakdown of how much memory is allocated where. This information is provided for debugging purposes.
     * </p>
     *
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>CompletableFuture</code>.</p>
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>InteractiveMemoryStatistic</code>
     *          representing the current memory usage of the Interactive integration the client is connected to
     *
     * @see     InteractiveMemoryStatistic
     *
     * @since   1.0.0
     */
    public CompletableFuture<InteractiveMemoryStatistic> getMemoryStats() {
        return using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.GET_MEMORY_STATS, EMPTY_JSON_OBJECT, InteractiveMemoryStatistic.class);
    }

    /**
     * <p>Retrieves statistics on the state of throttling rules set up in
     * {@link InteractiveMethod#SET_BANDWIDTH_THROTTLE} method requests. It returns the number of sent packets (ones
     * inserted into the bucket) and the number of rejected packets for each method that has a throttle set.</p>
     *
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>CompletableFuture</code>.</p>
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> of
     *          <code>InteractiveMethods</code> and their associated <code>ThrottleState</code>
     *
     * @see     BandwidthThrottle
     * @see     InteractiveMethod
     * @see     ThrottleState
     *
     * @since   1.0.0
     */
    public CompletableFuture<Map<InteractiveMethod, ThrottleState>> getThrottleState() {
        return using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.GET_THROTTLE_STATE, JsonNull.INSTANCE, THROTTLE_STATE_MAP_TYPE);
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
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>CompletableFuture</code>.</p>
     *
     * @param   throttleMap
     *          A <code>Map</code> of <code>InteractiveMethods</code> and their associated
     *          <code>BandwidthThrottle</code>
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#SET_BANDWIDTH_THROTTLE setBandwidthThrottle} method call completes with no
     *          errors
     *
     * @see     BandwidthThrottle
     * @see     InteractiveMethod
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> setBandwidthThrottle(Map<InteractiveMethod, BandwidthThrottle> throttleMap) {
        return throttleMap != null
                ? using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.SET_BANDWIDTH_THROTTLE, GSON.toJsonTree(throttleMap))
                : CompletableFuture.completedFuture(true);
    }

    /**
     * Registers all SDK provided service providers with the client.
     *
     * @since   1.0.0
     */
    private void registerServiceProviders() {
        serviceManager.register(RPC_SERVICE_PROVIDER);
        serviceManager.register(PARTICIPANT_SERVICE_PROVIDER);
        serviceManager.register(GROUP_SERVICE_PROVIDER);
        serviceManager.register(SCENE_SERVICE_PROVIDER);
        serviceManager.register(CONTROL_SERVICE_PROVIDER);
        serviceManager.register(TRANSACTION_SERVICE_PROVIDER);
    }
}
