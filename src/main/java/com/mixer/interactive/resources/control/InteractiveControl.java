package com.mixer.interactive.resources.control;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.IInteractiveCreatable;
import com.mixer.interactive.resources.IInteractiveDeletable;
import com.mixer.interactive.resources.InteractiveResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.mixer.interactive.GameClient.CONTROL_SERVICE_PROVIDER;

/**
 * The abstract class <code>InteractiveControl</code> is the superclass for all controls on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @see         ButtonControl
 * @see         JoystickControl
 *
 * @since       1.0.0
 */
public abstract class InteractiveControl<T extends InteractiveResource<T>>
        extends InteractiveResource<T>
        implements IInteractiveCreatable, IInteractiveDeletable, Comparable<InteractiveControl> {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Unique identifier for this control
     */
    private final String controlID;

    /**
     * Unique identifier for the scene containing this control
     */
    private final String sceneID;

    /**
     * The kind of control this is
     */
    private final InteractiveControlType kind;

    /**
     * Whether or not this control is disabled
     */
    private boolean disabled;

    /**
     * <code>Set</code> of <code>InteractiveControlPositions</code> for this control
     */
    @SerializedName("position")
    private final Set<InteractiveControlPosition> controlPositions = new HashSet<>();

    /**
     * Initializes a new <code>InteractiveControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>InteractiveControl</code>
     * @param   kind
     *          The <code>InteractiveControlType</code> of this control
     *
     * @since   1.0.0
     */
    InteractiveControl(String controlID, InteractiveControlType kind) {
        this(controlID, null, kind);
    }

    /**
     * Initializes a new <code>InteractiveControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>InteractiveControl</code>
     * @param   kind
     *          The <code>InteractiveControlType</code> of this control
     * @param   controlPositions
     *          An array of initial <code>InteractiveControlPosition</code> for this control
     *
     * @since   1.0.0
     */
    InteractiveControl(String controlID, InteractiveControlType kind, InteractiveControlPosition ... controlPositions) {
        this(controlID, null, kind, controlPositions);
    }

    /**
     * Initializes a new <code>InteractiveControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>InteractiveControl</code>
     * @param   kind
     *          The kind of control this <code>InteractiveControl</code> is
     *
     * @since   1.0.0
     */
    InteractiveControl(String controlID, String kind) {
        this(controlID, InteractiveControlType.from(kind));
    }

    /**
     * Initializes a new <code>InteractiveControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>InteractiveControl</code>
     * @param   kind
     *          The kind of control this <code>InteractiveControl</code> is
     * @param   controlPositions
     *          An array of initial <code>InteractiveControlPosition</code> for this control
     *
     * @since   1.0.0
     */
    InteractiveControl(String controlID, String kind, InteractiveControlPosition ... controlPositions) {
        this(controlID, InteractiveControlType.from(kind), controlPositions);
    }

    /**
     * Initializes a new <code>InteractiveControl</code>.
     *
     * @param   controlID
     *          Unique identifier for the <code>InteractiveControl</code>
     * @param   sceneID
     *          Unique identifier for the <code>InteractiveScene</code> that contains this control
     * @param   kind
     *          The <code>InteractiveControlType</code> of this control
     * @param   controlPositions
     *          An array of initial <code>InteractiveControlPosition</code> for this control
     *
     * @since   1.0.0
     */
    InteractiveControl(String controlID, String sceneID, InteractiveControlType kind, InteractiveControlPosition ... controlPositions) {

        if (controlID != null && !controlID.isEmpty()) {
            this.controlID = controlID;
        }
        else {
            LOG.fatal(String.format("ControlID must be non-null and non-empty (was provided: %s)", controlID));
            throw new IllegalArgumentException("ControlID must be non-null and non-empty");
        }

        if (sceneID != null) {
            this.sceneID = sceneID;
        }
        else {
            this.sceneID = "default";
        }

        if (kind != null) {
            this.kind = kind;
        }
        else {
            LOG.error("Invalid control type");
            throw new IllegalArgumentException("Invalid control type");
        }

        if (controlPositions != null) {
            Collections.addAll(this.controlPositions, controlPositions);
        }
    }

    /**
     * Returns the unique identifier for the <code>InteractiveControl</code>.
     *
     * @return  The unique identifier for the <code>InteractiveControl</code>
     *
     * @since   1.0.0
     */
    public String getControlID() {
        return controlID;
    }

    /**
     * Returns the unique identifier for the <code>InteractiveScene</code> that contains this control.
     *
     * @return  The unique identifier for the <code>InteractiveControl</code> that contains this control
     *
     * @since   1.0.0
     */
    public String getSceneID() {
        return sceneID;
    }

    /**
     * Returns the kind of control this <code>InteractiveControl</code> is.
     *
     * @return  <code>InteractiveControlType</code> representing what kind of <code>InteractiveControl</code> this is
     *
     * @since   1.0.0
     */
    public InteractiveControlType getKind() {
        return kind;
    }

    /**
     * Returns <code>true</code> if this <code>InteractiveControlType</code> is disabled, <code>false</code> otherwise
     *
     * @return  <code>true</code> if this <code>InteractiveControlType</code> is disabled, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets the disabled state for this <code>InteractiveControlType</code>.
     *
     * @param   disabled
     *          <code>true</code> if this <code>InteractiveControlType</code> should be disabled,
     *          <code>false</code> otherwise
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T setDisabled(Boolean disabled) {
        if (disabled != null) {
            this.disabled = disabled;
        }
        else {
            this.disabled = false;
        }
        return getThis();
    }

    /**
     * Returns <code>true</code> if this <code>InteractiveControl</code> has a controlPositions for the provided canvas size,
     * <code>false</code> otherwise.
     *
     * @param   canvasSize
     *          The canvas size that is being checked for a controlPositions setting for this control
     *
     * @return  <code>true</code> if this <code>InteractiveControl</code> has a controlPositions for the provided
     *          canvas size, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean hasPositionFor(String canvasSize) {
        return hasPositionFor(InteractiveCanvasSize.from(canvasSize));
    }

    /**
     * Returns <code>true</code> if this <code>InteractiveControl</code> has a controlPositions for the provided
     * <code>InteractiveCanvasSize</code>, <code>false</code> otherwise.
     *
     * @param   canvasSize
     *          The <code>InteractiveCanvasSize</code> that is being checked for a controlPositions setting for this control
     *
     * @return  <code>true</code> if this <code>InteractiveControl</code> has a controlPositions for the provided
     *          <code>InteractiveCanvasSize</code>, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public boolean hasPositionFor(InteractiveCanvasSize canvasSize) {
        return getPositionFor(canvasSize) != null;
    }

    /**
     * Returns the <code>InteractiveControlPosition</code> for the provided canvas size if there exists one,
     * <code>null</code> otherwise.
     *
     * @param   canvasSize
     *          Interactive control canvas size
     *
     * @return  The <code>InteractiveControlPosition</code> if one exists for the provided canvas size,
     *          <code>null</code> otherwise
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition getPositionFor(String canvasSize) {
        return getPositionFor(InteractiveCanvasSize.from(canvasSize));
    }

    /**
     * Returns the <code>InteractiveControlPosition</code> for the provided <code>InteractiveCanvasSize</code> if
     * there exists one, <code>null</code> otherwise.
     *
     * @param   canvasSize
     *          A <code>InteractiveCanvasSize</code>
     *
     * @return  The <code>InteractiveControlPosition</code> for the provided <code>InteractiveCanvasSize</code> if
     *          there exists one, <code>null</code> otherwise.
     *
     * @since   1.0.0
     */
    public InteractiveControlPosition getPositionFor(InteractiveCanvasSize canvasSize) {
        for (InteractiveControlPosition controlPosition : controlPositions) {
            if (controlPosition.getCanvasSize().equals(canvasSize)) {
                return controlPosition;
            }
        }
        return null;
    }

    /**
     * Returns a <code>Set</code> of all <code>InteractiveControlPositions</code> for this
     * <code>InteractiveControl</code>.
     *
     * @return  A <code>Set</code> of all <code>InteractiveControlPositions</code> for this
     *          <code>InteractiveControl</code>
     *
     * @since   1.0.0
     */
    public Set<InteractiveControlPosition> getPositions() {
        return controlPositions;
    }

    /**
     * Adds <code>InteractiveControlPositions</code> to this <code>InteractiveControl</code>. If there already exists
     * a <code>InteractiveControlPosition</code> for a <code>InteractiveCanvasSize</code>, then the existing value is
     * for that canvas size is retained.
     *
     * @param   controlPositions
     *          An array of <code>InteractiveControlPositions</code> to be added to this <code>InteractiveControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T addPosition(InteractiveControlPosition ... controlPositions) {
        for (InteractiveControlPosition controlPosition : controlPositions) {
            if (controlPosition != null) {
                this.controlPositions.add(controlPosition);
            }
        }
        return getThis();
    }

    /**
     * Sets the <code>InteractiveControlPositions</code> for this <code>InteractiveControl</code>. All previous
     * positions are cleared and the new ones are applied in place of them.
     *
     * @param   controlPositions
     *          A <code>Collection</code> of <code>InteractiveControlPositions</code> to be set for this
     *          <code>InteractiveControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T setPositions(Collection<InteractiveControlPosition> controlPositions) {
        if (controlPositions != null) {
            return setPositions(controlPositions.toArray(new InteractiveControlPosition[0]));
        }
        return getThis();
    }

    /**
     * Sets the <code>InteractiveControlPositions</code> for this <code>InteractiveControl</code>. All previous
     * positions are cleared and the new ones are applied in place of them.
     *
     * @param   controlPositions
     *          An array of <code>InteractiveControlPositions</code> to be set for this <code>InteractiveControl</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T setPositions(InteractiveControlPosition ...  controlPositions) {
        if (controlPositions != null) {
            this.controlPositions.clear();
            Collections.addAll(this.controlPositions, controlPositions);
        }
        return getThis();
    }

    /**
     * Attempts to remove any <code>InteractiveControlPosition</code> associated with the provided
     * <code>InteractiveCanvasSize</code>.
     *
     * @param   canvasSize
     *          The <code>InteractiveCanvasSize</code> for which we would like to remove a
     *          <code>InteractiveControlPosition</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T removePosition(InteractiveCanvasSize canvasSize) {
        InteractiveControlPosition candidatePosition = null;
        for (InteractiveControlPosition controlPosition : controlPositions) {
            if (controlPosition.getCanvasSize().equals(canvasSize)) {
                candidatePosition = controlPosition;
            }
        }
        if (candidatePosition != null) {
            controlPositions.remove(candidatePosition);
        }
        return getThis();
    }

    /**
     * Attempts to remove any <code>InteractiveControlPosition</code> associated with the provided canvas size.
     *
     * @param   canvasSize
     *          The canvas size for which we would like to remove a <code>InteractiveControlPosition</code>
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T removePosition(String canvasSize) {
        removePosition(InteractiveCanvasSize.from(canvasSize));
        return getThis();
    }

    /**
     * Removes all <code>InteractiveControlPositions</code> associated with this <code>InteractiveControl</code>.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public T removeAllPositions() {
        controlPositions.clear();
        return getThis();
    }

    /**
     * Creates <code>this</code> in the default scene on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the create operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_CONTROLS create} method call completes with no errors
     *
     * @see     IInteractiveCreatable#create(GameClient)
     *
     * @since   2.0.0
     */
    @Override
    public CompletableFuture<Boolean> create(GameClient gameClient) {
        if (gameClient == null || controlPositions.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        return gameClient.using(CONTROL_SERVICE_PROVIDER).create(this)
                .thenCompose(promiseMap -> {
                    for (InteractiveControl control : promiseMap.keySet()) {
                        if (getControlID().equals(control.getControlID())) {
                            return promiseMap.get(control);
                        }
                    }
                    return CompletableFuture.completedFuture(false);
                });
    }

    /**
     * Asynchronously updates <code>this</code> in the specified on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the update operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#UPDATE_CONTROLS update} method call completes with no errors
     *
     * @since   2.0.0
     */
    public CompletableFuture<Boolean> update(GameClient gameClient) {
        if (gameClient == null || controlPositions.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        return gameClient.using(CONTROL_SERVICE_PROVIDER).update(Collections.singletonList(this))
                .thenCompose(updatePromises -> {
                    for (InteractiveControl control : updatePromises.keySet()) {
                        if (getControlID().equals(control.getControlID())) {
                            return updatePromises.get(control);
                        }
                    }
                    return CompletableFuture.supplyAsync(Collections::emptySet);
                })
                .thenCompose(updatedControls -> CompletableFuture.supplyAsync(() -> syncIfEqual(updatedControls)));
    }

    /**
     * Asynchronously deletes <code>this</code> from the specified scene.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS delete} method call completes with no errors
     *
     * @since   2.0.0
     */
    public CompletableFuture<Boolean> delete(GameClient gameClient) {
        if (gameClient == null) {
            return CompletableFuture.completedFuture(false);
        }

        return gameClient.using(CONTROL_SERVICE_PROVIDER).delete(this)
                .thenCompose(promiseMap -> {
                    for (InteractiveControl control : promiseMap.keySet()) {
                        if (getControlID().equals(control.getControlID())) {
                            return promiseMap.get(control);
                        }
                    }
                    return CompletableFuture.completedFuture(false);
                });
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int compareTo(InteractiveControl o) {
        return (o != null) ? controlID.compareTo(o.controlID) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int hashCode() {
        return Hashing.md5().newHasher()
                .putString(controlID, StandardCharsets.UTF_8)
                .putString(kind.toString(), StandardCharsets.UTF_8)
                .putBoolean(disabled)
                .putObject(controlPositions, (Funnel<Set<InteractiveControlPosition>>) (controlPositions, into) ->
                        controlPositions.forEach(controlPosition -> into.putInt(controlPosition.hashCode())))
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
        return o instanceof InteractiveControl && this.compareTo((InteractiveControl) o) == 0;
    }
}
