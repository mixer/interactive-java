package com.mixer.interactive.event.scene;

/**
 * Interactive event posted by the Interactive service when a scene has been deleted.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class SceneDeleteEvent extends SceneEvent {

    /**
     * The identifier for the <code>InteractiveScene</code> that has been deleted
     */
    private final String sceneID;

    /**
     * The identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> have been
     * reassigned to
     */
    private final String reassignSceneID;

    /**
     * Initializes a new <code>SceneDeleteEvent</code>.
     *
     * @param   sceneID
     *          The identifier for the <code>InteractiveScene</code> that has been deleted
     * @param   reassignSceneID
     *          The identifier for the <code>InteractiveScene</code> that <code>InteractiveGroup</code> have
     *          been reassigned to
     *
     * @since   1.0.0
     */
    public SceneDeleteEvent(String sceneID, String reassignSceneID) {
        this.sceneID = sceneID;
        this.reassignSceneID = reassignSceneID;
    }

    /**
     * Returns the identifier for the <code>InteractiveScene</code> that has been deleted.
     *
     * @return  The identifier for the <code>InteractiveScene</code> that has been deleted
     *
     * @since   1.0.0
     */
    public String getSceneID() {
        return sceneID;
    }

    /**
     * Returns the identifier for the <code>InteractiveScene</code> that <code>InteractiveGroups</code> have
     * been reassigned to.
     *
     * @return  The identifier for the <code>InteractiveScene</code> that <code>InteractiveGroup</code> have
     *          been reassigned to
     *
     * @since   1.0.0
     */
    public String getReassignSceneID() {
        return reassignSceneID;
    }
}
