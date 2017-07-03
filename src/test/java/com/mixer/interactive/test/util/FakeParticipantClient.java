package com.mixer.interactive.test.util;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
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
}
