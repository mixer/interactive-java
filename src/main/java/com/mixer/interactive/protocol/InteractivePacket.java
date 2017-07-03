package com.mixer.interactive.protocol;

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
        if (!"method".equals(type) && !"reply".equals(type)) {
            throw new IllegalArgumentException("Packet type be either 'method' or 'reply'");
        }
        
        this.id = id;
        this.type = type;
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
}
