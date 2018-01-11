package com.mixer.interactive.test.util;

/**
 * <code>ChannelResponse</code> is used to parse specific connection related information from queries to an Interactive
 * channel, so as to allow a participant to connect to the channel.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.1.0
 */
public class ChannelResponse {

    /**
     * Socket address to be used to connect a participant to a channel that is Interactive
     */
    public String socketAddress;

    /**
     * Authentication key to be used to connect a participant to a channel that is Interactive
     */
    public String key;

    /**
     * User id for the participant connecting to the a channel that is Interactive
     */
    public String user;
}
