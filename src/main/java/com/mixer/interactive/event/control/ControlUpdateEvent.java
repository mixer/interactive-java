package com.mixer.interactive.event.control;

import com.mixer.interactive.resources.control.InteractiveControl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Interactive event posted by the Interactive service when a control has been updated.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ControlUpdateEvent extends ControlEvent {

    /**
     * The <code>Set</code> of <code>InteractiveControls</code> included in the event.
     */
    private final Set<InteractiveControl> controls = new HashSet<>();

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
        super(sceneID);
        this.controls.addAll(controls);
    }

    /**
     * Returns the <code>Set</code> of <code>InteractiveControls</code> included in the event.
     *
     * @return  A <code>Set</code> of <code>InteractiveControls</code>
     *
     * @since   2.0.0
     */
    public Set<InteractiveControl> getControls() {
        return controls;
    }
}
