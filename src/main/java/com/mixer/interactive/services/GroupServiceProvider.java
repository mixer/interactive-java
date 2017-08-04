package com.mixer.interactive.services;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
     * Retrieves all the groups connected to the Interactive integration.
     *
     * @return  A <code>Set</code> of <code>InteractiveGroups</code> for the connected Interactive integration
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> getGroups() throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.GET_GROUPS, EMPTY_JSON_OBJECT, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * <p>Retrieves all the groups connected to the Interactive integration.</p>
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
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of
     *          <code>InteractiveGroups</code> for the connected Interactive integration
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveGroup>> getGroupsAsync() {
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequestAsync(InteractiveMethod.GET_GROUPS, EMPTY_JSON_OBJECT, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration.</p>
     *
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be created
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void createGroups(InteractiveGroup ... groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (groups != null) {
            createGroups(Arrays.asList(groups));
        }
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration.</p>
     *
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be created
     *
     * @throws InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void createGroups(Collection<InteractiveGroup> groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (groups != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.add(PARAM_KEY_GROUPS, GameClient.GSON.toJsonTree(groups));
            gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.CREATE_GROUPS, jsonParams);
        }
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration.</p>
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
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_GROUPS createGroups} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createGroupsAsync(InteractiveGroup ... groups) {
        return createGroupsAsync(groups != null ? Arrays.asList(groups) : Collections.emptyList());
    }

    /**
     * <p>Creates one or more new groups. Each group may have an initial scene set, however if one is not set the
     * Interactive service will assign the group to the default scene. Group IDs MUST be unique and not already exist in
     * the Interactive integration.</p>
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
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_GROUPS createGroups} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createGroupsAsync(Collection<InteractiveGroup> groups) {
        if (groups == null) {
            return Futures.immediateFuture(true);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_GROUPS, GameClient.GSON.toJsonTree(groups));
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequestAsync(InteractiveMethod.CREATE_GROUPS, jsonParams);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
     *
     * @param   groups
     *          An array of <code>InteractiveGroup InteractiveGroups</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveGroups</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> updateGroups(InteractiveGroup ... groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return updateGroups(0, groups);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
     *
     * @param   priority
     *          The priority value for the update
     * @param   groups
     *          An array of <code>InteractiveGroup InteractiveGroups</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveGroups</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> updateGroups(int priority, InteractiveGroup ... groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return updateGroups(priority, groups != null ? Arrays.asList(groups) : Collections.emptySet());
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
     *
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveGroups</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> updateGroups(Collection<InteractiveGroup> groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return updateGroups(0, groups);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
     *
     * @param   priority
     *          The priority value for the update
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveGroups</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> updateGroups(int priority, Collection<InteractiveGroup> groups) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (groups == null) {
            return Collections.emptySet();
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_GROUPS, GameClient.GSON.toJsonTree(groups));
        jsonParams.addProperty(PARAM_UPDATE_PRIORITY, priority);
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.UPDATE_GROUPS, jsonParams, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
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
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveGroup>> updateGroupsAsync(InteractiveGroup ... groups) {
        return updateGroupsAsync(0, groups);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
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
     * @param   priority
     *          The priority value for the update
     * @param   groups
     *          An array of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveGroup>> updateGroupsAsync(int priority, InteractiveGroup ... groups) {
        return updateGroupsAsync(priority, groups != null ? Arrays.asList(groups) : Collections.emptyList());
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
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
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveGroup>> updateGroupsAsync(Collection<InteractiveGroup> groups) {
        return updateGroupsAsync(0, groups);
    }

    /**
     * <p>Bulk-updates groups that already exist. The Interactive service will reply with a set of updated groups.</p>
     *
     * <p>The Interactive service will either update all the groups provided, or fail in which case NONE of the
     * groups provided will be updated. In no case will the Interactive service apply updates to a subset of
     * groups.</p>
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
     * @param   priority
     *          The priority value for the update
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveGroups</code>
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveGroup>> updateGroupsAsync(int priority, Collection<InteractiveGroup> groups) {
        if (groups == null) {
            return Futures.immediateFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_GROUPS, GameClient.GSON.toJsonTree(groups));
        jsonParams.addProperty(PARAM_UPDATE_PRIORITY, priority);
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequestAsync(InteractiveMethod.UPDATE_GROUPS, jsonParams, PARAM_KEY_GROUPS, GROUP_SET_TYPE);
    }

    /**
     * Removes a group from the Interactive integration, reassigning any participants who were in that group to the
     * default group. The server MAY not return an error if the group to remove does not exist.
     *
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void deleteGroup(String groupID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        deleteGroup(groupID, DEFAULT_VALUE);
    }

    /**
     * Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.
     *
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroupID
     *          Identifier for the <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public void deleteGroup(String groupID, String reassignGroupID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (groupID != null && reassignGroupID != null) {
            JsonObject jsonGroup = new JsonObject();
            jsonGroup.addProperty(PARAM_KEY_GROUP_ID, groupID);
            jsonGroup.addProperty(PARAM_KEY_REASSIGN_GROUP_ID, reassignGroupID);
            gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.DELETE_GROUP, jsonGroup);
        }
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to the
     * default group. The server MAY not return an error if the group to remove does not exist.</p>
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
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteGroupAsync(String groupID) {
        return deleteGroupAsync(groupID, DEFAULT_VALUE);
    }

    /**
     * <p>Removes a group from the Interactive integration, reassigning any participants who were in that group to a
     * different one. The server MAY not return an error if the group to remove does not exist.</p>
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
     * @param   groupID
     *          Identifier for an <code>InteractiveGroup</code> to be deleted
     * @param   reassignGroupID
     *          Identifier for the <code>InteractiveGroup</code> participants will be reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @see     InteractiveGroup
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteGroupAsync(String groupID, String reassignGroupID) {
        if (groupID == null || reassignGroupID == null) {
            return Futures.immediateFuture(false);
        }

        JsonObject jsonGroup = new JsonObject();
        jsonGroup.addProperty(PARAM_KEY_GROUP_ID, groupID);
        jsonGroup.addProperty(PARAM_KEY_REASSIGN_GROUP_ID, reassignGroupID);
        return gameClient.using(GameClient.RPC_SERVICE_PROVIDER).makeRequestAsync(InteractiveMethod.DELETE_GROUP, jsonGroup);
    }
}
