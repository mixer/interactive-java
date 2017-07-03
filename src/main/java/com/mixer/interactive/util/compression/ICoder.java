package com.mixer.interactive.util.compression;

import java.io.IOException;

/**
 * The interface <code>ICoder</code> defines methods relating to the compression/decompression of messages exchanged
 * between the client and the Interactive service. Implementing classes must be registered in the
 * <code>CompressionUtil#coderMap</code> in order to be available for use.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public interface ICoder {

    /**
     * Encodes a message.
     *
     * @param   message
     *          The message to be encoded
     *
     * @return  The encoded message as an array of bytes
     *
     * @throws  IOException
     *          If there is problem encoding the message
     *
     * @since   1.0.0
     */
    byte[] encode(String message) throws IOException;

    /**
     * Decodes an encoded message.
     *
     * @param   encodedMessage
     *          The message to be decoded
     *
     * @return  The decoded message
     *
     * @throws  IOException
     *          If there is a problem decoding the message
     *
     * @since   1.0.0
     */
    String decode(byte[] encodedMessage) throws IOException;
}
