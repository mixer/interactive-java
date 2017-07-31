package com.mixer.interactive.services;

import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.scene.InteractiveScene;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mixer.interactive.GameClient.RPC_SERVICE_PROVIDER;

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
     * Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object in
     * this method; the etags provided will be used as their initial values.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be created
     *
     * @throws InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void createControls(String sceneID, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        createControls(sceneID, controls.toArray(new InteractiveControl[0]));
    }

    /**
     * Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object in
     * this method; the etags provided will be used as their initial values.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be created
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void createControls(String sceneID, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && controls != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
            jsonParams.add(PARAM_KEY_CONTROLS, GameClient.GSON.toJsonTree(controls, InteractiveControl[].class));
            gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.CREATE_CONTROLS, jsonParams);
        }
    }

    /**
     * <p>Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object
     * in this method; the etags provided will be used as their initial values.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_CONTROLS createControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createControlsAsync(String sceneID, Collection<InteractiveControl> controls) {
        return (sceneID != null && controls != null) ? createControlsAsync(sceneID, controls.toArray(new InteractiveControl[0])) : Futures.immediateFuture(false);
    }

    /**
     * <p>Creates one or more new controls in a scene. The client MUST provide a fully qualified, tagged control object
     * in this method; the etags provided will be used as their initial values.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> that will contain the controls being created
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be created
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CREATE_CONTROLS createControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> createControlsAsync(String sceneID, InteractiveControl ... controls) {
        if (sceneID == null || controls == null) {
            return Futures.immediateFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROLS, GameClient.GSON.toJsonTree(controls, InteractiveControl[].class));
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequestAsync(InteractiveMethod.CREATE_CONTROLS, jsonParams);
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveControls</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveControl> updateControls(String sceneID, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        return (sceneID != null && controls != null) ? updateControls(sceneID, controls.toArray(new InteractiveControl[0])) : Collections.emptySet();
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>Set</code> of updated <code>InteractiveControls</code>
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public Set<InteractiveControl> updateControls(String sceneID, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID == null || controls == null) {
            return Collections.emptySet();
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROLS, GameClient.GSON.toJsonTree(controls, InteractiveControl[].class));
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.UPDATE_CONTROLS, jsonParams, PARAM_KEY_CONTROLS, CONTROL_SET_TYPE);
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveControls</code>
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveControl>> updateControlsAsync(String sceneID, Collection<InteractiveControl> controls) {
        return (sceneID != null && controls != null) ? updateControlsAsync(sceneID, controls.toArray(new InteractiveControl[0])) : Futures.immediateFuture(Collections.emptySet());
    }

    /**
     * <p>Updates control objects already present in a scene. The Interactive service will reply with a set of updated
     * controls with their new etags.</p>
     *
     * <p>The Interactive service will either update all the controls provided, or fail in which case NONE of the
     * controls provided will be updated. In no case will the Interactive service apply updates to a subset of
     * controls.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be updated
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be updated
     *
     * @return  A <code>ListenableFuture</code> that when complete returns a <code>Set</code> of updated
     *          <code>InteractiveControls</code>
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Set<InteractiveControl>> updateControlsAsync(String sceneID, InteractiveControl ... controls) {
        if (sceneID == null || controls == null) {
            return Futures.immediateFuture(Collections.emptySet());
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROLS, GameClient.GSON.toJsonTree(controls, InteractiveControl[].class));
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequestAsync(InteractiveMethod.UPDATE_CONTROLS, jsonParams, PARAM_KEY_CONTROLS, CONTROL_SET_TYPE);
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(String sceneID, Collection<InteractiveControl> controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && controls != null) {
            deleteControls(sceneID, controls.toArray(new InteractiveControl[0]));
        }
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(String sceneID, InteractiveControl ... controls) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && controls != null) {
            deleteControls(sceneID, Arrays.stream(controls)
                    .map(InteractiveControl::getControlID)
                    .collect(Collectors.toSet())
                    .toArray(new String[0]));
        }
    }

    /**
     * Removes one or more controls from the provided scene.
     *
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controlIDs
     *          An array of identifiers for <code>InteractiveControls</code> to be deleted
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public void deleteControls(String sceneID, String ... controlIDs) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (sceneID != null && controlIDs != null) {
            JsonObject jsonParams = new JsonObject();
            jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
            jsonParams.add(PARAM_KEY_CONTROL_IDS, GameClient.GSON.toJsonTree(controlIDs, String[].class));
            gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.DELETE_CONTROLS, jsonParams);
        }
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS deleteControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteControlsAsync(String sceneID, Collection<InteractiveControl> controls) {
        return (sceneID != null && controls != null) ? deleteControlsAsync(sceneID, controls.toArray(new InteractiveControl[0])) : Futures.immediateFuture(false);
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controls
     *          An array of <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS deleteControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteControlsAsync(String sceneID, InteractiveControl ... controls) {
        if (sceneID == null || controls == null) {
            return Futures.immediateFuture(false);
        }

        return deleteControlsAsync(sceneID, Arrays.stream(controls)
                .map(InteractiveControl::getControlID)
                .collect(Collectors.toSet())
                .toArray(new String[0]));
    }

    /**
     * <p>Removes one or more controls from the provided scene.</p>
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
     * @param   sceneID
     *          Identifier for an <code>InteractiveScene</code> containing the controls to be deleted
     * @param   controlIDs
     *          An array of identifiers for <code>InteractiveControls</code> to be deleted
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#DELETE_CONTROLS deleteControls} method call completes with no errors
     *
     * @see     InteractiveControl
     * @see     InteractiveScene
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> deleteControlsAsync(String sceneID, String ... controlIDs) {
        if (sceneID == null || controlIDs == null) {
            return Futures.immediateFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_SCENE_ID, sceneID);
        jsonParams.add(PARAM_KEY_CONTROL_IDS, GameClient.GSON.toJsonTree(controlIDs, String[].class));
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequestAsync(InteractiveMethod.DELETE_CONTROLS, jsonParams);
    }
}
