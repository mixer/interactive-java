package com.mixer.interactive.test.integration.manager;

import com.mixer.interactive.GameClient;
import com.mixer.interactive.test.util.TestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.concurrent.ExecutionException;

import static com.mixer.interactive.test.util.TestUtils.*;

/**
 * Tests state manager functionality.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StateManagerIntegrationTests {

    /**
     * Logger
     */
    private static Logger LOG = LogManager.getLogger();

    /**
     * The <code>GameClient</code> that this test will use
     */
    private static GameClient gameClient;

    @Before
    public void setup_test() {
        gameClient = new GameClient(INTERACTIVE_PROJECT_ID, TestUtils.CLIENT_ID);
    }

    @After
    public void teardown_test() {
        gameClient.disconnect().join();
        gameClient = null;
    }

    @Test
    public void can_calculate_time_adjustment() {
        try {
            gameClient.connectTo(OAUTH_BEARER_TOKEN, INTERACTIVE_SERVICE_URI)
                    .thenRunAsync(() -> {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).get();

            Assert.assertNotEquals("The state manager has calculated a time adjustment", 0, gameClient.getStateManager().getTimeAdjustment());
        } catch (InterruptedException | ExecutionException e) {
            Assert.fail(e.getMessage());
        }
    }
}
