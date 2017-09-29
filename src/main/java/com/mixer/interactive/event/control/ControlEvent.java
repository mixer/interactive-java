package com.mixer.interactive.event.control;

import com.mixer.interactive.event.InteractiveEvent;

/**
 * The class <code>ParticipantEvent</code> is the superclass of all classes relating to controls sent by the Interactive
 * service to the <code>GameClient</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ControlEvent extends InteractiveEvent {

    /**
     * Identifier for the <code>InteractiveScene</code> containing the <code>InteractiveControls</code>
     */
    private final String sceneID;

    /**
     * Initializes a new <code>ControlEvent</code>.
     *
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code> containing the <code>InteractiveControls</code>
     *
     * @since   2.0.0
     */
    public ControlEvent(String sceneID) {
        this.sceneID = sceneID;
    }

    /**
     * Returns the identifier for the <code>InteractiveScene</code> containing the <code>InteractiveControls</code>.
     *
     * @return  Identifier for the <code>InteractiveScene</code> containing the <code>InteractiveControls</code>
     *
     * @since   1.0.0
     */
    public String getSceneID() {
        return sceneID;
    }
}
