package com.mixer.interactive.services;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveException;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.protocol.MethodPacket;
import com.mixer.interactive.protocol.ReplyPacket;
import com.mixer.interactive.resources.participant.InteractiveParticipant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Provides all functionality relating to making requests and interpreting replies from the Interactive service
 * regarding participants.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveParticipant
 *
 * @since       1.0.0
 */
public class ParticipantServiceProvider extends AbstractServiceProvider {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveParticipant</code>.
     */
    private static final Type PARTICIPANT_SET_TYPE = new TypeToken<Set<InteractiveParticipant>>(){}.getType();

    /**
     * Collection of parameter key names for various participant method calls and events
     */
    private static final String PARAM_UPDATE_PRIORITY = "priority";
    private static final String PARAM_KEY_PARTICIPANTS = "participants";
    private static final String PARAM_KEY_FROM = "from";
    private static final String PARAM_KEY_HAS_MORE = "hasMore";
    private static final String PARAM_KEY_THRESHOLD = "threshold";

    /**
     * Initializes a new <code>ParticipantServiceProvider</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this service provider
     *
     * @since   1.0.0
     */
    public ParticipantServiceProvider(GameClient gameClient) {
        super(gameClient);
    }

    /**
     * <p>Retrieves all of the participants that are currently connected to the Interactive integration that this client
     * is connected to, in ascending order by the time they connected.</p>
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
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of all
     *          <code>InteractiveParticipants</code> connected to the Interactive integration
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveParticipant>> getAllParticipants() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getParticipants(InteractiveMethod.GET_ALL_PARTICIPANTS, 0, Comparator.comparingLong(InteractiveParticipant::getConnectedAt));
            }
            catch (InteractiveException ex) {
                return Collections.emptySet();
            }
        });
    }

    /**
     * <p>Retrieves all of the currently connected participants who have given input after the specified threshold time,
     * where the threshold is given as a UTC unix timestamp (in milliseconds), in ascending order by the time they last
     * gave input.</p>
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
     * @param   thresholdTimestamp
     *          A UTC unix timestamp (in milliseconds)
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of
     *          <code>InteractiveParticipants</code> connected to the Interactive integration
     *          that have given input since the provided threshold
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveParticipant>> getActiveParticipants(long thresholdTimestamp) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getParticipants(InteractiveMethod.GET_ACTIVE_PARTICIPANTS, thresholdTimestamp, Comparator.comparingLong(InteractiveParticipant::getLastInputAt));
            }
            catch (InteractiveException ex) {
                return Collections.emptySet();
            }
        });
    }

    /**
     * <p>Bulk-updates a collection of participants. The Interactive service will reply with a set of updated
     * participants.</p>
     *
     * <p>The Interactive service will either update all the participants provided, or fail in which case NONE of the
     * participants provided will be updated. In no case will the Interactive service apply updates to a subset of
     * participants. If a provided participant is not connected to the integration, the update to that participant will
     * be ignored.</p>
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
     * @param   participants
     *          An array of <code>InteractiveParticipants</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveParticipants</code>
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveParticipant>> update(InteractiveParticipant ... participants) {
        return update(0, participants);
    }

    /**
     * <p>Bulk-updates a collection of participants. The Interactive service will reply with a set of updated
     * participants.</p>
     *
     * <p>The Interactive service will either update all the participants provided, or fail in which case NONE of the
     * participants provided will be updated. In no case will the Interactive service apply updates to a subset of
     * participants. If a provided participant is not connected to the integration, the update to that participant will
     * be ignored.</p>
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
     * @param   priority
     *          The priority value for the update
     * @param   participants
     *          An array of <code>InteractiveParticipants</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveParticipants</code>
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveParticipant>> update(int priority, InteractiveParticipant ... participants) {
        return participants != null
                ? update(priority, Arrays.asList(participants))
                : CompletableFuture.completedFuture(Collections.emptySet());
    }

    /**
     * <p>Bulk-updates a collection of participants. The Interactive service will reply with a set of updated
     * participants.</p>
     *
     * <p>The Interactive service will either update all the participants provided, or fail in which case NONE of the
     * participants provided will be updated. In no case will the Interactive service apply updates to a subset of
     * participants. If a provided participant is not connected to the integration, the update to that participant will
     * be ignored.</p>
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
     * @param   participants
     *          A <code>Collection</code> of <code>InteractiveParticipants</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveParticipants</code>
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveParticipant>> update(Collection<InteractiveParticipant> participants) {
        return update(0, participants);
    }

    /**
     * <p>Bulk-updates a collection of participants. The Interactive service will reply with a set of updated
     * participants.</p>
     *
     * <p>The Interactive service will either update all the participants provided, or fail in which case NONE of the
     * participants provided will be updated. In no case will the Interactive service apply updates to a subset of
     * participants. If a provided participant is not connected to the integration, the update to that participant will
     * be ignored.</p>
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
     * @param   priority
     *          The priority value for the update
     * @param   participants
     *          A <code>Collection</code> of <code>InteractiveParticipants</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveParticipants</code>
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveParticipant>> update(int priority, Collection<InteractiveParticipant> participants) {
        if (participants == null) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_PARTICIPANTS, GameClient.GSON.toJsonTree(participants));
        jsonParams.addProperty(PARAM_UPDATE_PRIORITY, priority);
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.UPDATE_PARTICIPANTS, jsonParams, PARAM_KEY_PARTICIPANTS, PARTICIPANT_SET_TYPE);
    }

    /**
     * Retrieves all of the currently connected participants who meet the conditions of the specified method call and
     * the initial marker, sorted in the specified ordering. Only <code>getAllParticipants</code> and
     * <code>getActiveParticipants</code> calls are supported; all other method calls will throw an
     * <code>IllegalArgumentException.</code>
     *
     * @param   method
     *          The method request to send to the Interactive service
     * @param   initialMarker
     *          Initial marker to base the first request to the Interactive service off of
     * @param   comparator
     *          Comparator indicating the sort preference for the returned participants
     *
     * @return  A <code>Set</code> of <code>InteractiveParticipants</code> connected to the Interactive integration that
     *          meet the conditions of the specified method call and the initial marker, sorted in the specified
     *          ordering
     *
     * @throws InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveParticipant
     *
     * @since   1.0.0
     */
    private Set<InteractiveParticipant> getParticipants(InteractiveMethod method, long initialMarker, Comparator<InteractiveParticipant> comparator) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {

        if (method != InteractiveMethod.GET_ALL_PARTICIPANTS && method != InteractiveMethod.GET_ACTIVE_PARTICIPANTS) {
            LOG.fatal("Illegal method specified (may only be one of 'getAllParticipants' or 'getActiveParticipants')");
            throw new IllegalArgumentException("Illegal method specified (may only be one of 'getAllParticipants' or 'getActiveParticipants')");
        }

        Set<InteractiveParticipant> participants = new TreeSet<>(comparator);
        boolean hasMore = true;
        long marker = initialMarker;
        String property = (method == InteractiveMethod.GET_ALL_PARTICIPANTS ? PARAM_KEY_FROM : PARAM_KEY_THRESHOLD);

        while (hasMore) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(property, marker);
            int nextPacketId = gameClient.using(GameClient.RPC_SERVICE_PROVIDER).claimNextPacketId();
            MethodPacket requestPacket = new MethodPacket(nextPacketId, method, jsonObject);
            ReplyPacket replyPacket = gameClient.using(GameClient.RPC_SERVICE_PROVIDER).send(requestPacket).join();
            if (replyPacket.hasError()) {
                throw new InteractiveReplyWithErrorException(requestPacket, replyPacket.getError());
            }

            if (replyPacket.getResult().isJsonObject()) {
                JsonObject jsonResultObject = (JsonObject) replyPacket.getResult();
                InteractiveParticipant[] partialParticipants = GameClient.GSON.fromJson(jsonResultObject.get(PARAM_KEY_PARTICIPANTS), InteractiveParticipant[].class);
                if (partialParticipants != null && partialParticipants.length > 0) {
                    Collections.addAll(participants, partialParticipants);
                    if (method == InteractiveMethod.GET_ALL_PARTICIPANTS) {
                        marker = partialParticipants[partialParticipants.length - 1].getConnectedAt();
                    }
                    else {
                        marker = partialParticipants[partialParticipants.length - 1].getLastInputAt();
                    }
                }
                hasMore = jsonResultObject.get(PARAM_KEY_HAS_MORE).getAsBoolean();
            }
        }

        return participants;
    }
}
