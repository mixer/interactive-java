package com.mixer.interactive.event.scene;

import com.mixer.interactive.resources.scene.InteractiveScene;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Interactive event posted by the Interactive service when a scene has been updated.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class SceneUpdateEvent extends SceneEvent {

    /**
     * A <code>Set</code> of <code>InteractiveScenes</code> that have been updated
     */
    private final Set<InteractiveScene> scenes = new HashSet<>();

    /**
     * Initializes a new <code>SceneUpdateEvent</code>.
     *
     * @param   scenes
     *          A <code>Collection</code> of <code>InteractiveScenes</code> that have been updated
     *
     * @since   1.0.0
     */
    public SceneUpdateEvent(Collection<InteractiveScene> scenes) {
        if (scenes != null) {
            this.scenes.addAll(scenes);
        }
    }

    /**
     * Returns a <code>Set</code> of <code>InteractiveScenes</code> that have been updated.
     *
     * @return  A <code>Set</code> of <code>InteractiveScenes</code> that have been updated
     *
     * @since   1.0.0
     */
    public Set<InteractiveScene> getScenes() {
        return scenes;
    }
}
