package com.mixer.interactive.test.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * Constants used for testing in multiple test suites. Wherever possible constants are read from a properties file.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class TestUtils {

    /**
     * Property file name
     */
    private static final String PROPERTY_FILE_NAME = "interactive-project.json";

    /**
     * Property key for retrieving the <code>testLocal</code> setting from the property file
     */
    private static final String PROPERTY_KEY_TEST_LOCAL = "testLocal";

    /**
     * Property key for retrieving the <code>projectVersion</code> setting for an empty Interactive integration from the
     * property file
     */
    private static final String PROPERTY_KEY_EMPTY_PROJECT_VERSION = "emptyProjectVersion";

    /**
     * Property key for retrieving the <code>projectVersion</code> setting for an empty Interactive integration from the
     * property file
     */
    private static final String PROPERTY_KEY_SINGLE_SCENE_PROJECT_VERSION = "singleSceneProjectVersion";

    /**
     * Property key for retrieving the <code>projectVersion</code> setting for an empty Interactive integration from the
     * property file
     */
    private static final String PROPERTY_KEY_MULTIPLE_SCENE_PROJECT_VERSION = "multiSceneProjectVersion";

    /**
     * Property key for retrieving the <code>oauthToken</code> setting from the property file
     */
    private static final String PROPERTY_KEY_OAUTH_TOKEN = "oauthToken";

    /**
     * URI for localhost testing
     */
    public static final URI INTERACTIVE_LOCALHOST = URI.create("ws://127.0.0.1:3000/gameClient");

    /**
     * Whether or not to test against a locally running Interactive service
     */
    public static final boolean TEST_LOCAL;

    /**
     * Project version ID for an Interactive integration with no content
     */
    public static final int EMPTY_INTERACTIVE_PROJECT;

    /**
     * Project version ID for an Interactive integration with a single scene, populated with controls
     */
    public static final int SINGLE_SCENE_INTERACTIVE_PROJECT;

    /**
     * Project version ID for an Interactive integration with multiple scenes, populated with controls
     */
    public static final int MULTIPLE_SCENES_INTERACTIVE_PROJECT;

    /**
     * OAuth Bearer token
     */
    public static final String OAUTH_BEARER_TOKEN;

    static {
        URL propertyFileURL = ClassLoader.getSystemClassLoader().getResource(PROPERTY_FILE_NAME);
        JsonElement jsonElement = null;

        if (propertyFileURL != null) {
            File propertyFile = new File(propertyFileURL.getFile());
            try (JsonReader jsonReader = new JsonReader(new FileReader(propertyFile))) {
                jsonElement = new JsonParser().parse(jsonReader);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (jsonElement != null) {
            TEST_LOCAL = ((JsonObject) jsonElement).get(PROPERTY_KEY_TEST_LOCAL).getAsBoolean();
            EMPTY_INTERACTIVE_PROJECT = ((JsonObject) jsonElement).get(PROPERTY_KEY_EMPTY_PROJECT_VERSION).getAsInt();
            SINGLE_SCENE_INTERACTIVE_PROJECT = ((JsonObject) jsonElement).get(PROPERTY_KEY_SINGLE_SCENE_PROJECT_VERSION).getAsInt();
            MULTIPLE_SCENES_INTERACTIVE_PROJECT = ((JsonObject) jsonElement).get(PROPERTY_KEY_MULTIPLE_SCENE_PROJECT_VERSION).getAsInt();
            OAUTH_BEARER_TOKEN = ((JsonObject) jsonElement).get(PROPERTY_KEY_OAUTH_TOKEN).getAsString();
        }
        else {
            TEST_LOCAL = true;
            EMPTY_INTERACTIVE_PROJECT = 1000;
            SINGLE_SCENE_INTERACTIVE_PROJECT = 1001;
            MULTIPLE_SCENES_INTERACTIVE_PROJECT = 1002;
            OAUTH_BEARER_TOKEN = "foo";
        }
    }

    /**
     * Waits a period of time to give the <code>InteractiveWebSocketClient</code> time to receive input
     * from the Interactive service prior to executing any assertions.
     *
     * @since 1.0.0
     */
    public static void waitForWebSocket() {
        try {
            Thread.sleep(75);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
