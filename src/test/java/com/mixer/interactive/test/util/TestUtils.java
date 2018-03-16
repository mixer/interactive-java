package com.mixer.interactive.test.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
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
import java.util.ArrayList;
import java.util.List;

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
     * Property key for retrieving the <code>API_BASE_URL</code> property
     */
    private static final String PROPERTY_KEY_API_BASE_URL = "API_BASE_URL";

    /**
     * Property key for retrieving the <code>PARTICIPANT_TOKENS</code> property
     */
    private static final String PROPERTY_PARTICIPANT_TOKENS = "PARTICIPANT_TOKENS";

    /**
     * URI for localhost testing
     */
    private static final URI INTERACTIVE_LOCALHOST = URI.create("ws://localhost:3000/gameClient");

    /**
     * Test client id
     */
    public static final String CLIENT_ID = "clientId";

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
     * Channel Id that test participants will connect to
     */
    public static final int CHANNEL_ID;

    /**
     * Which base url to use for API calls
     */
    public static final String API_BASE_URL;

    /**
     * <code>List</code> of test participants
     */
    public static final List<InteractiveTestParticipant> TEST_PARTICIPANTS = new ArrayList<>();

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
                    serviceHost = EndpointUtil.getInteractiveHost(CLIENT_ID).getAddress();
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

        API_BASE_URL = System.getenv(PROPERTY_KEY_API_BASE_URL) != null
                ? System.getenv(PROPERTY_KEY_API_BASE_URL)
                : (jsonElement != null && jsonElement instanceof JsonObject && ((JsonObject) jsonElement).get(PROPERTY_KEY_API_BASE_URL) != null
                ? ((JsonObject) jsonElement).get(PROPERTY_KEY_API_BASE_URL).getAsString()
                : "https://localhost/api/v1/");

        INTERACTIVE_PARTICIPANT_URI = URI.create(String.format("%s://%s/participant?channel=%s", INTERACTIVE_SERVICE_URI.getScheme(), INTERACTIVE_SERVICE_URI.getAuthority(), CHANNEL_ID));

        String encodedTokens     = System.getenv(PROPERTY_PARTICIPANT_TOKENS) != null
                ? System.getenv(PROPERTY_PARTICIPANT_TOKENS)
                : (jsonElement != null && jsonElement instanceof JsonObject && ((JsonObject) jsonElement).get(PROPERTY_PARTICIPANT_TOKENS) != null
                ? ((JsonObject) jsonElement).get(PROPERTY_PARTICIPANT_TOKENS).getAsString()
                : "[\"foo\", \"foo\", \"foo\"]");
        List<String> tokens = new Gson().fromJson(encodedTokens, new TypeToken<List<String>>(){}.getType());
        tokens.forEach(token -> TEST_PARTICIPANTS.add(new InteractiveTestParticipant(token)));
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
