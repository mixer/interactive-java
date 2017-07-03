package com.mixer.interactive.resources.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A <code>InteractiveMemoryStatistic</code> represents a point in time snapshot of the current memory usage for the
 * Interactive integration the client is currently connected to.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveMemoryStatistic {

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
    private final Set<InteractiveResourceMemoryStatistic> resources = new HashSet<>();

    /**
     * Initializes a new <code>InteractiveMemoryStatistic</code>.
     *
     * @param   usedBytes
     *          Number of bytes used by the Interactive integration
     * @param   totalBytes
     *          Total number of bytes for the Interactive integration
     * @param   resources
     *          A <code>Collection</code> of <code>InteractiveResourceMemoryStatistics</code>
     *
     * @since   1.0.0
     */
    public InteractiveMemoryStatistic(int usedBytes, int totalBytes, Collection<InteractiveResourceMemoryStatistic> resources) {
        this.usedBytes = usedBytes;
        this.totalBytes = totalBytes;
        if (resources != null) {
            this.resources.addAll(resources);
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
    public Set<InteractiveResourceMemoryStatistic> getResourceMemoryStats() {
        return resources;
    }
}
