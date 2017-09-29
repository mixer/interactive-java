package com.mixer.interactive.resources.scene;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.IInteractiveCreatable;
import com.mixer.interactive.resources.IInteractiveDeletable;
import com.mixer.interactive.resources.IInteractiveUpdatable;
import com.mixer.interactive.resources.InteractiveResource;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.group.InteractiveGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.mixer.interactive.GameClient.SCENE_SERVICE_PROVIDER;

/**
 * A <code>InteractiveScene</code> represents a scene resource on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveScene
        extends InteractiveResource<InteractiveScene>
        implements IInteractiveCreatable, IInteractiveDeletable, Comparable<InteractiveScene> {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Constant representing the default scene
     */
    private static final String DEFAULT_SCENE = "default";

    /**
     * Unique identifier for this scene
     */
    private final String sceneID;

    /**
     * <code>Set</code> of <code>InteractiveGroup</code> for this scene
     */
    private final Set<InteractiveGroup> groups = new HashSet<>();

    /**
     * <code>Set</code> of <code>InteractiveControls</code> for this scene
     */
    private final Set<InteractiveControl> controls = new HashSet<>();

    /**
     * Initializes a new <code>InteractiveScene</code>.
     *
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code>
     *
     * @since   1.0.0
     */
    public InteractiveScene(String sceneID) {
        this(sceneID, null, null);
    }

    /**
     * Initializes a new <code>InteractiveScene</code>.
     *
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code>
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> that are currently on this
     *          <code>InteractiveScene</code>
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> that this <code>InteractiveScene</code>
     *          will contain
     *
     * @since 1.0.0
     */
    public InteractiveScene(String sceneID, Collection<InteractiveGroup> groups, Collection<InteractiveControl> controls) {

        if (sceneID != null && !sceneID.isEmpty()) {
            this.sceneID = sceneID;
        }
        else {
            LOG.fatal("SceneID must be non-null and non-empty");
            throw new IllegalArgumentException("SceneID must be non-null and non-empty");
        }

        if (groups != null) {
            this.groups.addAll(groups);
        }

        if (controls != null) {
            this.controls.addAll(controls);
        }
    }

    /**
     * Returns the unique identifier for this scene.
     *
     * @return  Unique identifier for this scene
     *
     * @since   1.0.0
     */
    public String getSceneID() {
        return sceneID;
    }

    /**
     * Returns <code>true</code> if this is the default scene, <code>false</code> otherwise.
     *
     * @return  <code>true</code> if this is the default scene, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean isDefault() {
        return DEFAULT_SCENE.equals(sceneID);
    }

    /**
     * Returns the <code>InteractiveGroup</code> matching by <code>groupID</code> if this scene contains it,
     * <code>null</code> otherwise.
     *
     * @param   groupID
     *          Unique identifier for a <code>InteractiveGroup</code>
     *
     * @return  The <code>InteractiveGroup</code> matching by <code>groupID</code> if this scene contains it,
     *          <code>null</code> otherwise
     *
     * @since   1.0.0
     */
    public InteractiveGroup getGroup(String groupID) {
        for (InteractiveGroup group : groups) {
            if (group.getGroupID().equals(groupID)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Returns all <code>InteractiveGroups</code> for this scene.
     *
     * @return  <code>Set</code> of <code>InteractiveGroups</code> for this scene
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> getGroups() {
        return groups;
    }

    /**
     * Returns the <code>InteractiveControl</code> matching by <code>controlID</code> if this scene contains it,
     * <code>null</code> otherwise.
     *
     * @param   controlID
     *          Unique identifier for a <code>InteractiveControl</code>
     *
     * @return  The <code>InteractiveControl</code> matching by <code>controlID</code> if this scene contains it,
     *          <code>null</code> otherwise
     *
     * @since   1.0.0
     */
    public InteractiveControl getControl(String controlID) {
        for (InteractiveControl control : controls) {
            if (control.getControlID().equals(controlID)) {
                return control;
            }
        }
        return null;
    }

    /**
     * Returns all <code>InteractiveControls</code> for this scene.
     *
     * @return  <code>Set</code> of <code>InteractiveControls</code> for this scene
     *
     * @since   1.0.0
     */
    public Set<InteractiveControl> getControls() {
        return controls;
    }

    /**
     * {@inheritDoc}
     *
     * @see     InteractiveResource#getThis()
     *
     * @since   1.0.0
     */
    @Override
    protected InteractiveScene getThis() {
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
                    this.meta = ((InteractiveScene) o).meta;
                    this.controls.clear();
                    this.controls.addAll(((InteractiveScene) o).getControls());
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
     *          {@link InteractiveMethod#CREATE_SCENES create} method call completes with no errors
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

        return gameClient.using(SCENE_SERVICE_PROVIDER).create(this)
                .thenCompose(scenes -> CompletableFuture.supplyAsync(() -> syncIfEqual(scenes)));
    }

    /**
     * Updates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the update operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#UPDATE_SCENES update} method call completes with no errors
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

        return gameClient.using(SCENE_SERVICE_PROVIDER).update(this)
                .thenCompose(scenes -> CompletableFuture.supplyAsync(() -> syncIfEqual(scenes)));
    }

    /**
     * Deletes <code>this</code> from the Interactive service, reassigning groups on this scene to the default scene.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     IInteractiveDeletable#delete(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public CompletableFuture<Boolean> delete(GameClient gameClient) {
        return delete(gameClient, DEFAULT_SCENE);
    }

    /**
     * Deletes <code>this</code> from the Interactive service, reassigning groups on this scene to the specified scene.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     * @param   reassignSceneID
     *          Identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> will be
     *          reassigned to
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> delete(GameClient gameClient, String reassignSceneID) {
        return gameClient != null
                ? gameClient.using(SCENE_SERVICE_PROVIDER).delete(sceneID, reassignSceneID)
                : CompletableFuture.completedFuture(false);
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int compareTo(InteractiveScene o) {
        return (o != null) ? sceneID.compareTo(o.sceneID) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int hashCode() {
        return Hashing.md5().newHasher()
                .putString(sceneID != null ? sceneID : "", StandardCharsets.UTF_8)
                .putObject(controls, (Funnel<Set<InteractiveControl>>) (interactiveControls, into) -> {
                    if (interactiveControls != null) {
                        interactiveControls.forEach(control -> into.putInt(control.hashCode()));
                    }
                })
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
        return o instanceof InteractiveScene && this.compareTo((InteractiveScene) o) == 0;
    }
}
