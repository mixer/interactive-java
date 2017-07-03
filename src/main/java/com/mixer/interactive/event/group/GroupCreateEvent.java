package com.mixer.interactive.event.group;

import com.mixer.interactive.resources.group.InteractiveGroup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Interactive event posted by the Interactive service when a new group has been created.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class GroupCreateEvent extends GroupEvent {

    /**
     * A <code>Set</code> of <code>InteractiveGroups</code> that have been created
     */
    private final Set<InteractiveGroup> groups = new HashSet<>();

    /**
     * Initializes a new <code>GroupCreateEvent</code>.
     *
     * @param   groups
     *          A <code>Collection</code> of <code>InteractiveGroups</code> that have been created
     *
     * @since   1.0.0
     */
    public GroupCreateEvent(Collection<InteractiveGroup> groups) {
        if (groups != null) {
            this.groups.addAll(groups);
        }
    }

    /**
     * Returns a <code>Set</code> of <code>InteractiveGroups</code> that have been created.
     *
     * @return  A <code>Set</code> of <code>InteractiveGroups</code> that have been created
     *
     * @since   1.0.0
     */
    public Set<InteractiveGroup> getGroups() {
        return groups;
    }
}
