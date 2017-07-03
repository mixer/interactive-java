package com.mixer.interactive.event.participant;

import com.mixer.interactive.resources.participant.InteractiveParticipant;

import java.util.Collection;

/**
 * Interactive event posted by the Interactive service when a participant(s) join the Interactive integration the client
 * is currently connected to.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ParticipantJoinEvent extends ParticipantEvent {

    /**
     * Initializes a new <code>ParticipantJoinEvent</code>.
     *
     * @param   participants
     *          A <code>Collection</code> of <code>InteractiveParticipants</code> that have joined the Interactive
     *          integration
     *
     * @since   1.0.0
     */
    public ParticipantJoinEvent(Collection<InteractiveParticipant> participants) {
        super(participants);
    }
}
