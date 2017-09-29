package com.mixer.interactive.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.protocol.ReplyPacket;
import com.mixer.interactive.ws.InteractiveWebSocketClient;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Provides all functionality relating to making requests and interpreting replies from the Interactive service. In the
 * majority of cases a developer would use <code>makeRequest</code> methods to send method requests to the Interactive
 * service and interpret the reply. However in the event a developer would like to craft their own
 * <code>MethodPacket</code> (e.g., to call a method that does not yet exist in the
 * <code>InteractiveMethod</code> enum), <code>send</code> methods are available to accomplish this goal.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveMethod
 * @see         MethodPacket
 *
 * @since       1.0.0
 */
public class RemoteProcedureCallServiceProvider extends AbstractServiceProvider {

    /**
     * The default time unit for timing out unfulfilled method requests.
     */
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * The default duration for timing out unfulfilled method requests.
     */
    private static final long DEFAULT_DURATION = 15;

    /**
     * Initializes a new <code>RemoteProcedureCallServiceProvider</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this service provider
     *
     * @since   1.0.0
     */
    public RemoteProcedureCallServiceProvider(GameClient gameClient) {
        super(gameClient);
    }

    /**
     * Prepares and sends a request to the Interactive service, expecting no reply.
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public void makeRequestNoReply(InteractiveMethod method, JsonElement params) {
        send(new MethodPacket(claimNextPacketId(), method, params, true));
    }

    /**
     * <p>Prepares and sends a request to the Interactive service.</p>
     *
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>CompletableFuture</code>.</p>
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} for this request
     *          if it completes with no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> makeRequest(InteractiveMethod method, JsonElement params) {
        MethodPacket requestPacket = new MethodPacket(claimNextPacketId(), method, params);

        return send(requestPacket).thenCompose(replyPacket -> {
            CompletableFuture<Boolean> composedFuture = new CompletableFuture<>();
            if (replyPacket == null) {
                composedFuture.completeExceptionally(new InteractiveRequestNoReplyException(requestPacket));
            }
            else if (replyPacket.hasError()) {
                composedFuture.completeExceptionally(new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError()));
            }
            else {
                composedFuture.complete(true);
            }
            return composedFuture;
        });
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>CompletableFuture</code>.</p>
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     * @param   type
     *          Type of object to be parsed from reply
     * @param   <T>
     *          Class of object to be parsed from the reply
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>T</code> object parsed from the
     *          <code>ReplyPacket</code> sent back from the Interactive service for this request if it completes with
     *          no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public <T> CompletableFuture<T> makeRequest(InteractiveMethod method, JsonElement params, Type type) {
        return makeRequest(method, params, null, type);
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>CompletableFuture</code>.</p>
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     * @param   memberName
     *          Member name of parameter to be parsed from reply
     * @param   type
     *          Type of object to be parsed from reply
     * @param   <T>
     *          Class of object to be parsed from the reply
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>T</code> object parsed from the
     *          <code>ReplyPacket</code> sent back from the Interactive service for this request if it completes with
     *          no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public <T> CompletableFuture<T> makeRequest(InteractiveMethod method, JsonElement params, String memberName, Type type) {
        MethodPacket requestPacket = new MethodPacket(claimNextPacketId(), method, params);

        return send(requestPacket).thenCompose(replyPacket -> {
            CompletableFuture<T> composedFuture = new CompletableFuture<>();
            if (replyPacket == null) {
                composedFuture.completeExceptionally(new InteractiveRequestNoReplyException(requestPacket));
            }
            else if (replyPacket.hasError()) {
                composedFuture.completeExceptionally(new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError()));
            }
            else if (replyPacket.getResult().isJsonObject()) {
                if (memberName == null) {
                    composedFuture.complete(replyPacket.getResultAs(type));
                }
                else {
                    composedFuture.complete(GameClient.GSON.fromJson(((JsonObject) replyPacket.getResult()).get(memberName), type));
                }
            }
            else {
                composedFuture.complete(null);
            }
            return composedFuture;
        });
    }

    /**
     * Sends a request to the Interactive service, returning the reply. If a reply is not received within the specified
     * timeout duration, throws an <code>InteractiveRequestNoReplyException</code> exception.
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     *
     * @return  A <code>CompletableFuture</code> that when complete returns the <code>ReplyPacket</code> sent back from
     *          the Interactive service for the provided method request
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public CompletableFuture<ReplyPacket> send(MethodPacket requestPacket) {
        return send(requestPacket, DEFAULT_DURATION, DEFAULT_TIME_UNIT);
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>CompletableFuture</code>.</p>
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     * @param   duration
     *          Duration before request is considered timed out (no reply)
     * @param   timeUnit
     *          A <code>TimeUnit</code> indicating the units to be used in the timeout
     *
     * @return  A <code>CompletableFuture</code> that when complete returns the <code>ReplyPacket</code> sent back from
     *          the Interactive service for the provided method request
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public CompletableFuture<ReplyPacket> send(MethodPacket requestPacket, long duration, TimeUnit timeUnit) {

        Map<MethodPacket, CompletableFuture<ReplyPacket>> requestPromiseMap = send(Collections.singletonList(requestPacket), duration, timeUnit);
        if (requestPromiseMap.containsKey(requestPacket)) {
            return requestPromiseMap.get(requestPacket);
        }

        CompletableFuture<ReplyPacket> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new InteractiveRequestNoReplyException(requestPacket));
        return failedFuture;
    }

    /**
     * <p>Prepares and sends one or many requests to the Interactive service, returning a list of replies that are in
     * the same order as the input collection.</p>
     *
     * <p>The result of each reply may include checked exceptions that were thrown in the event that there was a problem
     * with the request to the Interactive service. Specifically, two types of checked exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned list.</p>
     *
     * @param   requestPackets
     *          A <code>Collection</code> of <code>MethodPacket</code> representing the requests being sent
     *
     * @return  A <code>List</code> of <code>CompletableFutures</code> that when complete return the
     *          <code>ReplyPacket</code> for the corresponding input <code>MethodPacket</code>. This list is in the same
     *          order as the input collection.
     *
     * @since   1.0.0
     */
    public Map<MethodPacket, CompletableFuture<ReplyPacket>> send(Collection<MethodPacket> requestPackets) {
        return send(requestPackets, DEFAULT_DURATION, DEFAULT_TIME_UNIT);
    }

