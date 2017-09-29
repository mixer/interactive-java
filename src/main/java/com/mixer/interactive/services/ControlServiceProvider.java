package com.mixer.interactive.services;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.scene.InteractiveScene;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.RPC_SERVICE_PROVIDER;
import static com.mixer.interactive.GameClient.SCENE_SERVICE_PROVIDER;

/**
 * Provides all functionality relating to making requests and interpreting replies from the Interactive service
 * regarding controls.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveGroup
 *
 * @since       1.0.0
 */
public class ControlServiceProvider extends AbstractServiceProvider {

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveControls</code>.
     */
    private static final Type CONTROL_SET_TYPE = new TypeToken<Set<InteractiveControl>>(){}.getType();

    /**
     * Collection of parameter key names for various method calls and events
     */
    private static final String PARAM_UPDATE_PRIORITY = "priority";
    private static final String PARAM_KEY_SCENE_ID = "sceneID";
    private static final String PARAM_KEY_CONTROLS = "controls";
    private static final String PARAM_KEY_CONTROL_IDS = "controlIDs";

    /**
     * Initializes a new <code>ControlServiceProvider</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this service provider
     *
     * @since   1.0.0
     */
    public ControlServiceProvider(GameClient gameClient) {
        super(gameClient);
    }

    /**
     * <p>Retrieves all the controls for the Interactive integration.</p>
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
     *          <code>InteractiveControls</code> for the currently connected Interactive integration
     *
     * @see     InteractiveControl
     *
     * @since   2.0.0
     */
    public final CompletableFuture<Set<InteractiveControl>> getControls() {
        return gameClient.using(SCENE_SERVICE_PROVIDER).getScenes()
                .thenCompose(scenes -> {
                    Set<InteractiveControl> controls = new HashSet<>();
                    scenes.forEach(scene -> controls.addAll(scene.getControls()));
                    return CompletableFuture.completedFuture(controls);
                });
    }

    /**
     * <p>Creates one or more new controls. Control IDs MUST be unique and not already exist in the Interactive
     * integration.</p>
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
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> containing mappings of
     *          <code>InteractiveControls</code> to the respective <code>CompletableFuture</code> holding the result
     *          of the request.
     *
     * @since   2.0.0
     */
    public CompletableFuture<Map<InteractiveControl, CompletableFuture<Boolean>>> create(InteractiveControl ... controls) {
        return create(Arrays.asList(controls));
    }

    /**
     * <p>Creates one or more new controls. Control IDs MUST be unique and not already exist in the Interactive
     * integration.</p>
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
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> containing mappings of
     *          <code>InteractiveControls</code> to the respective <code>CompletableFuture</code> holding the result
     *          of the request.
     *
     * @since   2.0.0
     */
    public CompletableFuture<Map<InteractiveControl, CompletableFuture<Boolean>>> create(Collection<InteractiveControl> controls) {
        return CompletableFuture.supplyAsync(() -> {
            Map<InteractiveControl, CompletableFuture<Boolean>> createPromises = new HashMap<>();
            groupControls(controls).forEach((sceneId, interactiveControls) -> {
                if (interactiveControls != null) {
                    CompletableFuture<Boolean> createPromise = create(sceneId, interactiveControls);
                    interactiveControls.forEach(control -> createPromises.put(control, createPromise));
                }
            });
            return createPromises;
        });
    }

    /**
     * <p>Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object
     * in this method.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_CONTROLS create} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    private CompletableFuture<Boolean> create(String sceneID, Collection<InteractiveControl> controls) {
        if (sceneID == null || controls == null) {
            return CompletableFuture.completedFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROLS, GameClient.GSON.toJsonTree(controls));
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.CREATE_CONTROLS, jsonParams);
    }

    /**
     * <p>Updates one or more new controls.</p>
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
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> containing mappings of
     *          <code>InteractiveControls</code> to the respective <code>CompletableFuture</code> holding the result
     *          of the request.
     *
     * @since   2.0.0
     */
    public CompletableFuture<Map<InteractiveControl, CompletableFuture<Set<InteractiveControl>>>> update(InteractiveControl ... controls) {
        return update(0, Arrays.asList(controls));
    }

    /**
     * <p>Updates one or more new controls.</p>
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
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> containing mappings of
     *          <code>InteractiveControls</code> to the respective <code>CompletableFuture</code> holding the result
     *          of the request.
     *
     * @since   2.0.0
     */
    public CompletableFuture<Map<InteractiveControl, CompletableFuture<Set<InteractiveControl>>>> update(int priority, InteractiveControl ... controls) {
        return update(priority, Arrays.asList(controls));
    }

