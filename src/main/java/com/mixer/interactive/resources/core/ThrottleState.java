package com.mixer.interactive.resources.core;

import com.mixer.interactive.protocol.InteractiveMethod;

/**
 * A class that represents the state of how packets have been sent and dropped as a result of throttling rules set up
 * in {@link InteractiveMethod#SET_BANDWIDTH_THROTTLE}. It contains the number of sent packets (ones inserted into the
 * bucket) and the number of rejected packets.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class ThrottleState {

    /**
     * A count of how many packets have been successfully sent
     */
    private final int inserted;

    /**
     * A count of how many packets have been dropped (were not sent successfully)
     */
    private final int rejected;

    /**
     * Initializes a new <code>ThrottleState</code>.
     *
     * @param   inserted
     *          A count of how many packets have been successfully sent
     * @param   rejected
     *          A count of how many packets have been dropped (were not sent successfully)
     *
     * @since   1.0.0
     */
    public ThrottleState(int inserted, int rejected) {
        this.inserted = inserted;
        this.rejected = rejected;
    }

    /**
     * Returns a count of how many packets have been successfully sent.
     *
     * @return  A count of how many packets have been successfully sent
     *
     * @since   1.0.0
     */
    public int getInsertedPacketCount() {
        return inserted;
    }

    /**
     * Returns a count of how many packets have been dropped (were not sent successfully) as a result of throttling
     * rules.
     *
     * @return  A count of how many packets have been dropped (were not sent successfully)
     *
     * @since   1.0.0
     */
    public int getRejectedPacketCount() {
        return rejected;
    }
}
