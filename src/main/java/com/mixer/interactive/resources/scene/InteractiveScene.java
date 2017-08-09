package com.mixer.interactive.resources.scene;

import com.google.common.base.Objects;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonElement;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.IInteractiveCreatable;
import com.mixer.interactive.resources.IInteractiveDeletable;
import com.mixer.interactive.resources.IInteractiveUpdatable;
import com.mixer.interactive.resources.InteractiveResource;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.group.InteractiveGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

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
        implements IInteractiveCreatable<InteractiveScene>, IInteractiveDeletable, Comparable<InteractiveScene> {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InteractiveScene.class);

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
            LOG.error("SceneID must be non-null and non-empty");
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
     * Add <code>InteractiveControls</code> to this scene.
     *
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be added to this scene
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveScene addControls(InteractiveControl ... controls) {
        return addControls(Arrays.asList(controls));
    }

    /**
     * Add <code>InteractiveControls</code> to this scene.
     *
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be added to this scene
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveScene addControls(Collection<InteractiveControl> controls) {
        this.controls.addAll(controls);
        return getThis();
    }

    /**
     * Add a <code>InteractiveControl</code> to this scene.
     *
     * @param   supplier
     *          <code>Supplier</code> of a <code>InteractiveControl</code> that will be added to this scene
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveScene supplyControl(Supplier<InteractiveControl> supplier) {
        if (supplier != null) {
            return addControls(supplier.get());
        }
        return getThis();
    }

    /**
     * Remove <code>InteractiveControls</code> from this scene.
     *
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be removed from this scene
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveScene removeControls(InteractiveControl ... controls) {
        if (controls != null) {
            for (InteractiveControl control : controls) {
                this.controls.remove(control);
            }
        }
        return getThis();
    }

    /**
     * Remove <code>InteractiveControls</code> from this scene.
     *
     * @param   controlIDs
     *          An array of identifiers representing <code>InteractiveControls</code> to be removed from this scene
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveScene removeControls(String ... controlIDs) {
        for (String controlID : controlIDs) {
            controls.removeIf(control -> controlID.equals(control.getControlID()));
        }
        return getThis();
    }

    /**
     * Remove all <code>InteractiveControls</code> from this scene.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveScene removeAllControls() {
        controls.clear();
        return getThis();
    }

    /**
     * Sets the <code>InteractiveControls</code> for this scene, clearing out any pre-existing ones.
     *
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be added to this scene
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveScene setControls(InteractiveControl ... controls) {
        return removeAllControls().addControls(controls);
    }

    /**
     * Sets the <code>InteractiveControls</code> for this scene, clearing out any pre-existing ones.
     *
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be added to this scene
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveScene setControls(Collection<InteractiveControl> controls) {
        return removeAllControls().addControls(controls);
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
     * Iterates through a <code>Collection</code> of <code>InteractiveScenes</code>. If <code>this</code> is found to be
     * in the <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of <code>InteractiveScenes</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @see     InteractiveResource#syncIfEqual(Collection)
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveScene syncIfEqual(Collection<? extends InteractiveScene> objects) {
        if (objects != null) {
            for (InteractiveScene object : objects) {
                if (this.equals(object)) {
                    this.meta = object.meta;
                    this.controls.clear();
                    this.controls.addAll(object.getControls());
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
    public InteractiveScene create(GameClient gameClient) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException {
        if (gameClient != null) {
            syncIfEqual(gameClient.using(SCENE_SERVICE_PROVIDER).createScenes(this));
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
    public ListenableFuture<InteractiveScene> createAsync(GameClient gameClient) {
        return (gameClient != null)
                ? Futures.transform(gameClient.using(SCENE_SERVICE_PROVIDER).createScenesAsync(this), (AsyncFunction<Set<InteractiveScene>, InteractiveScene>) createdScenes -> Futures.immediateFuture(syncIfEqual(createdScenes)))
                : Futures.immediateFuture(getThis());
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
    public InteractiveScene update(GameClient gameClient) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException {
        if (gameClient != null) {
            syncIfEqual(gameClient.using(SCENE_SERVICE_PROVIDER).updateScenes(this));
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
    public ListenableFuture<InteractiveScene> updateAsync(GameClient gameClient) {
        return (gameClient != null)
                ? Futures.transform(gameClient.using(SCENE_SERVICE_PROVIDER).updateScenesAsync(this), (AsyncFunction<Set<InteractiveScene>, InteractiveScene>) updatedScenes -> Futures.immediateFuture(syncIfEqual(updatedScenes)))
                : Futures.immediateFuture(getThis());
    }

    /**
     * Deletes <code>this</code> from the Interactive service, reassigning groups on this scene to the default scene.
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
        delete(gameClient, DEFAULT_SCENE);
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
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @since   1.0.0
     */
    public void delete(GameClient gameClient, String reassignSceneID) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException {
        if (gameClient != null) {
            gameClient.using(SCENE_SERVICE_PROVIDER).deleteScene(sceneID, reassignSceneID);
        }
    }

    /**
     * Asynchronously deletes <code>this</code> from the Interactive service, reassigning groups on this scene to
     * the default scene.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     IInteractiveDeletable#deleteAsync(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public ListenableFuture<Boolean> deleteAsync(GameClient gameClient) {
        return deleteAsync(gameClient, DEFAULT_SCENE);
    }

    /**
     * Asynchronously deletes <code>this</code> from the Interactive service, reassigning groups on this scene to
     * the specified scene.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     * @param   reassignSceneID
     *          Identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> will be
     *          reassigned to
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteAsync(GameClient gameClient, String reassignSceneID) {
        return gameClient != null
                ? gameClient.using(SCENE_SERVICE_PROVIDER).deleteSceneAsync(sceneID, reassignSceneID)
                : Futures.immediateFuture(false);
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

    /**
     * Returns a <code>String</code> representation of this <code>InteractiveScene</code>.
     *
     * @return  <code>String</code> representation of this <code>InteractiveScene</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("sceneID", sceneID)
                .add("groups", groups)
                .add("controls", controls)
                .add("meta", getMeta())
                .toString();
    }
}