    /**
     * <p>Prepares and sends one or many requests to the Interactive service, returning a list of replies that are in
     * the same order as the input collection.</p>
     *
     * <p>The result of each reply may include checked exceptions that were thrown in the event that there was a problem
     * with the request to the Interactive service. Specifically, two types of checked exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned list.</p>
     *
     * @param   requestPackets
     *          A <code>Collection</code> of <code>MethodPacket</code> representing the requests being sent
     * @param   duration
     *          Duration before request is considered timed out (no reply)
     * @param   timeUnit
     *          A <code>TimeUnit</code> indicating the units to be used in the timeout
     *
     * @return  A <code>List</code> of <code>CompletableFutures</code> that when complete return the
     *          <code>ReplyPacket</code> for the corresponding input <code>MethodPacket</code>. This list is in the same
     *          order as the input collection.
     *
     * @since   1.0.0
     */
    public Map<MethodPacket, CompletableFuture<ReplyPacket>> send(Collection<MethodPacket> requestPackets, long duration, TimeUnit timeUnit) {
        if (requestPackets == null || requestPackets.isEmpty() || timeUnit == null || duration < 0) {
            return Collections.emptyMap();
        }

        InteractiveWebSocketClient webSocketClient = gameClient.getWebSocketClient();
        Map<MethodPacket, CompletableFuture<ReplyPacket>> requestPromiseMap = new HashMap<>();
        JsonArray requestArray = new JsonArray();

        for (MethodPacket requestPacket : requestPackets) {
            requestPacket.setSequenceNumber(getSequenceNumber());
            CompletableFuture<ReplyPacket> replyPromise = new CompletableFuture<>();
            if (webSocketClient == null) {
                replyPromise.completeExceptionally(new InteractiveRequestNoReplyException(requestPacket));
                requestPromiseMap.put(requestPacket, replyPromise);
                continue;
            }

            // Queue up the request
            requestArray.add(GameClient.GSON.toJsonTree(requestPacket));

            // If the request is to be discarded, do not track it. Otherwise, track it and add a listener to time it out
            // in the event a reply is not received within the specified time frame.
            if (requestPacket.getDiscard()) {
                requestPromiseMap.put(requestPacket, CompletableFuture.completedFuture(null));
            }
            else {
                webSocketClient.getWaitingFuturesMap().put(requestPacket.getPacketID(), replyPromise);
                gameClient.getExecutorService().schedule(() ->
                    replyPromise.completeExceptionally(new InteractiveRequestNoReplyException(requestPacket))
                , duration, timeUnit);
                requestPromiseMap.put(requestPacket, replyPromise);
            }
        }

        // If multiple requests are to be sent, send them as an array. Otherwise send the request as an object.
        if (webSocketClient != null) {
            if (requestArray.size() > 1) {
                webSocketClient.send(GameClient.GSON.toJson(requestArray));
            }
            else if (requestArray.size() == 1) {
                webSocketClient.send(GameClient.GSON.toJson(requestArray.get(0)));
            }
        }

        return requestPromiseMap;
    }

    /**
     * Claims and returns the next available packet id.
     *
     * @return  The next available packet id
     *
     * @see     InteractiveWebSocketClient#claimNextPacketId()
     *
     * @since   1.0.0
     */
    public int claimNextPacketId() {
        return (gameClient.getWebSocketClient() != null) ? gameClient.getWebSocketClient().claimNextPacketId() : 0;
    }

    /**
     * Returns the last seen sequence number.
     *
     * @return  The last seen sequence number
     *
     * @see     InteractiveWebSocketClient#getLastSequenceNumber()
     *
     * @since   2.0.0
     */
    public int getSequenceNumber() {
        return gameClient.getWebSocketClient().getLastSequenceNumber();
    }
}
