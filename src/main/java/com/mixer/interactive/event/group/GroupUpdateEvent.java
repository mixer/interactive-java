package com.mixer.interactive.event.group;

import com.mixer.interactive.resources.group.InteractiveGroup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Interactive event posted by the Interactive service when a group has been updated. This event is only posted
 * when an attribute of the group (e.g. a meta property or the sceneID) has changed. It is not posted if a participant
 * within the group is updated.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class GroupUpdateEvent extends GroupEvent {

    /**
     * A <code>Set</code> of <code>InteractiveGroups</code> that have been updated
     */
    private final Set<InteractiveGroup> groups = new HashSet<>();

    /**
     * Initializes a new <code>GroupUpdateEvent</code>.
     *
     * @param   groups
     *          A <code>Set</code> of <code>InteractiveGroups</code> that have been updated
     *
     * @since   1.0.0
     */
    public GroupUpdateEvent(Collection<InteractiveGroup> groups) {
        if (groups != null) {
            this.groups.addAll(groups);
        }
    }

    /**
     * Returns a <code>Set</code> of <code>InteractiveGroups</code> that have been updated.
     *
     * @return  A <code>Set</code> of <code>InteractiveGroups</code> that have been updated
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> getGroups() {
        return groups;
    }
}
