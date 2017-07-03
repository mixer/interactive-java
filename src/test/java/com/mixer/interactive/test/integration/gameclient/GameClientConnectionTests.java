package com.mixer.interactive.test.integration.gameclient;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveNoHostsFoundException;
import com.mixer.interactive.util.EndpointUtil;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.net.URI;

import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests connection functionality between <code>GameClient</code> and the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GameClientConnectionTests {

    /**
     * URI for Interactive host being tested against
     */
    private static URI interactiveHost;

    /**
     * The <code>GameClient</code> that this test will use
     */
    private static GameClient gameClient;

    @BeforeClass
    public static void setup() throws InteractiveNoHostsFoundException {
        interactiveHost = INTERACTIVE_LOCALHOST;
        if (!TEST_LOCAL) {
            interactiveHost = EndpointUtil.getInteractiveHost().getAddress();
        }
    }

    @Before
    public void setupGameClient() {
        gameClient = new GameClient(EMPTY_INTERACTIVE_PROJECT);
    }

    @After
    public void teardownGameClient() {
        gameClient.disconnect();
        gameClient = null;
    }

    /**********         Testing GameClient#connect                  **********/
    @Test
    public void connect_valid() {
        gameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
        waitForWebSocket();
        Assert.assertTrue(gameClient.isConnected());
    }

    @Test
    public void connect_invalid_null_oauth_token() {
        gameClient.connect(null, interactiveHost);
        waitForWebSocket();
        Assert.assertFalse(gameClient.isConnected());
    }

    @Test
    public void connect_invalid_empty_oauth_token() {
        gameClient.connect("");
        waitForWebSocket();
        Assert.assertFalse(gameClient.isConnected());
    }

    @Test
    public void connect_invalid_random_oauth_token() {
        gameClient.connect("afafpafjafhakjcn;avn74739i3jfnf", interactiveHost);
        waitForWebSocket();
        Assert.assertFalse(gameClient.isConnected());
    }

    @Test
    public void connect_invalid_null_interactive_host() {
        gameClient.connect(OAUTH_BEARER_TOKEN, null, null);
        waitForWebSocket();
        Assert.assertFalse(gameClient.isConnected());
    }

    @Test
    public void connect_invalid_empty_interactive_host() {
        gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create(""));
        waitForWebSocket();
        Assert.assertFalse(gameClient.isConnected());
    }

    @Test
    public void connect_invalid_not_an_interactive_host() {
        gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create("ws://mixer.com/"));
        waitForWebSocket();
        Assert.assertFalse(gameClient.isConnected());
    }

    @Test
    public void connect_invalid_scheme_for_interactive_host() {
        gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create("https://mixer.com/"));
        waitForWebSocket();
        Assert.assertFalse(gameClient.isConnected());
    }

    /**********         Testing GameClient#connectAsync             **********/
    @Test
    public void connectAsync_valid() {
        ListenableFuture<?> connectPromise = gameClient.connectAsync(OAUTH_BEARER_TOKEN, interactiveHost);
        Futures.addCallback(connectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertTrue(gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail(t.getMessage());
            }
        });
    }

    @Test
    public void connectAsync_invalid_null_oauth_token() {
        ListenableFuture<?> connectPromise = gameClient.connectAsync(null, interactiveHost);
        Futures.addCallback(connectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertFalse(gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail(t.getMessage());
            }
        });
    }

    @Test
    public void connectAsync_invalid_empty_oauth_token() {
        ListenableFuture<?> connectPromise = gameClient.connectAsync("", interactiveHost);
        Futures.addCallback(connectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertFalse(gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail(t.getMessage());
            }
        });
    }

    @Test
    public void connectAsync_invalid_random_oauth_token() {
        ListenableFuture<?> connectPromise = gameClient.connectAsync("afafpafjafhakjcn;avn74739i3jfnf", interactiveHost);
        Futures.addCallback(connectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertFalse(gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail(t.getMessage());
            }
        });
    }

    @Test
    public void connectAsync_invalid_null_interactive_host() {
        ListenableFuture<?> connectPromise = gameClient.connectAsync(OAUTH_BEARER_TOKEN, null, null);
        Futures.addCallback(connectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertFalse(gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail(t.getMessage());
            }
        });
    }

    @Test
    public void connectAsync_invalid_empty_interactive_host() {
        ListenableFuture<?> connectPromise = gameClient.connectAsync(OAUTH_BEARER_TOKEN, null, URI.create(""));
        Futures.addCallback(connectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertFalse(gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail(t.getMessage());
            }
        });
    }

    @Test
    public void connectAsync_invalid_not_an_interactive_host() {
        ListenableFuture<?> connectPromise = gameClient.connectAsync(OAUTH_BEARER_TOKEN, null, URI.create("ws://mixer.com/"));
        Futures.addCallback(connectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertFalse(gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail(t.getMessage());
            }
        });
    }

    @Test
    public void connectAsync_invalid_scheme_for_interactive_host() {
        ListenableFuture<?> connectPromise = gameClient.connectAsync(OAUTH_BEARER_TOKEN, null, URI.create("https://mixer.com/"));
        Futures.addCallback(connectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertTrue(!gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail(t.getMessage());
            }
        });
    }

    /**********         Testing GameClient#disconnect               **********/

    @Test
    public void disconnect() {
        gameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
        waitForWebSocket();
        Assert.assertTrue(gameClient.isConnected());
        gameClient.disconnect();
        Assert.assertFalse(gameClient.isConnected());
    }

    /**********         Testing GameClient#disconnectAsync          **********/

    @Test
    public void disconnectAsync() {
        gameClient.connect(OAUTH_BEARER_TOKEN, interactiveHost);
        waitForWebSocket();
        Assert.assertTrue(gameClient.isConnected());
        ListenableFuture<?> disconnectPromise = gameClient.connectAsync(OAUTH_BEARER_TOKEN, interactiveHost);
        Futures.addCallback(disconnectPromise, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assert.assertFalse(gameClient.isConnected());
            }

            @Override
            public void onFailure(Throwable t) {
                Assert.fail();
            }
        });
    }
}
