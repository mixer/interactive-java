package com.mixer.interactive.util.compression;

import com.google.common.collect.ImmutableMap;
import com.mixer.interactive.resources.core.CompressionScheme;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Utility class to allow for generic compression/decompression of messages, depending on the client's current
 * <code>CompressionScheme</code>.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class CompressionUtil {

    /**
     * <code>Map</code> of <code>CompressionScheme</code> and their associated <code>ICoder</code>'s
     */
    private static final Map<CompressionScheme, ICoder> coderMap = ImmutableMap.<CompressionScheme, ICoder>builder()
            .build();

    /**
     * Private constructor to prevent instantiation of an utility class.
     *
     * @since   1.0.0
     */
    private CompressionUtil() {
        // NO-OP
    }

    /**
     * Encodes a message using the associated <code>ICoder</code> for the provided <code>CompressionScheme</code>.
     *
     * @param   compressionScheme
     *          The <code>CompressionScheme</code> to use to encode the message
     * @param   message
     *          The message to be encoded
     *
     * @return  Encoded message as an array of bytes
     *
     * @throws  IOException
     *          If there is problem encoding the message
     *
     * @since   1.0.0
     */
    public static byte[] encode(CompressionScheme compressionScheme, String message) throws IOException {
        if (!coderMap.containsKey(compressionScheme)) {
            throw new NoSuchElementException("No coder has been specified for the specified compression scheme - " + compressionScheme);
        }
        return coderMap.get(compressionScheme).encode(message);
    }

    /**
     * Decodes a message using the associated <code>ICoder</code> for the provided <code>CompressionScheme</code>.
     *
     * @param   compressionScheme
     *          The <code>CompressionScheme</code> to use to decode the message
     * @param   message
     *          The encoded message
     *
     * @return  The decoded message
     *
     * @throws  IOException
     *          If there is problem decoding the message
     *
     * @since   1.0.0
     */
    public static String decode(CompressionScheme compressionScheme, byte[] message) throws IOException {
        if (!coderMap.containsKey(compressionScheme)) {
            throw new NoSuchElementException("No coder has been specified for the specified compression scheme - " + compressionScheme);
        }
        return coderMap.get(compressionScheme).decode(message);
    }
}
