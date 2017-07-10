package com.mixer.interactive.services;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Provides all functionality relating to making requests and interpreting replies from the Interactive service. In the
 * majority of cases a developer would use <code>makeRequest</code> methods to send method requests to the Interactive
 * service and interpret the reply. However in the event a developer would like to craft their own
 * <code>MethodPacket</code> (e.g., to call a method that does not yet exist in the
 * <code>InteractiveMethod</code> enum), <code>send</code> methods are available to accomplish this goal.
 *
 * @author      Microsoft Corporation
 * @author      pahimar
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
    private static final TimeUnit DEFAULT_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;

    /**
     * The default duration for timing out unfulfilled method requests.
     */
    private static final long DEFAULT_TIMEOUT_DURATION = 15;

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
     * Prepares and sends a request to the Interactive service.
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     *
     * @return <code>true</code> if the request was successful (no errors were returned), <code>false</code> otherwise
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public boolean makeRequest(InteractiveMethod method, JsonElement params) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return !(send(new MethodPacket(claimNextPacketId(), method, params))).hasError();
    }

    /**
     * Prepares and sends a request to the Interactive service, returning the parsed reply.
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
     * @return  A <code>T</code> object parsed from the <code>ReplyPacket</code> sent back from the Interactive service for
     *          this request
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public <T> T makeRequest(InteractiveMethod method, JsonElement params, Type type) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return send(new MethodPacket(claimNextPacketId(), method, params)).getResultAs(type);
    }

    /**
     * Prepares and sends a request to the Interactive service, returning the parsed reply.
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
     * @return  A <code>T</code> object parsed from the <code>ReplyPacket</code> sent back from the Interactive service for this
     *          request if it completes with no errors
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public <T> T makeRequest(InteractiveMethod method, JsonElement params, String memberName, Type type) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        ReplyPacket replyPacket = send(new MethodPacket(claimNextPacketId(), method, params));
        return replyPacket.getResult().isJsonObject()
                ? GameClient.GSON.fromJson(((JsonObject) replyPacket.getResult()).get(memberName),type)
                : null;
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
        sendAsync(new MethodPacket(claimNextPacketId(), method, params, true));
    }

    /**
     * <p>Prepares and sends a request to the Interactive service.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>ListenableFuture</code>.</p>
     *
     * @param   method
     *          An <code>InteractiveMethod</code> for the request
     * @param   params
     *          Json encoded map of parameters for the request
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} for this request
     *          if it completes with no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> makeRequestAsync(InteractiveMethod method, JsonElement params) {
        MethodPacket requestPacket = new MethodPacket(claimNextPacketId(), method, params);

        return Futures.transform(sendAsync(requestPacket), (AsyncFunction<ReplyPacket, Boolean>) replyPacket -> {
            if (replyPacket == null) {
                throw new InteractiveRequestNoReplyException(requestPacket);
            }
            if (replyPacket.hasError()) {
                throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
            }

            return Futures.immediateFuture(true);
        });
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>ListenableFuture</code>.</p>
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
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>T</code> object parsed from the
     *          <code>ReplyPacket</code> sent back from the Interactive service for this request if it completes with
     *          no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public <T> ListenableFuture<T> makeRequestAsync(InteractiveMethod method, JsonElement params, Type type) {
        MethodPacket requestPacket = new MethodPacket(claimNextPacketId(), method, params);
        return Futures.transform(sendAsync(requestPacket), (AsyncFunction<ReplyPacket, T>) replyPacket -> {
            if (replyPacket == null) {
                throw new InteractiveRequestNoReplyException(requestPacket);
            }
            if (replyPacket.hasError()) {
                throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
            }

            return Futures.immediateFuture(replyPacket.getResultAs(type));
        });
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>ListenableFuture</code>.</p>
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
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>T</code> object parsed from the
     *          <code>ReplyPacket</code> sent back from the Interactive service for this request if it completes with
     *          no errors
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public <T> ListenableFuture<T> makeRequestAsync(InteractiveMethod method, JsonElement params, String memberName, Type type) {
        MethodPacket requestPacket = new MethodPacket(claimNextPacketId(), method, params);
        return Futures.transform(sendAsync(requestPacket), (AsyncFunction<ReplyPacket, T>) replyPacket -> {
            if (replyPacket == null) {
                throw new InteractiveRequestNoReplyException(requestPacket);
            }
            if (replyPacket.hasError()) {
                throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
            }

            return replyPacket.getResult().isJsonObject()
                    ? Futures.immediateFuture(GameClient.GSON.fromJson(((JsonObject) replyPacket.getResult()).get(memberName), type))
                    : Futures.immediateFuture(null);
        });
    }

    /**
     * Sends a request to the Interactive service, returning the reply.
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     *
     * @return  The <code>ReplyPacket</code> sent back from the Interactive service for the provided method request
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public ReplyPacket send(MethodPacket requestPacket) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return send(requestPacket, DEFAULT_TIMEOUT_TIME_UNIT, DEFAULT_TIMEOUT_DURATION);
    }

    /**
     * Sends a request to the Interactive service, returning the reply. If a reply is not received within the specified
     * timeout duration, throws an <code>InteractiveRequestNoReplyException</code> exception.
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     * @param   timeoutUnit
     *          A <code>TimeUnit</code> indicating the units to be used in the timeout
     * @param   timeoutDuration
     *          Duration before request is considered timed out (no reply)
     *
     * @return  The <code>ReplyPacket</code> sent back from the Interactive service for the provided method request
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public ReplyPacket send(MethodPacket requestPacket, TimeUnit timeoutUnit, long timeoutDuration) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {

        ReplyPacket replyPacket = null;
        try {
            replyPacket = sendAsync(requestPacket, timeoutUnit, timeoutDuration).get();
        }
        catch (InterruptedException | ExecutionException e) {
            // NO-OP
        }

        if (replyPacket == null) {
            throw new InteractiveRequestNoReplyException(requestPacket);
        }
        if (replyPacket.hasError()) {
            throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
        }

        return replyPacket;
    }

    /**
     * Sends a request to the Interactive service, returning the reply. If a reply is not received within the specified
     * timeout duration, throws an <code>InteractiveRequestNoReplyException</code> exception.
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     *
     * @return  A <code>ListenableFuture</code> that when complete returns the <code>ReplyPacket</code> sent back from
     *          the Interactive service for the provided method request
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public ListenableFuture<ReplyPacket> sendAsync(MethodPacket requestPacket) {
        return sendAsync(requestPacket, DEFAULT_TIMEOUT_TIME_UNIT, DEFAULT_TIMEOUT_DURATION);
    }

    /**
     * <p>Prepares and sends a request to the Interactive service, returning the parsed reply.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>ListenableFuture</code>.</p>
     *
     * @param   requestPacket
     *          A <code>MethodPacket</code> representing the request being sent
     * @param   timeoutUnit
     *          A <code>TimeUnit</code> indicating the units to be used in the timeout
     * @param   timeoutDuration
     *          Duration before request is considered timed out (no reply)
     *
     * @return  A <code>ListenableFuture</code> that when complete returns the <code>ReplyPacket</code> sent back from
     *          the Interactive service for the provided method request
     *
     * @see     MethodPacket
     *
     * @since   1.0.0
     */
    public ListenableFuture<ReplyPacket> sendAsync(MethodPacket requestPacket, TimeUnit timeoutUnit, long timeoutDuration) {

        InteractiveWebSocketClient webSocketClient = gameClient.getWebSocketClient();
        if (webSocketClient == null) {
            return Futures.immediateFailedFuture(new InteractiveRequestNoReplyException(requestPacket));
        }

        String packetString = GameClient.GSON.toJson(requestPacket);
        if (requestPacket.getDiscard()) {
            webSocketClient.send(packetString);
            return Futures.immediateFuture(null);
        }
        else {
            SettableFuture<ReplyPacket> sendRequest = SettableFuture.create();
            webSocketClient.getWaitingPromisesMap().put(requestPacket.getPacketID(), sendRequest);
            gameClient.getExecutorService().schedule((Runnable) () -> sendRequest.setException(new InteractiveRequestNoReplyException(requestPacket)), timeoutDuration, timeoutUnit);
            webSocketClient.send(packetString);
            return sendRequest;
        }
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
}
