package com.mixer.interactive.gson;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.mixer.interactive.resources.control.InteractiveControl;
import com.mixer.interactive.resources.group.InteractiveGroup;
import com.mixer.interactive.resources.scene.InteractiveScene;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * De-serializes a <code>InteractiveScene</code>.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveScene
 *
 * @since       1.0.0
 */
public class InteractiveSceneAdapter implements JsonDeserializer<InteractiveScene> {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Type object used to serialize/de-serialize a <code>Set</code> of <code>InteractiveGroups</code>.
     */
    private static final Type GROUP_SET_TYPE = new TypeToken<Set<InteractiveGroup>>(){}.getType();

    /**
     * Collection of parameter key names
     */
    private static final String PARAM_KEY_SCENE_ID = "sceneID";
    private static final String PARAM_KEY_GROUPS = "groups";
    private static final String PARAM_KEY_CONTROLS = "controls";
    private static final String PARAM_META = "meta";

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public InteractiveScene deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        if (!json.isJsonObject()) {
            LOG.fatal("Unable to parse an InteractiveScene");
            throw new JsonParseException("Unable to parse an InteractiveScene");
        }

        Set<InteractiveGroup> groups = new HashSet<>();
        Set<InteractiveControl> controls = new HashSet<>();
        JsonObject metaObject = null;
        JsonObject jsonObject = json.getAsJsonObject();

        String sceneID = jsonObject.get(PARAM_KEY_SCENE_ID).getAsString();

        Set<InteractiveGroup> groupSet = context.deserialize(jsonObject.get(PARAM_KEY_GROUPS), GROUP_SET_TYPE);
        if (groupSet != null) {
            groups.addAll(groupSet);
        }

        // Injects the sceneID into the control objects being de-serialized
        if (jsonObject.has(PARAM_KEY_CONTROLS) && jsonObject.get(PARAM_KEY_CONTROLS).isJsonArray()) {
            for (JsonElement jsonElement : jsonObject.get(PARAM_KEY_CONTROLS).getAsJsonArray()) {
                if (jsonElement.isJsonObject()) {
                    jsonElement.getAsJsonObject().addProperty(PARAM_KEY_SCENE_ID, sceneID);
                    controls.add(context.deserialize(jsonElement, InteractiveControl.class));
                }
            }
        }

        if (jsonObject.has(PARAM_META) && jsonObject.get(PARAM_META).isJsonObject()) {
            metaObject = jsonObject.get(PARAM_META).getAsJsonObject();
        }

        return new InteractiveScene(sceneID, groups, controls).setMeta(metaObject);
    }
}
