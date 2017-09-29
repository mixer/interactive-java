package com.mixer.interactive.resources.participant;

import com.google.common.hash.Funnel;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.IInteractiveUpdatable;
import com.mixer.interactive.resources.InteractiveResource;
import com.mixer.interactive.resources.group.InteractiveGroup;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * A <code>InteractiveParticipant</code> represents a participant using the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveParticipant extends InteractiveResource<InteractiveParticipant> implements Comparable<InteractiveParticipant> {

    /**
     * Unique string identifier for the user in this session
     */
    private final String sessionID;

    /**
     * The user id (as an unsigned integer) for the participant on Mixer.
     */
    private final Integer userID;;

    /**
     * Participant's username on Mixer
     */
    private String username;

    /**
     * Participant's numeric (unsigned integer) Mixer level
     */
    private Integer level;

    /**
     * UTC unix timestamp (in milliseconds) since the participant last interacted with controls
     */
    private Long lastInputAt;

    /**
     * UTC unix timestamp (in milliseconds) when the participant connected
     */
    private Long connectedAt;

    /**
     * Whether or not a the participants’s input as been disabled
     */
    private boolean disabled;

    /**
     * Identifier for the <code>InteractiveGroup</code> the participant is a member of
     */
    private String groupID;

    /**
     * Initializes a new <code>InteractiveParticipant</code>.
     *
     * @param   sessionID
     *          Unique string identifier for the participant in this session
     * @param   userID
     *          The user id (as an unsigned integer) for the participant on Mixer.
     * @param   username
     *          Participant's username on Mixer
     * @param   level
     *          Participant's numeric (unsigned integer) Mixer level
     * @param   lastInputAt
     *          UTC unix timestamp (in milliseconds) since the participant last interacted with controls
     * @param   connectedAt
     *          UTC unix timestamp (in milliseconds) when the participant connected
     * @param   disabled
     *          Whether or not a the participants’s input as been disabled
     * @param   groupID
     *          Identifier for the <code>InteractiveGroup</code> the participant is a member of
     *
     * @since   1.0.0
     */
    public InteractiveParticipant(String sessionID, Integer userID, String username, Integer level, Long lastInputAt, Long connectedAt, Boolean disabled, String groupID) {
        this.sessionID = sessionID;
        this.userID = userID;
        this.username = username;
        this.level = level;
        this.lastInputAt = lastInputAt;
        this.connectedAt = connectedAt;
        this.disabled = disabled;
        this.groupID = groupID;
    }

    /**
     * Returns the unique string identifier for the participant in this session.
     *
     * @return  Unique string identifier for the participant in this session
     *
     * @since   1.0.0
     */
    public String getSessionID() {
        return sessionID;
    }

    /**
     * Returns the user id (as an unsigned integer) for the participant on Mixer.
     *
     * @return  The user id (as an unsigned integer) for the participant on Mixer
     *
     * @since   1.0.0
     */
    public Integer getUserID() {
        return userID;
    }

    /**
     * Returns the participant's username on Mixer.
     *
     * @return  The participant's username on Mixer
     *
     * @since   1.0.0
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the participant's numeric (unsigned integer) Mixer level.
     *
     * @return  The participant's numeric (unsigned integer) Mixer level
     *
     * @since   1.0.0
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * Returns the UTC unix timestamp (in milliseconds) since the participant last interacted with controls.
     *
     * @return  UTC unix timestamp (in milliseconds) since the participant last interacted with controls
     *
     * @since   1.0.0
     */
    public Long getLastInputAt() {
        return lastInputAt;
    }

    /**
     * Returns the UTC unix timestamp (in milliseconds) when the participant connected.
     *
     * @return  UTC unix timestamp (in milliseconds) when the participant connected
     *
     * @since   1.0.0
     */
    public Long getConnectedAt() {
        return connectedAt;
    }

    /**
     * Returns whether or not a the participants’s input as been disabled
     *
     * @return  <code>true</code> if the participant has had their input disabled, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public Boolean isDisabled() {
        return disabled;
    }

    /**
     * Sets a participant's ability to provide input.
     *
     * @param   disabled
     *          <code>true</code> if the participant should have their input disabled, <code>false</code> otherwise
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveParticipant setDisabled(Boolean disabled) {
        if (disabled != null) {
            this.disabled = disabled;
        }
        else {
            this.disabled = false;
        }
        return this;
    }

    /**
     * Returns the identifier for the <code>InteractiveGroup</code> the participant is a member of.
     *
     * @return  Identifier for the <code>InteractiveGroup</code> the participant is a member of
     *
     * @since   1.0.0
     */
    public String getGroupID() {
        return groupID;
    }

    /**
     * Changes the group that the participant is a member of.
     *
     * @param   groupID
     *          Identifier for the <code>InteractiveGroup</code> the participant will be moved to
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveParticipant changeGroup(String groupID) {
        if (groupID != null && !groupID.isEmpty()) {
            this.groupID = groupID;
        }
        else {
            this.groupID = "default";
        }
        return this;
    }

    /**
     * Changes the group that the participant is a member of.
     *
     * @param   group
     *          <code>InteractiveGroup</code> the participant will be moved to
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveParticipant changeGroup(InteractiveGroup group) {
        return changeGroup(group != null ? group.getGroupID() : null);
    }

    /**
     * Moves the participant to the default <code>InteractiveGroup</code>.
     *
     * @return  <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    public InteractiveParticipant resetGroup() {
        return changeGroup("default");
    }

    /**
     * {@inheritDoc}
     *
     * @see     InteractiveResource#getThis()
     *
     * @since   1.0.0
     */
    @Override
    protected InteractiveParticipant getThis() {
        return this;
    }

    /**
     * Iterates through a <code>Collection</code> of Objects. If <code>this</code> is found to be in the
     * <code>Collection</code> then <code>this</code> has it's values updated.
     *
     * @param   objects
     *          A <code>Collection</code> of Objects
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          provided <code>Collection</code> contains <code>this</code>
     *
     * @see     InteractiveResource#syncIfEqual(Collection)
     *
     * @since   2.0.0
     */
    @Override
    public boolean syncIfEqual(Collection<?> objects) {
        if (objects != null) {
            for (Object o : objects) {
                if (this.equals(o)) {
                    this.meta = ((InteractiveParticipant) o).meta;
                    this.username = ((InteractiveParticipant) o).username;
                    this.level = ((InteractiveParticipant) o).level;
                    this.lastInputAt = ((InteractiveParticipant) o).lastInputAt;
                    this.connectedAt = ((InteractiveParticipant) o).connectedAt;
                    this.disabled = ((InteractiveParticipant) o).disabled;
                    this.groupID = ((InteractiveParticipant) o).groupID;
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Updates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the update operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#UPDATE_PARTICIPANTS update} method call completes with no errors
     *
     * @see     IInteractiveUpdatable#update(GameClient)
     *
     * @since   1.0.0
     */
    @Override
    public CompletableFuture<Boolean> update(GameClient gameClient) {
        if (gameClient == null) {
            return CompletableFuture.completedFuture(false);
        }

        return gameClient.using(GameClient.PARTICIPANT_SERVICE_PROVIDER).update(this)
                .thenCompose(participants -> CompletableFuture.supplyAsync(() -> syncIfEqual(participants)));
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int compareTo(InteractiveParticipant participant) {
        return (participant != null) ? this.sessionID.compareTo(participant.sessionID) : -1;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int hashCode() {
        return Hashing.md5().newHasher()
                .putString(sessionID, StandardCharsets.UTF_8)
                .putInt(userID)
                .putInt(level)
                .putString(groupID, StandardCharsets.UTF_8)
                .putBoolean(disabled)
                .putString(username, StandardCharsets.UTF_8)
                .putLong(lastInputAt)
                .putLong(connectedAt)
                .putObject(meta, (Funnel<JsonElement>) (from, into) -> {
                    if (from != null && !from.isJsonNull()) {
                        into.putString(from.toString(), StandardCharsets.UTF_8);
                    }
                })
                .hash()
                .asInt();
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof InteractiveParticipant && this.compareTo((InteractiveParticipant) o) == 0;
    }
}
