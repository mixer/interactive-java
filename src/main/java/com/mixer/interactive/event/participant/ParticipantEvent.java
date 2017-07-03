package com.mixer.interactive.event.participant;

import com.mixer.interactive.event.InteractiveEvent;
import com.mixer.interactive.resources.participant.InteractiveParticipant;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The class <code>ParticipantEvent</code> is the superclass of all classes relating to participants sent by the
 * Interactive service to the <code>GameClient</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ParticipantEvent extends InteractiveEvent {

    /**
     * The <code>Set</code> of <code>InteractiveParticipants</code> included in the event.
     */
    private final Set<InteractiveParticipant> participants = new HashSet<>();

    /**
     * Initializes a new <code>ParticipantEvent</code>.
     *
     * @param   participants
     *          A <code>Collection</code> of <code>InteractiveParticipants</code>
     *
     * @since   1.0.0
     */
    ParticipantEvent(Collection<InteractiveParticipant> participants) {
        if (participants != null) {
            this.participants.addAll(participants);
        }
    }

    /**
     * Returns the <code>Set</code> of <code>InteractiveParticipants</code> included in the event.
     *
     * @return  A <code>Set</code> of <code>InteractiveParticipants</code>
     *
     * @since   1.0.0
     */
    public Set<InteractiveParticipant> getParticipants() {
        return participants;
    }
}
