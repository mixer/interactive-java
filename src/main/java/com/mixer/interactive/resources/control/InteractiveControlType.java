package com.mixer.interactive.resources.control;

/**
 * Enum containing all the types of controls supported by the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveControl
 *
 * @since       1.0.0
 */
public enum InteractiveControlType {
    BUTTON("button"),
    JOYSTICK("joystick"),
    LABEL("label"),
    TEXTBOX("textbox");

    /**
     * Array of all <code>InteractiveControlType</code> enum values
     */
    private static final InteractiveControlType[] TYPES = InteractiveControlType.values();

    /**
     * Interactive control type name
     */
    private final String typeName;

    /**
     * Initializes a new <code>InteractiveControlType</code>.
     *
     * @param   typeName
     *          Interactive control type name
     *
     * @since   1.0.0
     */
    InteractiveControlType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Returns the associated <code>InteractiveControlType</code> object for the provided control type name
     * (if there is one).
     *
     * @param   typeName
     *          Interactive control type name
     *
     * @return  The matching <code>InteractiveControlType</code> for the provided control type name. If a match is not
     *          found then <code>null</code> is returned
     *
     * @since   1.0.0
     */
    public static InteractiveControlType from(String typeName) {
        for (InteractiveControlType type : TYPES) {
            if (type.typeName.equals(typeName)) {
                return type;
            }
        }

        return null;
    }

    /**
     * Returns a <code>String</code> representation of this <code>InteractiveControlType</code>.
     *
     * @return  <code>String</code> representation of this <code>InteractiveControlType</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return typeName;
    }
}
