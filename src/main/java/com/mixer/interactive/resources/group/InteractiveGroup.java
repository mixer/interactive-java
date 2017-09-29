package com.mixer.interactive.resources.group;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.mixer.interactive.GameClient;
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
import java.util.concurrent.CompletableFuture;

import static com.mixer.interactive.GameClient.GROUP_SERVICE_PROVIDER;

/**
 * A <code>InteractiveGroup</code> represents a group on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveGroup
        extends InteractiveResource<InteractiveGroup>
        implements IInteractiveCreatable, IInteractiveDeletable, Comparable<InteractiveGroup> {

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

        if (groupID != null && !groupID.isEmpty()) {
            this.groupID = groupID;
        }
        else {
            LOG.fatal("GroupID must be non-null and non-empty");
            throw new IllegalArgumentException("GroupID must be non-null and non-empty");
        }

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

        return this;
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
     * Iterates through a <code>Collection</code> of Objects. If <code>this</code> is found to be in the
     * <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of Objects
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          provided <code>Collection</code> contains <code>this</code>
     *
     * @see     InteractiveResource#syncIfEqual(Collection)
     *
     * @since   2.0.0
     */
    @Override
    public boolean syncIfEqual(Collection<?> objects) {
        if (objects != null) {
            for (Object o : objects) {
                if (this.equals(o)) {
                    this.meta = ((InteractiveGroup) o).meta;
                    this.sceneID = ((InteractiveGroup) o).sceneID;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the create operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_GROUPS create} method call completes with no errors
     *
     * @see     IInteractiveCreatable#create(GameClient)
     *
     * @since   2.0.0
     */
    @Override
    public CompletableFuture<Boolean> create(GameClient gameClient) {
        if (gameClient == null) {
            return CompletableFuture.completedFuture(false);
        }

        return gameClient.using(GROUP_SERVICE_PROVIDER).create(this);
    }

    /**
     * Updates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the update operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#UPDATE_GROUPS update} method call completes with no errors
     *
     * @see     IInteractiveUpdatable#update(GameClient)
     *
     * @since   2.0.0
     */
    @Override
    public CompletableFuture<Boolean> update(GameClient gameClient) {
        if (gameClient == null) {
            return CompletableFuture.completedFuture(false);
        }

        return gameClient.using(GROUP_SERVICE_PROVIDER).update(this)
                .thenCompose(groups -> CompletableFuture.supplyAsync(() -> syncIfEqual(groups)));
    }

    /**
     * Deletes <code>this</code> from the Interactive service, reassigning participants of this group to the default group.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP delete} method call completes with no errors
     *
     * @see     IInteractiveDeletable#delete(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public CompletableFuture<Boolean> delete(GameClient gameClient) {
        return delete(gameClient, DEFAULT_GROUP);
    }

    /**
     * Deletes <code>this</code> from the Interactive service, reassigning participants of this group to the specified group.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     * @param   reassignGroupID
     *          The identifier for the <code>InteractiveGroup</code> that <code>InteractiveParticipants</code> will be
     *          reassigned to
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_GROUP delete} method call completes with no errors
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> delete(GameClient gameClient, String reassignGroupID) {
        return (gameClient != null)
                ? gameClient.using(GROUP_SERVICE_PROVIDER).delete(groupID, reassignGroupID)
                : CompletableFuture.completedFuture(false);
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
                .putString(sceneID, StandardCharsets.UTF_8)
                .putObject(meta, (Funnel<JsonElement>) (from, into) -> {
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
}
