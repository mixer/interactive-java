package com.mixer.interactive.event.control;

import com.mixer.interactive.resources.control.InteractiveControl;

import java.util.Collection;

/**
 * Interactive event posted by the Interactive service when a control has been updated.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ControlUpdateEvent extends ControlEvent {

    /**
     * Initializes a new <code>ControlUpdateEvent</code>.
     *
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code> that contains the <code>InteractiveControls</code>
     *          that have been updated
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> that have been updated
     *
     * @since   1.0.0
     */
    public ControlUpdateEvent(String sceneID, Collection<InteractiveControl> controls) {
        super(sceneID, controls);
    }
}
