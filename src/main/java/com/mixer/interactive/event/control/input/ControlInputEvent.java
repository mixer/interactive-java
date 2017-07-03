package com.mixer.interactive.event.control.input;

import com.mixer.interactive.event.InteractiveEvent;
import com.mixer.interactive.resources.control.InteractiveControlInput;
import com.mixer.interactive.resources.transaction.InteractiveTransaction;

/**
 * The class <code>ParticipantEvent</code> is the superclass of all classes relating to control input sent by the
 * Interactive service to the <code>GameClient</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ControlInputEvent extends InteractiveEvent {

    /**
     * Identifier for the <code>InteractiveParticipant</code> that supplied the <code>InteractiveControlInput</code>
     *
     * @see com.mixer.interactive.resources.participant.InteractiveParticipant#sessionID
     */
    protected final String participantID;

    /**
     * The <code>InteractiveTransaction</code> associated with this input event, if one exists
     */
    protected final InteractiveTransaction transaction;

    /**
     * The <code>InteractiveControlInput</code> that was supplied by the <code>InteractiveParticipant</code>
     */
    protected final InteractiveControlInput input;

    /**
     * Initializes a new <code>ControlInputEvent</code>.
     *
     * @param   participantID
     *          Identifier for the <code>InteractiveParticipant</code> that supplied the
     *          <code>InteractiveControlInput</code>
     * @param   transactionID
     *          Identifier for an <code>InteractiveTransaction</code> associated with the input event
     * @param   input
     *          The <code>InteractiveControlInput</code> that was supplied by the <code>InteractiveParticipant</code>
     *
     * @since   1.0.0
     */
    public ControlInputEvent(String participantID, String transactionID, InteractiveControlInput input) {
        this.participantID = participantID;

        if (transactionID != null && !transactionID.isEmpty()) {
            this.transaction = new InteractiveTransaction(transactionID);
        }
        else {
            this.transaction = null;
        }

        this.input = input;
    }

    /**
     * Returns the identifier for the <code>InteractiveParticipant</code> that supplied the
     * <code>InteractiveControlInput</code>
     *
     * @return  The identifier for the <code>InteractiveParticipant</code> that supplied the
     *          <code>InteractiveControlInput</code>
     *
     * @since   1.0.0
     */
    public String getParticipantID() {
        return participantID;
    }

    /**
     * Returns the <code>InteractiveTransaction</code> associated with this input event, if one exists.
     *
     * @return  The <code>InteractiveTransaction</code> associated with this input event if one exists,
     *          <code>null</code> otherwise
     *
     * @since   1.0.0
     */
    public InteractiveTransaction getTransaction() {
        return transaction;
    }

    /**
     * Returns the <code>InteractiveControlInput</code> that was supplied by the <code>InteractiveParticipant</code>
     *
     * @return  The <code>InteractiveControlInput</code> that was supplied by the <code>InteractiveParticipant</code>
     *
     * @since   1.0.0
     */
    public InteractiveControlInput getControlInput() {
        return input;
    }
}
