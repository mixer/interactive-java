package com.mixer.interactive.event.group;

/**
 * Interactive event posted by the Interactive service when a group has been deleted.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class GroupDeleteEvent extends GroupEvent {

    /**
     * The identifier for the <code>InteractiveGroup</code> that has been deleted
     */
    private final String groupID;

    /**
     * The identifier for the <code>InteractiveGroup</code> that <code>InteractiveParticipants</code> have been
     * reassigned to
     */
    private final String reassignGroupID;

    /**
     * Initializes a new <code>GroupDeleteEvent</code>.
     *
     * @param   groupID
     *          The identifier for the <code>InteractiveGroup</code> that has been deleted
     * @param   reassignGroupID
     *          The identifier for the <code>InteractiveGroup</code> that <code>InteractiveParticipants</code> have been
     *          reassigned to
     *
     * @since   1.0.0
     */
    public GroupDeleteEvent(String groupID, String reassignGroupID) {
        this.groupID = groupID;
        this.reassignGroupID = reassignGroupID;
    }

    /**
     * Returns the identifier for the <code>InteractiveGroup</code> that has been deleted.
     *
     * @return  The identifier for the <code>InteractiveGroup</code> that has been deleted
     *
     * @since   1.0.0
     */
    public String getGroupID() {
        return groupID;
    }

    /**
     * Returns the identifier for the <code>InteractiveGroup</code> that <code>InteractiveParticipants</code> have
     * been reassigned to.
     *
     * @return  The identifier for the <code>InteractiveGroup</code> that <code>InteractiveParticipants</code> have
     *          been reassigned to
     *
     * @since   1.0.0
     */
    public String getReassignGroupID() {
        return reassignGroupID;
    }
}
