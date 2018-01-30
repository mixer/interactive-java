package com.mixer.interactive.event.control.input;

import com.mixer.interactive.resources.control.InteractiveControlInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Interactive event posted by the Interactive service when a control has a <code>mousedown</code> input supplied.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ControlMouseDownInputEvent extends ControlInputEvent {

    /**
     * Keycode the participant used when interacting with the control
     */
    private final Integer button;

    /**
     * Initializes a new <code>ControlMouseDownInputEvent</code>.
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
    public ControlMouseDownInputEvent(String participantID, String transactionID, InteractiveControlInput controlInput) {
        super(participantID, transactionID, controlInput);

        if (!input.getRawInput().containsKey("button")) {
            this.button = null;
        }
        else {
            this.button = input.getRawInput().get("button").getAsInt();
        }
    }

    /**
     * Returns the keycode the participant used when interacting with the control.
     *
     * @return  Keycode the participant used when interacting with the control
     *
     * @since   1.0.0
     */
    public Integer getButton() {
        return button;
    }
}
