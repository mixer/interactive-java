package com.mixer.interactive.event.control.input;

import com.mixer.interactive.resources.control.InteractiveControlInput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Interactive event posted by the Interactive service when a control has a <code>move</code> input supplied.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ControlMoveInputEvent extends ControlInputEvent {

    /**
     * Logger.
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * X position of the joystick control (will be null if the input was provided from a button control)
     */
    private final Float x;

    /**
     * Y position of the joystick control (will be null if the input was provided from a button control)
     */
    private final Float y;

    /**
     * Initializes a new <code>ControlMoveInputEvent</code>.
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
    public ControlMoveInputEvent(String participantID, String transactionID, InteractiveControlInput controlInput) {
        super(participantID, transactionID, controlInput);

        if (!input.getRawInput().containsKey("x") || !input.getRawInput().containsKey("y")) {
            LOG.fatal("Could not find required parameters expected for ControlMoveInputEvent");
            throw new IllegalArgumentException("Could not find required parameters expected for ControlMoveInputEvent");
        }
        this.x = input.getRawInput().get("x").getAsFloat();
        this.y = input.getRawInput().get("y").getAsFloat();
    }

    /**
     * Returns the X position of the joystick control.
     *
     * @return  X position of the joystick control
     *
     * @since   1.0.0
     */
    public Float getX() {
        return x;
    }

    /**
     * Returns the Y position of the joystick control.
     *
     * @return  Y position of the joystick control
     *
     * @since   1.0.0
     */
    public Float getY() {
        return y;
    }
}
