package com.mixer.interactive.services;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.scene.InteractiveScene;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.mixer.interactive.GameClient.RPC_SERVICE_PROVIDER;

/**
 * Provides all functionality relating to making requests and interpreting replies from the Interactive service
 * regarding scenes.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveScene
 *
 * @since       1.0.0
 */
public class SceneServiceProvider extends AbstractServiceProvider {

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveScenes</code>.
     */
    private static final Type SCENE_SET_TYPE = new TypeToken<Set<InteractiveScene>>(){}.getType();

    /**
     * Empty Json object for use in several method calls.
     */
    private static final JsonElement EMPTY_JSON_OBJECT = new JsonObject();

    /**
     * Constant representing the default scene IDs
     */
    private static final String DEFAULT_VALUE = "default";

    /**
     * Collection of parameter key names for various method calls and events
     */
    private static final String PARAM_UPDATE_PRIORITY = "priority";
    private static final String PARAM_KEY_SCENES = "scenes";
    private static final String PARAM_KEY_SCENE_ID = "sceneID";
    private static final String PARAM_KEY_REASSIGN_SCENE_ID = "reassignSceneID";

    /**
     * Initializes a new <code>SceneServiceProvider</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this service provider
     * @since   1.0.0
     */
    public SceneServiceProvider(GameClient gameClient) {
        super(gameClient);
    }

    /**
     * <p>Retrieves all the scenes for the Interactive integration.</p>
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
     *          <code>InteractiveScenes</code> for the currently connected Interactive integration
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveScene>> getScenes() {
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.GET_SCENES, EMPTY_JSON_OBJECT, PARAM_KEY_SCENES, SCENE_SET_TYPE);
    }

    /**
     * <p>Creates one or more new scenes. Scene IDs MUST be unique and not already exist in the Interactive
     * integration.</p>
     *
     * <p>Scene objects may also optionally include controls to be set on the scene initially, rather than requiring
     * further {@link InteractiveMethod#CREATE_CONTROLS} calls. If an initial set of controls are provided, they
     * MUST be fully-qualified, tagged control objects.</p>
     *
     * <p>The Interactive service will either create all scenes and controls, or fail in which case NONE of the
     * scenes and controls provided will be created. In no case will the Interactive service create a subset of scenes
     * and controls.</p>
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
     * @param   scenes
     *          An array of <code>InteractiveScenes</code> to be created
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of newly created
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveScene>> create(InteractiveScene ... scenes) {
        return create(scenes != null ? Arrays.asList(scenes) : Collections.emptySet());
    }

    /**
     * <p>Creates one or more new scenes. Scene IDs MUST be unique and not already exist in the Interactive
     * integration.</p>
     *
     * <p>Scene objects may also optionally include controls to be set on the scene initially, rather than requiring
     * further {@link InteractiveMethod#CREATE_CONTROLS} calls. If an initial set of controls are provided, they
     * MUST be fully-qualified, tagged control objects.</p>
     *
     * <p>The Interactive service will either create all scenes and controls, or fail in which case NONE of the
     * scenes and controls provided will be created. In no case will the Interactive service create a subset of scenes
     * and controls.</p>
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
     * @param   scenes
     *          A <code>Collection</code> of <code>InteractiveScenes</code> to be created
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of newly created
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveScene>> create(Collection<InteractiveScene> scenes) {
        if (scenes == null) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_SCENES, GameClient.GSON.toJsonTree(scenes));
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.CREATE_SCENES, jsonParams, PARAM_KEY_SCENES, SCENE_SET_TYPE);
    }

    /**
     * <p>Updates scenes that already exist. The Interactive service will reply with a set of updated scenes.</p>
     *
     * <p>The Interactive service will either update all the scenes provided, or fail in which case NONE of the
     * scenes provided will be updated. In no case will the Interactive service apply updates to a subset of
     * scenes.</p>
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
     * @param   scenes
     *          An array of <code>InteractiveScenes</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveScene>> update(InteractiveScene ... scenes) {
        return update(0, scenes);
    }

    /**
     * <p>Updates scenes that already exist. The Interactive service will reply with a set of updated scenes.</p>
     *
     * <p>The Interactive service will either update all the scenes provided, or fail in which case NONE of the
     * scenes provided will be updated. In no case will the Interactive service apply updates to a subset of
     * scenes.</p>
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
     * @param   scenes
     *          An array of <code>InteractiveScenes</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveScene>> update(int priority, InteractiveScene ... scenes) {
        return update(priority, scenes != null ? Arrays.asList(scenes) : Collections.emptySet());
    }

    /**
     * <p>Updates scenes that already exist. The Interactive service will reply with a set of updated scenes with.</p>
     *
     * <p>The Interactive service will either update all the scenes provided, or fail in which case NONE of the
     * scenes provided will be updated. In no case will the Interactive service apply updates to a subset of
     * scenes.</p>
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
     * @param   scenes
     *          A <code>Collection</code> of <code>InteractiveScenes</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveScene>> update(Collection<InteractiveScene> scenes) {
        return update(0, scenes);
    }

    /**
     * <p>Updates scenes that already exist. The Interactive service will reply with a set of updated scenes with.</p>
     *
     * <p>The Interactive service will either update all the scenes provided, or fail in which case NONE of the
     * scenes provided will be updated. In no case will the Interactive service apply updates to a subset of
     * scenes.</p>
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
     * @param   scenes
     *          A <code>Collection</code> of <code>InteractiveScenes</code> to be updated
     *
     * @return  A <code>CompletableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveScenes</code>
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Set<InteractiveScene>> update(int priority, Collection<InteractiveScene> scenes) {
        if (scenes == null) {
            return CompletableFuture.completedFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.add(PARAM_KEY_SCENES, GameClient.GSON.toJsonTree(scenes));
        jsonParams.addProperty(PARAM_UPDATE_PRIORITY, priority);
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.UPDATE_SCENES, jsonParams, PARAM_KEY_SCENES, SCENE_SET_TYPE);
    }

    /**
     * <p>Removes a scene from the Interactive integration, reassigning any groups who were on that scene to the default
     * one. The server MAY not return an error if the scene to remove does not exist.</p>
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
     *          Identifier for an <code>InteractiveScene</code> to be deleted
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> delete(String sceneID) {
        return delete(sceneID, DEFAULT_VALUE);
    }

    /**
     * <p>Removes a scene from the Interactive integration, reassigning any groups who were on that scene to a different
     * one. The server MAY not return an error if the scene to remove does not exist.</p>
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
     *          Identifier for an <code>InteractiveScene</code> to be deleted
     * @param   reassignSceneID
     *          Identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> will be
     *          reassigned to
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_SCENE deleteScene} method call completes with no errors
     *
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> delete(String sceneID, String reassignSceneID) {
        if (sceneID == null || reassignSceneID == null) {
            return CompletableFuture.completedFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.addProperty(PARAM_KEY_REASSIGN_SCENE_ID, reassignSceneID);
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.DELETE_SCENE, jsonParams);
    }
}
