package com.mixer.interactive.test.util;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveConnectionException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A test participant is used to test the participant functionality of the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.1.0
 */
public class InteractiveTestParticipant {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Random number generator
     */
    private static final Random RANDOM = new Random();

    /**
     * Thread executor service for creating <code>CompletableFutures</code>
     */
    private final ScheduledExecutorService executor;

    /**
     * Authentication token for the test participant
     */
    private final transient String token;

    /**
     * WebSocket client that this test participant uses to communicate with the Interactive service
     */
    private InteractiveTestParticipantClient webSocketClient;

    /**
     * Constructs a new test participant with a default authentication token.
     *
     * @since   2.1.0
     */
    public InteractiveTestParticipant() {
        this("foo");
    }

    /**
     * Constructs a new test participant with the provided authentication token.
     *
     * @param   token
     *          Authentication token
     *
     * @since   2.1.0
     */
    public InteractiveTestParticipant(String token) {
        executor = Executors.newScheduledThreadPool(1);
        this.token = token;
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
     * Connects this test participant to the Interactive service we are testing.
     *
     * @return  A <code>CompletableFuture</code> that completes when the connection attempt is finished
     *
     * @since   2.1.0
     */
    public CompletableFuture<Boolean> connect() {
        CompletableFuture<Boolean> connectionPromise = new CompletableFuture<>();
        try {
            connectToInteractive();
        }
        catch (InteractiveConnectionException e) {
            connectionPromise.completeExceptionally(e);
            return connectionPromise;
        }

        executor.schedule(() -> {
            if (webSocketClient != null && webSocketClient.getConnectionPromise() != null) {
                if (webSocketClient.isOpen()) {
                    webSocketClient.getConnectionPromise().complete(true);
                }
                else {
                    webSocketClient.getConnectionPromise().completeExceptionally(new InteractiveConnectionException("Test participant connection attempt timed out after 15 seconds"));
                }
            }
        }, 15, TimeUnit.SECONDS);

        return webSocketClient.getConnectionPromise();
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
     * Supplies test participant input to the connected game client.
     *
     * @param   controlID
     *          ID for an Interactive control that is receiving input
     *
     * @since   1.0.0
     */
    public void giveInput(String controlID) {
        giveInput(controlID, "mousedown");
    }

    /**
     * Supplies test participant input to the connected game client.
     *
     * @param   controlID
     *          ID for an Interatcive control that is receiving input
     * @param   event
     *          Type of input event
     *
     * @since   1.0.0
     */
    public void giveInput(String controlID, String event) {
        giveInput(controlID, event, true);
    }

    /**
     * Supplies mock participant input to the connected game client.
     *
     * @param   controlID
     *          ID for an Interactive control that is receiving input
     * @param   event
     *          Type of input event
     * @param   discard
     *          <code>true</code> if no reply should be sent (the reply is discarded), <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public void giveInput(String controlID, String event, boolean discard) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            JsonObject params = new JsonObject();
            JsonObject input = new JsonObject();
            input.addProperty("controlID", controlID);
            input.addProperty("event", event);
            params.add("input", input);
            MethodPacket requestPacket = new MethodPacket(Math.abs(RANDOM.nextInt()), InteractiveMethod.GIVE_INPUT, params, discard);
            webSocketClient.send(GameClient.GSON.toJson(requestPacket));
        }
    }

    /**
     * Connects this test participant to the Interactive service we are testing.
     *
     * @throws  InteractiveConnectionException
     *          If there is an error connecting to Interactive
     *
     * @since   2.1.0
     */
    private void connectToInteractive() throws InteractiveConnectionException {
        if (TestUtils.API_BASE_URL.contains("localhost") || TestUtils.API_BASE_URL.contains("127.0.0.1")) {
            webSocketClient = new InteractiveTestParticipantClient(generate(), token);
        }
        else {
            ChannelResponse channelResponse = getChannelResponse();
            webSocketClient = new InteractiveTestParticipantClient(URI.create(String.format("%s&x-protocol-version=2.0&key=%s&x-auth-user=", channelResponse.socketAddress, channelResponse.key)), Integer.parseInt(channelResponse.user));
        }

        try {
            if ("wss".equals(webSocketClient.getURI().getScheme())) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
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
     * Retrieves information on how to connect this test participant to the channel being used to test participant
     * functionality.
     *
     * @return  <code>ChannelResponse</code> containing the URI that the websocket client should connect to, and what
     *          authentication key to supply when connecting
     *
     * @throws  InteractiveConnectionException
     *          If there is an error connecting to Interactive
     *
     * @since   2.1.0
     */
    private ChannelResponse getChannelResponse() throws InteractiveConnectionException {
        ChannelResponse channelResponse = null;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet channelInfoGet = new HttpGet(URI.create(TestUtils.API_BASE_URL + "interactive/" + TestUtils.CHANNEL_ID));
            channelInfoGet.addHeader("Authorization", TestUtils.OAUTH_BEARER_TOKEN.startsWith("XBL3.0") ? token : "Bearer " + token);
            channelResponse = httpClient.execute(channelInfoGet, response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    String entity = EntityUtils.toString(response.getEntity());
                    return GameClient.GSON.fromJson(entity, ChannelResponse.class);
                }
                return null;
            });
        }
        catch (IOException ex) {
            LOG.debug(ex);
        }

        if (channelResponse == null) {
            throw new InteractiveConnectionException("No channel was found");
        }
        return channelResponse;
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
        return new InteractiveParticipant("", id, "TestParticipant-" + id, RANDOM.nextInt(100), 0L,0L, false, "default");
    }
}
