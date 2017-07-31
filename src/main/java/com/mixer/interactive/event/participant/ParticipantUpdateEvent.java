package com.mixer.interactive.event.participant;

import com.mixer.interactive.resources.participant.InteractiveParticipant;

import java.util.Collection;

/**
 * Interactive event posted by the Interactive service when a participant(s) has been updated (for example, the
 * participant's group has been changed).
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ParticipantUpdateEvent extends ParticipantEvent {

    /**
     * Initializes a new <code>ParticipantUpdateEvent</code>.
     *
     * @param   participants
     *          A <code>Collection</code> of <code>InteractiveParticipants</code> that have been updated
     *
     * @since   1.0.0
     */
    public ParticipantUpdateEvent(Collection<InteractiveParticipant> participants) {
        super(participants);
    }
}
