package com.mixer.interactive.event.control;

import com.mixer.interactive.event.InteractiveEvent;
import com.mixer.interactive.resources.control.InteractiveControl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
     * The <code>Set</code> of <code>InteractiveControls</code> included in the event.
     */
    private final Set<InteractiveControl> controls = new HashSet<>();

    /**
     * Initializes a new <code>ControlEvent</code>.
     *
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code> containing the <code>InteractiveControls</code>
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code>
     *
     * @since   1.0.0
     */
    public ControlEvent(String sceneID, Collection<InteractiveControl> controls) {
        this.sceneID = sceneID;
        this.controls.addAll(controls);
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

    /**
     * Returns the <code>Set</code> of <code>InteractiveControls</code> included in the event.
     *
     * @return  A <code>Set</code> of <code>InteractiveControls</code>
     *
     * @since   1.0.0
     */
    public Set<InteractiveControl> getControls() {
        return controls;
    }
}
