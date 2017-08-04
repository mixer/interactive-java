package com.mixer.interactive.event.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Interactive event posted by the Interactive service when the compression algorithm used to encode messages.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class SetCompressionEvent extends InteractiveCoreEvent {

    /**
     * A <code>Set</code> of <code>Strings</code> representing compression schemes
     */
    private final List<String> scheme = new ArrayList<>();

    /**
     * Initializes a new <code>SetCompressionEvent</code>.
     *
     * @param   scheme
     *          A <code>Collection</code> of <code>Strings</code> representing compression schemes
     *
     * @since   1.0.0
     */
    public SetCompressionEvent(Collection<String> scheme) {
        if (scheme != null) {
            this.scheme.addAll(scheme);
        }
    }

    /**
     * Returns a <code>Set</code> of <code>Strings</code> representing compression schemes.
     *
     * @return  A <code>Set</code> of <code>Strings</code> representing compression schemes
     *
     * @since   1.0.0
     */
    public List<String> getCompressionSchemes() {
        return scheme;
    }
}
