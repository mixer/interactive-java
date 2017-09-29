package com.mixer.interactive.services;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.group.InteractiveGroup;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Provides all functionality relating to making requests and interpreting replies from the Interactive service
 * regarding groups.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveGroup
 *
 * @since       1.0.0
 */
public class GroupServiceProvider extends AbstractServiceProvider {

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveGroups</code>.
     */
    private static final Type GROUP_SET_TYPE = new TypeToken<Set<InteractiveGroup>>(){}.getType();

    /**
     * Empty Json object for use in several method calls.
     */
    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    /**
     * Constant representing the default group ID
     */
    private static final String DEFAULT_VALUE = "default";

    /**
     * Collection of parameter key names for various group method calls and events
     */
    private static final String PARAM_UPDATE_PRIORITY = "priority";
    private static final String PARAM_KEY_GROUPS = "groups";
    private static final String PARAM_KEY_GROUP_ID = "groupID";
    private static final String PARAM_KEY_REASSIGN_GROUP_ID = "reassignGroupID";

    /**
     * Initializes a new <code>GroupServiceProvider</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this service provider
     *
     * @since   1.0.0
     */
    public GroupServiceProvider(GameClient gameClient) {
        super(gameClient);
    }

    /**
     * <p>Retrieves all the groups connected to the Interactive integration.</p>
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
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of
     *          <code>InteractiveGroups</code> for the connected Interactive integration
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveGroup>> getGroups() {
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.GET_GROUPS, EMPTY_JSON_OBJECT, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration.</p>
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
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be created
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_GROUPS create} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> create(InteractiveGroup ... groups) {
        return create(groups != null ? Arrays.asList(groups) : Collections.emptyList());
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration.</p>
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
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be created
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_GROUPS create} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> create(Collection<InteractiveGroup> groups) {
        if (groups == null) {
            return CompletableFuture.completedFuture(true);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_GROUPS, GameClient.GSON.toJsonTree(groups));
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.CREATE_GROUPS, jsonParams);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
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
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveGroup>> update(InteractiveGroup ... groups) {
        return update(0, groups);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
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
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveGroup>> update(int priority, InteractiveGroup ... groups) {
        return update(priority, groups != null ? Arrays.asList(groups) : Collections.emptyList());
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
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
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveGroup>> update(Collection<InteractiveGroup> groups) {
        return update(0, groups);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
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
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveGroup>> update(int priority, Collection<InteractiveGroup> groups) {
        if (groups == null) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_GROUPS, GameClient.GSON.toJsonTree(groups));
        jsonParams.addProperty(PARAM_UPDATE_PRIORITY, priority);
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.UPDATE_GROUPS, jsonParams, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to the
     * default group. The server MAY not return an error if the group to remove does not exist.</p>
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
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP delete} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> delete(String groupID) {
        return delete(groupID, DEFAULT_VALUE);
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.</p>
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
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroupID
     *          Identifier for the <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP delete} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> delete(String groupID, String reassignGroupID) {
        if (groupID == null || reassignGroupID == null) {
            return CompletableFuture.completedFuture(false);
        }

        JsonObject jsonGroup = new JsonObject();
        jsonGroup.addProperty(PARAM_KEY_GROUP_ID, groupID);
        jsonGroup.addProperty(PARAM_KEY_REASSIGN_GROUP_ID, reassignGroupID);
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.DELETE_GROUP, jsonGroup);
    }
}
