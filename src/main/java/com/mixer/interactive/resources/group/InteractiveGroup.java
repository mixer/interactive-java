package com.mixer.interactive.resources.group;

import com.google.common.base.Objects;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.IInteractiveCreatable;
import com.mixer.interactive.resources.IInteractiveDeletable;
import com.mixer.interactive.resources.IInteractiveUpdatable;
import com.mixer.interactive.resources.InteractiveResource;
import com.mixer.interactive.resources.scene.InteractiveScene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Set;

/**
 * A <code>InteractiveGroup</code> represents a group on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveGroup
        extends InteractiveResource<InteractiveGroup>
        implements IInteractiveCreatable<InteractiveGroup>, IInteractiveDeletable, Comparable<InteractiveGroup> {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Constant representing the default group
     */
    private static final String DEFAULT_GROUP = "default";

    /**
     * Unique identifier for this group
     */
    private final String groupID;

    /**
     * Identifier for the scene for this group
     */
    private String sceneID;

    /**
     * Initializes a new <code>InteractiveGroup</code>.
     *
     * @param   groupID
     *          Identifier for the <code>InteractiveGroup</code>
     *
     * @since   1.0.0
     */
    public InteractiveGroup(String groupID) {
        this(groupID, DEFAULT_GROUP);
    }

    /**
     * Initializes a new <code>InteractiveGroup</code>.
     *
     * @param   groupID
     *          Identifier for the <code>InteractiveGroup</code>
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code> for the <code>InteractiveGroup</code>
     *
     * @since   1.0.0
     */
    public InteractiveGroup(String groupID, String sceneID) {
        this(groupID, null, sceneID);
    }

    /**
     * Initializes a new <code>InteractiveGroup</code>.
     *
     * @param   groupID
     *          Identifier for the <code>InteractiveGroup</code>
     * @param   etag
     *          The etag for the <code>InteractiveGroup</code>
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code> for the <code>InteractiveGroup</code>
     *
     * @since   1.0.0
     */
    public InteractiveGroup(String groupID, String etag, String sceneID) {

        if (groupID != null && !groupID.isEmpty()) {
            this.groupID = groupID;
        }
        else {
            LOG.fatal("GroupID must be non-null and non-empty");
            throw new IllegalArgumentException("GroupID must be non-null and non-empty");
        }

        this.etag = etag;

        if (sceneID != null && !sceneID.isEmpty()) {
            this.sceneID = sceneID;
        }
        else {
            this.sceneID = DEFAULT_GROUP;
        }
    }

    /**
     * Returns the identifier for the <code>InteractiveGroup</code>.
     *
     * @return  Identifier for the <code>InteractiveGroup</code>
     *
     * @since   1.0.0
     */
    public String getGroupID() {
        return groupID;
    }

    /**
     * Returns <code>true</code> if this is the default group, <code>false</code> otherwise.
     *
     * @return  <code>true</code> if this is the default group, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean isDefault() {
        return DEFAULT_GROUP.equals(groupID);
    }

    /**
     * Returns the identifier for the <code>InteractiveScene</code> for the <code>InteractiveGroup</code>.
     *
     * @return  Identifier for the <code>InteractiveScene</code> for the <code>InteractiveGroup</code>
     *
     * @since   1.0.0
     */
    public String getSceneID() {
        return sceneID;
    }

    /**
     * Sets the <code>InteractiveScene</code> for the <code>InteractiveGroup</code>.
     *
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code> for the <code>InteractiveGroup</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveGroup setScene(String sceneID) {
        if (sceneID != null && !sceneID.isEmpty()) {
            this.sceneID = sceneID;
        }
        else {
            this.sceneID = DEFAULT_GROUP;
        }

        return getThis();
    }

    /**
     * Sets the <code>InteractiveScene</code> for the <code>InteractiveGroup</code>.
     *
     * @param   scene
     *          <code>InteractiveScene</code> for the <code>InteractiveGroup</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveGroup setScene(InteractiveScene scene) {
        return setScene(scene != null ? scene.getSceneID() : null);
    }

    /**
     * {@inheritDoc}
     *
     * @see     InteractiveResource#getThis()
     *
     * @since   1.0.0
     */
    @Override
    protected InteractiveGroup getThis() {
        return this;
    }

    /**
     * Iterates through a <code>Collection</code> of <code>InteractiveGroups</code>. If <code>this</code> is found to be
     * in the <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of <code>InteractiveGroups</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @see     InteractiveResource#syncIfEqual(Collection)
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveGroup syncIfEqual(Collection<? extends InteractiveGroup> objects) {
        if (objects != null) {
            for (InteractiveGroup object : objects) {
                if (this.equals(object)) {
                    this.meta = object.meta;
                    this.etag = object.etag;
                    this.sceneID = object.sceneID;
                }
            }
        }
        return getThis();
    }

    /**
     * Creates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the create operation
     *
     * @return  <code>this</code> for method chaining
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     IInteractiveCreatable#create(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveGroup create(GameClient gameClient) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException {
        if (gameClient != null) {
            gameClient.using(GameClient.GROUP_SERVICE_PROVIDER).createGroups(this);
        }
        return getThis();
    }

    /**
     * Asynchronously creates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the create operation
     *
     * @return  A <code>ListenableFuture</code> that when complete returns <code>this</code> for method chaining
     *
     * @see     IInteractiveCreatable#createAsync(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public ListenableFuture<InteractiveGroup> createAsync(GameClient gameClient) {
        if (gameClient == null) {
            return Futures.immediateFuture(getThis());
        }
        return Futures.transform(gameClient.using(GameClient.GROUP_SERVICE_PROVIDER).createGroupsAsync(this),
                (AsyncFunction<Boolean, InteractiveGroup>) input -> Futures.immediateFuture(getThis()));
    }

    /**
     * Updates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the update operation
     *
     * @return  <code>this</code> for method chaining
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     IInteractiveUpdatable#update(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveGroup update(GameClient gameClient) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException {
        if (gameClient != null) {
            syncIfEqual(gameClient.using(GameClient.GROUP_SERVICE_PROVIDER).updateGroups(this));
        }
        return getThis();
    }

    /**
     * Asynchronously updates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the update operation
     *
     * @return  A <code>ListenableFuture</code> that when complete returns <code>this</code> for method chaining
     *
     * @see     IInteractiveUpdatable#updateAsync(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public ListenableFuture<InteractiveGroup> updateAsync(GameClient gameClient) {
        if (gameClient == null) {
            return Futures.immediateFuture(getThis());
        }
        return  Futures.transform(gameClient.using(GameClient.GROUP_SERVICE_PROVIDER).updateGroupsAsync(this),
                (AsyncFunction<Set<InteractiveGroup>, InteractiveGroup>) updatedGroups ->
                        Futures.immediateFuture(syncIfEqual(updatedGroups)));
    }

    /**
     * Deletes <code>this</code> from the Interactive service, reassigning participants of this group to
     * the default group.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     IInteractiveDeletable#delete(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public void delete(GameClient gameClient) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException {
        delete(gameClient, DEFAULT_GROUP);
    }

    /**
     * Deletes <code>this</code> from the Interactive service, reassigning participants of this group to
     * the specified group.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     * @param   reassignGroupID
     *          The identifier for the <code>InteractiveGroup</code> that <code>InteractiveParticipants</code> will be
     *          reassigned to
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @since   1.0.0
     */
    public void delete(GameClient gameClient, String reassignGroupID) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException {
        if (gameClient != null) {
            gameClient.using(GameClient.GROUP_SERVICE_PROVIDER).deleteGroup(groupID, reassignGroupID);
        }
    }

    /**
     * Asynchronously deletes <code>this</code> from the Interactive service, reassigning participants of this group to
     * the default group.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @see     IInteractiveDeletable#deleteAsync(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public ListenableFuture<Boolean> deleteAsync(GameClient gameClient) {
        return deleteAsync(gameClient, DEFAULT_GROUP);
    }

    /**
     * Asynchronously deletes <code>this</code> from the Interactive service, reassigning participants of this group to
     * the specified group.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     * @param   reassignGroupID
     *          The identifier for the <code>InteractiveGroup</code> that <code>InteractiveParticipants</code> will be
     *          reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP deleteGroup} method call completes with no errors
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteAsync(GameClient gameClient, String reassignGroupID) {
        return (gameClient != null)
                ? gameClient.using(GameClient.GROUP_SERVICE_PROVIDER).deleteGroupAsync(groupID, reassignGroupID)
                : Futures.immediateFuture(false);
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int compareTo(InteractiveGroup o) {
        return (o != null) ? groupID.compareTo(o.groupID) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int hashCode() {
        return Hashing.md5().newHasher()
                .putString(groupID, StandardCharsets.UTF_8)
                .putString(etag != null ? etag : "", StandardCharsets.UTF_8)
                .putString(sceneID, StandardCharsets.UTF_8)
                .putObject(meta, (Funnel<JsonObject>) (from, into) -> {
                    if (from != null && !from.isJsonNull()) {
                        into.putString(from.toString(), StandardCharsets.UTF_8);
                    }
                })
                .hash()
                .asInt();
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof InteractiveGroup && this.compareTo((InteractiveGroup) o) == 0;
    }

    /**
     * Returns a <code>String</code> representation of this <code>InteractiveGroup</code>.
     *
     * @return  <code>String</code> representation of this <code>InteractiveGroup</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("groupID", groupID)
                .add("etag", etag)
                .add("sceneID", sceneID)
                .add("meta", getMeta())
                .toString();
    }
}
