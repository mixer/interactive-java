package com.mixer.interactive.protocol;

/**
 * Enum containing all the methods supported by the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public enum InteractiveMethod {
    CAPTURE("capture"),
    CREATE_CONTROLS("createControls"),
    CREATE_GROUPS("createGroups"),
    CREATE_SCENES("createScenes"),
    DELETE_CONTROLS("deleteControls"),
    DELETE_GROUP("deleteGroup"),
    DELETE_SCENE("deleteScene"),
    GET_ACTIVE_PARTICIPANTS("getActiveParticipants"),
    GET_ALL_PARTICIPANTS("getAllParticipants"),
    GET_GROUPS("getGroups"),
    GET_MEMORY_STATS("getMemoryStats"),
    GET_SCENES("getScenes"),
    GET_THROTTLE_STATE("getThrottleState"),
    GET_TIME("getTime"),
    GIVE_INPUT("giveInput"),
    HELLO("hello"),
    ISSUE_MEMORY_WARNING("issueMemoryWarning"),
    ON_CONTROL_CREATE("onControlCreate"),
    ON_CONTROL_DELETE("onControlDelete"),
    ON_CONTROL_UPDATE("onControlUpdate"),
    ON_GROUP_CREATE("onGroupCreate"),
    ON_GROUP_DELETE("onGroupDelete"),
    ON_GROUP_UPDATE("onGroupUpdate"),
    ON_PARTICIPANT_JOIN("onParticipantJoin"),
    ON_PARTICIPANT_LEAVE("onParticipantLeave"),
    ON_PARTICIPANT_UPDATE("onParticipantUpdate"),
    ON_READY("onReady"),
    ON_SCENE_CREATE("onSceneCreate"),
    ON_SCENE_DELETE("onSceneDelete"),
    ON_SCENE_UPDATE("onSceneUpdate"),
    READY("ready"),
    SET_BANDWIDTH_THROTTLE("setBandwidthThrottle"),
    SET_COMPRESSION("setCompression"),
    UPDATE_CONTROLS("updateControls"),
    UPDATE_GROUPS("updateGroups"),
    UPDATE_PARTICIPANTS("updateParticipants"),
    UPDATE_SCENES("updateScenes"),
    UNKNOWN(null);

    /**
     * Array of all <code>InteractiveMethod</code> enum values
     */
    private static final InteractiveMethod[] METHODS = InteractiveMethod.values();

    /**
     * Interactive method name
     */
    private final String methodName;

    /**
     * Initializes a new <code>InteractiveMethod</code>.
     *
     * @param   methodName
     *          Interactive method name
     *
     * @since   1.0.0
     */
    InteractiveMethod(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns the associated <code>InteractiveMethod</code> object for the provided method name (if there is one).
     *
     * @param   methodName
     *          Interactive method name
     *
     * @return  The matching <code>InteractiveMethod</code> for the provided scheme name. If a match is not found then
     *          <code>null</code> is returned
     *
     * @since   1.0.0
     */
    public static InteractiveMethod from(String methodName) {
        for (InteractiveMethod method : METHODS) {
            if (method.methodName.equals(methodName)) {
                return method;
            }
        }

        return UNKNOWN;
    }

    /**
     * Returns a <code>String</code> representation of this <code>InteractiveMethod</code>.
     *
     * @return  <code>String</code> representation of this <code>InteractiveMethod</code>
     *
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return methodName;
    }
}
