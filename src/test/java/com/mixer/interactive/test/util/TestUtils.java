package com.mixer.interactive.test.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mixer.interactive.exception.InteractiveNoHostsFoundException;
import com.mixer.interactive.util.EndpointUtil;

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
     * Property key for retrieving the <code>SERVICE_URI</code> property
     */
    private static final String PROPERTY_KEY_INTERACTIVE_SERVICE = "SERVICE_URI";

    /**
     * Property key for retrieving the <code>PROJECT_ID</code> property
     */
    private static final String PROPERTY_KEY_PROJECT_ID = "PROJECT_ID";

    /**
     * Property key for retrieving the <code>OAUTH_TOKEN</code> property
     */
    private static final String PROPERTY_KEY_OAUTH_TOKEN = "OAUTH_TOKEN";

    /**
     * Property key for retrieving the <code>CHANNEL_ID</code> property
     */
    private static final String PROPERTY_KEY_CHANNEL_ID = "CHANNEL_ID";

    /**
     * URI for localhost testing
     */
    private static final URI INTERACTIVE_LOCALHOST = URI.create("ws://localhost:3000/gameClient");

    /**
     * The URI of the Interactive service used for testing
     */
    public static final URI INTERACTIVE_SERVICE_URI;

    /**
     * The URI that mock participants connect to
     */
    static final URI INTERACTIVE_PARTICIPANT_URI;

    /**
     * Project version ID for the testing Interactive integration
     */
    public static final int INTERACTIVE_PROJECT_ID;

    /**
     * OAuth Bearer token
     */
    public static final String OAUTH_BEARER_TOKEN;

    /**
     * Channel Id that mock participants will connect to
     */
    private static final int CHANNEL_ID;

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

        if (System.getenv(PROPERTY_KEY_INTERACTIVE_SERVICE) != null) {
            INTERACTIVE_SERVICE_URI = URI.create(System.getenv(PROPERTY_KEY_INTERACTIVE_SERVICE));
        }
        else {
            if (jsonElement != null && jsonElement instanceof JsonObject && ((JsonObject) jsonElement).get(PROPERTY_KEY_INTERACTIVE_SERVICE) != null) {
                INTERACTIVE_SERVICE_URI = URI.create(((JsonObject) jsonElement).get(PROPERTY_KEY_INTERACTIVE_SERVICE).getAsString());
            }
            else {
                URI serviceHost;
                try {
                    serviceHost = EndpointUtil.getInteractiveHost().getAddress();
                }
                catch (InteractiveNoHostsFoundException e) {
                    serviceHost = INTERACTIVE_LOCALHOST;
                }
                INTERACTIVE_SERVICE_URI = serviceHost;
            }
        }

        OAUTH_BEARER_TOKEN = System.getenv(PROPERTY_KEY_OAUTH_TOKEN) != null
                ? System.getenv(PROPERTY_KEY_OAUTH_TOKEN)
                : (jsonElement != null && jsonElement instanceof JsonObject && ((JsonObject) jsonElement).get(PROPERTY_KEY_OAUTH_TOKEN) != null
                    ? ((JsonObject) jsonElement).get(PROPERTY_KEY_OAUTH_TOKEN).getAsString()
                    : "foo");

        INTERACTIVE_PROJECT_ID = System.getenv(PROPERTY_KEY_PROJECT_ID) != null
                ? Integer.parseInt(System.getenv(PROPERTY_KEY_PROJECT_ID))
                : (jsonElement != null && jsonElement instanceof JsonObject && ((JsonObject) jsonElement).get(PROPERTY_KEY_PROJECT_ID) != null
                    ? ((JsonObject) jsonElement).get(PROPERTY_KEY_PROJECT_ID).getAsInt()
                    : 1000);

        CHANNEL_ID = System.getenv(PROPERTY_KEY_CHANNEL_ID) != null
                ? Integer.parseInt(System.getenv(PROPERTY_KEY_CHANNEL_ID))
                : (jsonElement != null && jsonElement instanceof JsonObject && ((JsonObject) jsonElement).get(PROPERTY_KEY_CHANNEL_ID) != null
                ? ((JsonObject) jsonElement).get(PROPERTY_KEY_CHANNEL_ID).getAsInt()
                : 0);

        INTERACTIVE_PARTICIPANT_URI = URI.create(String.format("%s://%s/participant?channel=%s", INTERACTIVE_SERVICE_URI.getScheme(), INTERACTIVE_SERVICE_URI.getAuthority(), CHANNEL_ID));
    }

    /**
     * Waits a period of time to give the <code>InteractiveWebSocketClient</code> time to receive input
     * from the Interactive service prior to executing any assertions.
     *
     * @since 1.0.0
     */
    public static void waitForWebSocket() {
        try {
            Thread.sleep(250);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
