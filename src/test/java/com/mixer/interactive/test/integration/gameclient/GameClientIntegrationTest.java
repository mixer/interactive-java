package com.mixer.interactive.test.integration.gameclient;

import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveConnectionException;
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
    public static void setup_class() {
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
        try {
            Assert.assertEquals("The game client can connect with an OAuth token", true, gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).get());
        }
        catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_connect_with_null_oauth_token() {
        try {
            gameClient.connect(null, INTERACTIVE_SERVICE_URI).get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveConnectionException) {
                Assert.assertEquals("Cannot connect to Interactive with a null token", 4019, ((InteractiveConnectionException) e.getCause()).getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_connect_with_empty_oauth_token() {
        try {
            gameClient.connect("", INTERACTIVE_SERVICE_URI).get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveConnectionException) {
                Assert.assertEquals("Cannot connect to Interactive with an empty String as the token", 4019, ((InteractiveConnectionException) e.getCause()).getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_connect_with_invalid_oauth_token() {
        try {
            gameClient.connect("afafpafjafhakjcn;avn74739i3jfnf", INTERACTIVE_SERVICE_URI).get();
            Assert.fail("Exception should have been thrown");
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveConnectionException) {
                Assert.assertEquals("Cannot connect to Interactive with invalid token", 4019, ((InteractiveConnectionException) e.getCause()).getErrorCode());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void cannot_connect_to_empty_string_host_uri() {
        try {
            gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create("")).get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveConnectionException) {
                Assert.assertEquals("Cannot connect to Interactive with an empty String for the host URL", false, gameClient.isConnected());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_connect_to_non_interactive_host() {
        try {
            gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create("ws://mixer.com/")).get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveConnectionException) {
                Assert.assertEquals("Cannot connect to host that is not an Interactive host", false, gameClient.isConnected());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void cannot_connect_using_invalid_scheme() {
        try {
            gameClient.connect(OAUTH_BEARER_TOKEN, null, URI.create("https://mixer.com/")).get();
            Assert.fail("Exception should have been thrown");
        }
        catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof InteractiveConnectionException) {
                Assert.assertEquals("Cannot connect to Interactive with invalid protocol", false, gameClient.isConnected());
            }
            else {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void can_disconnect() {
        gameClient.connect(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI).join();
        Assert.assertEquals("Connected to Interactive", true, gameClient.isConnected());
        gameClient.disconnect().join();
        Assert.assertEquals("Disconnected from Interactive", false, gameClient.isConnected());
    }
}