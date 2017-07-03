package com.mixer.interactive.event.scene;

import com.mixer.interactive.resources.scene.InteractiveScene;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Interactive event posted by the Interactive service when a new scene has been created.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class SceneCreateEvent extends SceneEvent {

    /**
     * A <code>Set</code> of <code>InteractiveScene</code> that have been created
     */
    private final Set<InteractiveScene> scenes = new HashSet<>();

    /**
     * Initializes a new <code>SceneCreateEvent</code>.
     *
     * @param   scenes
     *          A <code>Collection</code> of <code>InteractiveScene</code> that have been created
     *
     * @since   1.0.0
     */
    public SceneCreateEvent(Collection<InteractiveScene> scenes) {
        if (scenes != null) {
            this.scenes.addAll(scenes);
        }
    }

    /**
     * Returns a <code>Set</code> of <code>InteractiveScene</code> that have been created.
     *
     * @return  A <code>Set</code> of <code>InteractiveScene</code> that have been created
     *
     * @since   1.0.0
     */
    public Set<InteractiveScene> getScenes() {
        return scenes;
    }
}