    /**
     * <p>Updates one or more new controls.</p>
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
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> containing mappings of
     *          <code>InteractiveControls</code> to the respective <code>CompletableFuture</code> holding the result
     *          of the request.
     *
     * @since   2.0.0
     */
    public CompletableFuture<Map<InteractiveControl, CompletableFuture<Set<InteractiveControl>>>> update(Collection<InteractiveControl> controls) {
        return update(0, controls);
    }

    /**
     * <p>Updates one or more new controls.</p>
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
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> containing mappings of
     *          <code>InteractiveControls</code> to the respective <code>CompletableFuture</code> holding the result
     *          of the request.
     *
     * @since   2.0.0
     */
    public CompletableFuture<Map<InteractiveControl, CompletableFuture<Set<InteractiveControl>>>> update(int priority, Collection<InteractiveControl> controls) {
        return CompletableFuture.supplyAsync(() -> {
            Map<InteractiveControl, CompletableFuture<Set<InteractiveControl>>> updatePromises = new HashMap<>();
            groupControls(controls).forEach((sceneId, interactiveControls) -> {
                if (interactiveControls != null) {
                    CompletableFuture<Set<InteractiveControl>> updatePromise = update(priority, sceneId, interactiveControls);
                    interactiveControls.forEach(control -> updatePromises.put(control, updatePromise));
                }
            });
            return updatePromises;
        });
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveControls</code>
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    private CompletableFuture<Set<InteractiveControl>> update(int priority, String sceneID, Collection<InteractiveControl> controls) {
        if (sceneID == null || controls == null) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROLS, GameClient.GSON.toJsonTree(controls));
        jsonParams.addProperty(PARAM_UPDATE_PRIORITY, priority);
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.UPDATE_CONTROLS, jsonParams, PARAM_KEY_CONTROLS, CONTROL_SET_TYPE);
    }

    /**
     * <p>Deletes one or more new controls.</p>
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
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> containing mappings of
     *          <code>InteractiveControls</code> to the respective <code>CompletableFuture</code> holding the result
     *          of the request.
     *
     * @since   2.0.0
     */
    public CompletableFuture<Map<InteractiveControl, CompletableFuture<Boolean>>> delete(InteractiveControl ... controls) {
        return delete(Arrays.asList(controls));
    }

    /**
     * <p>Deletes one or more new controls.</p>
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
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Map</code> containing mappings of
     *          <code>InteractiveControls</code> to the respective <code>CompletableFuture</code> holding the result
     *          of the request.
     *
     * @since   2.0.0
     */
    public CompletableFuture<Map<InteractiveControl, CompletableFuture<Boolean>>> delete(Collection<InteractiveControl> controls) {
        return CompletableFuture.supplyAsync(() -> {
            Map<InteractiveControl, CompletableFuture<Boolean>> deletePromises = new HashMap<>();
            groupControls(controls).forEach((sceneId, interactiveControls) -> {
                if (interactiveControls != null) {
                    CompletableFuture<Boolean> deletePromise = delete(sceneId, interactiveControls.stream().map(InteractiveControl::getControlID).collect(Collectors.toSet()));
                    interactiveControls.forEach(control -> deletePromises.put(control, deletePromise));
                }
            });
            return deletePromises;
        });
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controlIDs
     *          A <code>Collection</code> of identifiers for <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS delete} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    private CompletableFuture<Boolean> delete(String sceneID, Collection<String> controlIDs) {
        if (sceneID == null || controlIDs == null) {
            return CompletableFuture.completedFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROL_IDS, GameClient.GSON.toJsonTree(controlIDs));
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.DELETE_CONTROLS, jsonParams);
    }

    /**
     * Groups controls by scene id.
     *
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code>
     *
     * @return  A <code>Map</code> containing <code>Collections</code> of <code>InteractiveControls</code> partitioned by
     *          the <code>String</code> scene id for the scene they are a member of.
     *
     * @since   2.0.0
     */
    private Map<String, Set<InteractiveControl>> groupControls(Collection<InteractiveControl> controls) {
        return controls.stream().collect(Collectors.groupingBy(InteractiveControl::getSceneID, Collectors.toSet()));
    }
}
