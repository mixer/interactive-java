package com.mixer.interactive.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * The class <code>InteractivePacket</code> is the superclass of all packet classes representing messages to and from
 * the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public abstract class InteractivePacket {

    /**
     * Unique numeric identifier for the packet (an unsigned 32-bit integer)
     */
    private final int id;

    /**
     * Type of packet.
     */
    private final String type;

    /**
     * Sequence number of the packet.
     */
    @SerializedName("seq")
    private int sequenceNumber;

    /**
     * Initializes a new <code>InteractivePacket</code>.
     *
     * @param   id
     *          Unique numeric identifier for the packet
     * @param   type
     *          Type of packet. Must be one of either <code>method</code> or <code>reply</code>
     *
     * @since   1.0.0
     */
    InteractivePacket(int id, String type) {
        this(id, 0, type);
    }

    /**
     * Initializes a new <code>InteractivePacket</code>.
     *
     * @param   id
     *          Unique numeric identifier for the packet
     * @param   sequenceNumber
     *          Unique sequence number for the packet
     * @param   type
     *          Type of packet. Must be one of either <code>method</code> or <code>reply</code>
     *
     * @since   1.0.0
     */
    InteractivePacket(int id, int sequenceNumber, String type) {
        if (!"method".equals(type) && !"reply".equals(type)) {
            throw new IllegalArgumentException("Packet type be either 'method' or 'reply'");
        }

        this.id = id;
        this.type = type;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Returns the packet identifier.
     *
     * @return  The packet identifier
     *
     * @since   1.0.0
     */
    public int getPacketID() {
        return id;
    }

    /**
     * Returns the packet type.
     *
     * @return  The packet type
     *
     * @since   1.0.0
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the packet sequence number. This is primarily for internal 'low level' use in the socket. It increments
     * by 1 on every received packet.
     *
     * @return  The packet sequence number
     *
     * @since   1.0.0
     */
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the packet sequence number. This is primarily for internal 'low level' use in the socket in outgoing
     * packets.
     *
     * @param   sequenceNumber
     *          Unique sequence number for the packet
     *
     * @since   1.0.0
     */
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
