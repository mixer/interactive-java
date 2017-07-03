package com.mixer.interactive.resources.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A <code>InteractiveResourceMemoryStatistic</code> represents a point in time snapshot of the current memory usage for
 * a specific resource in the Interactive integration the client is currently connected to.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveResourceMemoryStatistic {

    /**
     * Identifier for the <code>InteractiveResource</code>
     */
    private final String id;

    /**
     * Number of bytes used by the <code>InteractiveResource</code>
     */
    private final int ownBytes;

    /**
     * Total number of bytes for the <code>InteractiveResource</code> and all of the <code>InteractiveResources</code>
     * that belong to it
     */
    private final int cumulativeBytes;

    /**
     * A <code>Set</code> of <code>InteractiveResourceMemoryStatistics</code> that belong to the
     * <code>InteractiveResource</code>
     */
    private final Set<InteractiveResourceMemoryStatistic> resources = new HashSet<>();

    /**
     * Initializes a new <code>InteractiveResourceMemoryStatistic</code>.
     *
     * @param   id
     *          Identifier for the <code>InteractiveResource</code>
     * @param   ownBytes
     *          Number of bytes used by the <code>InteractiveResource</code>
     * @param   cumulativeBytes
     *          Total number of bytes for the <code>InteractiveResource</code> and all of the
     *          <code>InteractiveResources</code> that belong to it
     * @param   resources
     *          A <code>Collection</code> of <code>InteractiveResourceMemoryStatistics</code> that belong to the
     *          <code>InteractiveResource</code>
     *
     * @since   1.0.0
     */
    public InteractiveResourceMemoryStatistic(String id, int ownBytes, int cumulativeBytes, Collection<InteractiveResourceMemoryStatistic> resources) {
        this.id = id;
        this.ownBytes = ownBytes;
        this.cumulativeBytes = cumulativeBytes;
        if (resources != null) {
            this.resources.addAll(resources);
        }
    }

    /**
     * Returns the identifier for the <code>InteractiveResource</code>.
     *
     * @return  Identifier for the <code>InteractiveResource</code>
     *
     * @since   1.0.0
     */
    public String getResourceID() {
        return id;
    }

    /**
     * Returns the number of bytes used by the <code>InteractiveResource</code>.
     *
     * @return  Number of bytes used by the <code>InteractiveResource</code>
     *
     * @since   1.0.0
     */
    public int getOwnBytes() {
        return ownBytes;
    }

    /**
     * Returns the total number of bytes for the <code>InteractiveResource</code> and all of the
     * <code>InteractiveResources</code> that belong to it.
     *
     * @return  Total number of bytes for the <code>InteractiveResource</code> and all of the
     *          <code>InteractiveResources</code> that belong to it
     *
     * @since   1.0.0
     */
    public int getCumulativeBytes() {
        return cumulativeBytes;
    }

    /**
     * Returns the <code>Set</code> of <code>InteractiveResourceMemoryStatistics</code> that belong to the
     * <code>InteractiveResource</code>.
     *
     * @return  <code>Set</code> of <code>InteractiveResourceMemoryStatistics</code> that belong to the
     *          <code>InteractiveResource</code>
     *
     * @since   1.0.0
     */
    public Set<InteractiveResourceMemoryStatistic> getResourceMemoryStats() {
        return resources;
    }
}
