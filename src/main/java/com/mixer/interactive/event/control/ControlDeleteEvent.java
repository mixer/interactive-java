package com.mixer.interactive.event.control;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Interactive event posted by the Interactive service when a control has been deleted.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ControlDeleteEvent extends ControlEvent {

    /**
     * The <code>Set</code> of identifiers of <code>InteractiveControls</code> that have been deleted.
     */
    private final Set<String> controlIds = new HashSet<>();

    /**
     * Initializes a new <code>ControlDeleteEvent</code>.
     *
     * @param   sceneID
     *          Identifier for the <code>InteractiveScene</code> that contains the <code>InteractiveControls</code>
     *          that have been deleted
     * @param   controlIds
     *          A <code>Collection</code> of identifiers of <code>InteractiveControls</code> that have been deleted
     *
     * @since   2.0.0
     */
    public ControlDeleteEvent(String sceneID, Collection<String> controlIds) {
        super(sceneID);
        this.controlIds.addAll(controlIds);
    }

    /**
     * Returns the <code>Set</code> of identifiers of <code>InteractiveControls</code> included in the event.
     *
     * @return  A <code>Set</code> of identifiers of <code>InteractiveControls</code>
     *
     * @since   2.0.0
     */
    public Set<String> getControlIds() {
        return controlIds;
    }
}
