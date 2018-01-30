package com.mixer.interactive.event.control.input;

import com.mixer.interactive.resources.control.InteractiveControlInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Interactive event posted by the Interactive service when a control has a <code>keydown</code> input supplied.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.1.0
 */
public class ControlKeyDownEvent extends ControlInputEvent {

    /**
     * Initializes a new <code>ControlKeyDownInputEvent</code>.
     *
     * @param   participantID
     *          Identifier for the <code>InteractiveParticipant</code> that supplied the
     *          <code>InteractiveControlInput</code>
     * @param   transactionID
     *          Identifier for an <code>InteractiveTransaction</code> associated with the input event
     * @param   controlInput
     *          The <code>InteractiveControlInput</code> that was supplied by the <code>InteractiveParticipant</code>
     *
     * @since   1.0.0
     */
    public ControlKeyDownEvent(String participantID, String transactionID, InteractiveControlInput controlInput) {
        super(participantID, transactionID, controlInput);
    }
}
