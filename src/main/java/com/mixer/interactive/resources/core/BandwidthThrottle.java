package com.mixer.interactive.resources.core;

import com.google.common.base.Objects;
import com.mixer.interactive.GameClient;

import java.util.Map;

/**
 * A <code>BandwidthThrottle</code> represents a throttle to be used by the Interactive service when sending messages to
 * the client.
 *
 * @author      Microsoft Corporation
 *
 * @see         GameClient#setBandwidthThrottle(Map)
 * @see         GameClient#setBandwidthThrottleAsync(Map)
 *
 * @since       1.0.0
 */
public class BandwidthThrottle {

    /**
     * The total bucket capacity (in bytes)
     */
    private final int capacity;

    /**
     * The drain rate (in bytes per second) for the bucket
     */
    private final int drainRate;

    /**
     * Initializes a new <code>BandwidthThrottle</code>.
     *
     * @param   capacity
     *          The total bucket capacity (in bytes)
     * @param   drainRate
     *          The drain rate (in bytes per second) for the bucket
     *
     * @since   1.0.0
     */
    public BandwidthThrottle(int capacity, int drainRate) {
        this.capacity = capacity;
        this.drainRate = drainRate;
    }

    /**
     * Returns the total bucket capacity.
     *
     * @return  The total bucket capacity
     *
     * @since   1.0.0
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the drain rate (in bytes per second) for the bucket.
     *
     * @return  The drain rate (in bytes per second) for the bucket
     *
     * @since   1.0.0
     */
    public int getDrainRate() {
        return drainRate;
    }

    /**
     * Returns a <code>String</code> representation of this <code>BandwidthThrottle</code>.
     *
     * @return  <code>String</code> representation of this <code>BandwidthThrottle</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("capacity", capacity)
                .add("drainRate", drainRate)
                .toString();
    }
}
