package com.mixer.interactive.util;

import com.google.gson.reflect.TypeToken;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveNoHostsFoundException;
import com.mixer.interactive.resources.core.InteractiveHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Utility class to query for Interactive hosts from the host endpoint.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class EndpointUtil {

    /**
     * API endpoint for discovering <code>InteractiveHosts</code>
     */
    private static final String INTERACTIVE_HOST_DISCOVERY_ENDPOINT = "https://mixer.com/api/v1/interactive/hosts";

    /**
     * Type object used to serialize/de-serialize a <code>List</code> of <code>InteractiveHosts</code>.
     */
    private static final Type INTERACTIVE_HOST_LIST_TYPE = new TypeToken<List<InteractiveHost>>(){}.getType();

    /**
     * Private constructor to prevent instantiation of an utility class.
     *
     * @since   1.0.0
     */
    private EndpointUtil() {
        // NO-OP
    }

    /**
     * Returns the first <code>InteractiveHost</code> returned from the API endpoint.
     *
     * @param   clientId
     *          The OAuth client id for the developer making this call
     *
     * @return  The first <code>InteractiveHost</code> returned from the API endpoint
     *
     * @throws  InteractiveNoHostsFoundException
     *          If no Interactive hosts were returned from the Interactive service
     *
     * @since   1.0.0
     */
    public static InteractiveHost getInteractiveHost(String clientId) throws InteractiveNoHostsFoundException {
        return getInteractiveHosts(clientId).get(0);
    }

    /**
     * Returns a <code>List</code> of <code>InteractiveHosts</code> returned from the API endpoint.
     *
     * @param   clientId
     *          The OAuth client id for the developer making this call
     *
     * @return  A <code>List</code> of <code>InteractiveHosts</code> returned from the API endpoint
     *
     * @throws  InteractiveNoHostsFoundException
     *          If no Interactive hosts were returned from the Interactive service
     *
     * @since   1.0.0
     */
    public static List<InteractiveHost> getInteractiveHosts(String clientId) throws InteractiveNoHostsFoundException {

        List<InteractiveHost> interactiveHosts;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(INTERACTIVE_HOST_DISCOVERY_ENDPOINT);
            httpGet.addHeader("Client-Id", clientId);
            interactiveHosts = httpClient.execute(httpGet, response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    return GameClient.GSON.fromJson(EntityUtils.toString(response.getEntity()), INTERACTIVE_HOST_LIST_TYPE);
                }
                return Collections.emptyList();
            });
        }
        catch (IOException ex) {
            throw new InteractiveNoHostsFoundException();
        }

        if (interactiveHosts == null || interactiveHosts.isEmpty()) {
            throw new InteractiveNoHostsFoundException();
        }

        return interactiveHosts;
    }
}
