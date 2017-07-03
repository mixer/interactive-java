package com.mixer.interactive.event.core;

import com.mixer.interactive.resources.core.InteractiveResourceMemoryStatistic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Interactive event posted by the Interactive service when the client reaches a prescribed threshold in their memory
 * limit. It will contain the number of bytes the client has allocated out of the total number of bytes, in addition to
 * a breakdown of how much memory is allocated where for debugging purposes.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class MemoryWarningEvent extends InteractiveCoreEvent {

    /**
     * Number of bytes used by the Interactive integration
     */
    private final int usedBytes;

    /**
     * Total number of bytes for the Interactive integration
     */
    private final int totalBytes;

    /**
     * A <code>Set</code> of <code>InteractiveResourceMemoryStatistics</code>
     */
    private final Set<InteractiveResourceMemoryStatistic> resourceMemoryStatistics = new HashSet<>();

    /**
     * Initializes a new <code>MemoryWarningEvent</code>.
     *
     * @param   usedBytes
     *          Number of bytes used by the Interactive integration
     * @param   totalBytes
     *          Total number of bytes for the Interactive integration
     * @param   resourceMemoryStatistics
     *          A <code>Collection</code> of <code>InteractiveResourceMemoryStatistics</code>
     *
     * @since   1.0.0
     */
    public MemoryWarningEvent(int usedBytes, int totalBytes, Collection<InteractiveResourceMemoryStatistic> resourceMemoryStatistics) {
        this.usedBytes = usedBytes;
        this.totalBytes = totalBytes;
        if (resourceMemoryStatistics != null) {
            this.resourceMemoryStatistics.addAll(resourceMemoryStatistics);
        }
    }

    /**
     * Returns the number of bytes used by the Interactive integration.
     *
     * @return  Number of bytes used by the Interactive integration
     *
     * @since   1.0.0
     */
    public int getUsedBytes() {
        return usedBytes;
    }

    /**
     * Returns the total number of bytes for the Interactive integration.
     *
     * @return  Total number of bytes for the Interactive integration
     *
     * @since   1.0.0
     */
    public int getTotalBytes() {
        return totalBytes;
    }

    /**
     * Returns a <code>Set</code> of <code>InteractiveResourceMemoryStatistics</code>.
     *
     * @return  A <code>Set</code> of <code>InteractiveResourceMemoryStatistics</code>
     *
     * @since   1.0.0
     */
    public Set<InteractiveResourceMemoryStatistic> getResourceMemoryStatistics() {
        return resourceMemoryStatistics;
    }
}
