package com.mixer.interactive.resources.core;

/**
 * Enum containing all the compression schemes supported by the Interactive service and the client.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public enum CompressionScheme {
    NONE("text");
    // TODO Add support for Gzip compression
    // TODO Add support for LW4 compression

    /**
     * Array of all <code>CompressionScheme</code> enum values
     */
    private static final CompressionScheme[] SCHEMES = CompressionScheme.values();

    /**
     * Name for the compression scheme
     */
    private final String scheme;

    /**
     * Initializes a new <code>CompressionScheme</code>.
     *
     * @param   scheme
     *          Name for the <code>CompressionScheme</code>
     *
     * @since   1.0.0
     */
    CompressionScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * Returns the associated <code>CompressionScheme</code> object for the provided scheme name (if there is one).
     *
     * @param   scheme
     *          Name for a <code>CompressionScheme</code>
     *
     * @return  The matching <code>CompressionScheme</code> for the provided scheme name. If a match is not found then
     *          {@link #NONE} is returned
     *
     * @since   1.0.0
     */
    public static CompressionScheme from(String scheme) {
        for (CompressionScheme compressionScheme : SCHEMES) {
            if (compressionScheme.scheme.equals(scheme)) {
                return compressionScheme;
            }
        }

        return NONE;
    }

    /**
     * Returns a <code>String</code> representation of this <code>CompressionScheme</code>.
     *
     * @return  <code>String</code> representation of this <code>CompressionScheme</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return scheme;
    }
}
