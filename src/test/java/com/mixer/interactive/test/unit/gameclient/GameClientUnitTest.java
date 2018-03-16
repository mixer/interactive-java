package com.mixer.interactive.test.unit.gameclient;

import com.mixer.interactive.GameClient;
import com.mixer.interactive.test.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for <code>GameClient</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class GameClientUnitTest {

    /**
     * The project version ID for the Interactive integration that this test will use
     */
    private static final int PROJECT_VERSION_ID = 1234;

    /**
     * The <code>GameClient</code> that this test will use
     */
    private static GameClient gameClient = new GameClient(PROJECT_VERSION_ID, TestUtils.CLIENT_ID);

    @Test
    public void testProjectVersionID() {
        Assert.assertTrue(gameClient.getProjectVersionId().equals(PROJECT_VERSION_ID));
    }
}
