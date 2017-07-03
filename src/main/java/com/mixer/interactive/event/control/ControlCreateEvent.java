package com.mixer.interactive.event.control;

import com.mixer.interactive.resources.control.InteractiveControl;

import java.util.Collection;

/**
 * Interactive event posted by the Interactive service when a control has been created.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ControlCreateEvent extends ControlEvent {

    /**
     * Initializes a new <code>ControlCreateEvent</code>.
     *
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code> that contains the <code>InteractiveControls</code>
     *          that have been created
     * @param   controls
     *          A <code>Collection</code> of <code>InteractiveControls</code> that have been created
     *
     * @since   1.0.0
     */
    public ControlCreateEvent(String sceneID, Collection<InteractiveControl> controls) {
        super(sceneID, controls);
    }
}
