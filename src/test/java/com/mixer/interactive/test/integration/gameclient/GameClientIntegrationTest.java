package com.mixer.interactive.test.integration.gameclient;

import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveNoHostsFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests connection functionality between <code>GameClient</code> and the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GameClientIntegrationTest {

    /**
     * Logger
     */
    private static Logger LOG = LogManager.getLogger();

    /**
     * The <code>GameClient</code> that this test will use
     */
    private static GameClient gameClient;

    @BeforeClass
    public static void setup_class() throws InteractiveNoHostsFoundException {
        LOG.warn("NOTE - RuntimeExceptions will be logged as part of this testing. They are not indications that tests are failing.");
    }

    @Before
    public void setup_test() {
        gameClient = new GameClient(INTERACTIVE_PROJECT_ID);
    }

    @After
    public void teardown_test() {
        gameClient.disconnect().join();
        gameClient = null;
    }

    @Test
    public void can_connect_with_oauth_token() {
        gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).join();
        Assert.assertEquals("Can connect to Interactive using oauth token", true, gameClient.isConnected());
    }

    @Test
    public void cannot_connect_with_null_oauth_token() {
        gameClient.connect(null, INTERACTIVE_SERVICE_URI).join();
        Assert.assertEquals("Cannot connect to Interactive with a null token", false, gameClient.isConnected());
    }

    @Test
    public void cannot_connect_with_empty_oauth_token() {
        gameClient.connect("", INTERACTIVE_SERVICE_URI).join();
        Assert.assertEquals("Cannot connect to Interactive with an empty String as the token", false, gameClient.isConnected());
    }

    @Test
    public void cannot_connect_with_invalid_oauth_token() {
        gameClient.connect("afafpafjafhakjcn;avn74739i3jfnf", INTERACTIVE_SERVICE_URI).join();
        waitForWebSocket();
        Assert.assertEquals("Cannot connect to Interactive with invalid token", false, gameClient.isConnected());
    }

    @Test
    public void cannot_connect_to_null_host_uri() {
        try {
            gameClient.connect(OAUTH_BEARER_TOKEN, null, null).get();
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveNoHostsFoundException) {
                Assert.assertEquals("Cannot connect to Interactive with null as the host URL", false, gameClient.isConnected());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_connect_to_empty_string_host_uri() {
        gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create("")).join();
        Assert.assertEquals("Cannot connect to Interactive with an empty String for the host URL", false, gameClient.isConnected());
    }

    @Test
    public void cannot_connect_to_non_interactive_host() {
        gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create("ws://mixer.com/")).join();
        Assert.assertEquals("Cannot connect to host that is not an Interactive host", false, gameClient.isConnected());
    }

    @Test
    public void cannot_connect_using_invalid_scheme() {
        gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create("https://mixer.com/")).join();
        Assert.assertEquals("Cannot connect to Interactive with invalid protocol", false, gameClient.isConnected());
    }

    @Test
    public void can_disconnect() {
        gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).join();
        Assert.assertEquals("Connected to Interactive", true, gameClient.isConnected());
        gameClient.disconnect().join();
        Assert.assertEquals("Disconnected from Interactive", false, gameClient.isConnected());
    }
}