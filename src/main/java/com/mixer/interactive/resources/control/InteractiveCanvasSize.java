package com.mixer.interactive.resources.control;

/**
 * Enum containing all the available control canvas sizes offered by the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveControlPosition
 *
 * @since       1.0.0
 */
public enum InteractiveCanvasSize {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    /**
     * Array of all <code>InteractiveCanvasSize</code> enum values
     */
    private static final InteractiveCanvasSize[] CANVAS_SIZES = InteractiveCanvasSize.values();

    /**
     * Interactive control area canvas size
     */
    private final String canvasSize;

    /**
     * Initializes a new <code>InteractiveCanvasSize</code>.
     *
     * @param   canvasSize
     *          Interactive controls canvas size
     *
     * @since   1.0.0
     */
    InteractiveCanvasSize(String canvasSize) {
        this.canvasSize = canvasSize;
    }

    /**
     * Returns the associated <code>InteractiveCanvasSize</code> object for the provided canvas size (if there is one).
     *
     * @param   layoutSize
     *          Interactive controls canvas size
     *
     * @return  The matching <code>InteractiveCanvasSize</code> for the provided canvas size. If a match is not found then
     *          <code>null</code> is returned.
     *
     * @since   1.0.0
     */
    public static InteractiveCanvasSize from(String layoutSize) {
        for (InteractiveCanvasSize canvasSize : CANVAS_SIZES) {
            if (canvasSize.canvasSize.equals(layoutSize)) {
                return canvasSize;
            }
        }

        return null;
    }

    /**
     * Returns a <code>String</code> representation of this <code>InteractiveCanvasSize</code>.
     *
     * @return  <code>String</code> representation of this <code>InteractiveCanvasSize</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return canvasSize;
    }
}
