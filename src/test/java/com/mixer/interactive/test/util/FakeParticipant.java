package com.mixer.interactive.test.util;

import com.google.common.base.Objects;
import com.mixer.interactive.GameClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * Fake participant for use in local testing
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class FakeParticipant {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Random number generator for use in {@link FakeParticipant#generate()}
     */
    private static final Random random = new Random();

    /**
     * User id for the fake participant
     */
    private final int ID;

    /**
     * Username for the fake participant
     */
    private final String Username;

    /**
     * XP level for the fake participant
     */
    private final int XP;

    /**
     * Initializes a new <code>FakeParticipant</code>.
     *
     * @param   id
     *          User id for the fake participant
     * @param   username
     *          Username for the fake participant
     * @param   xp
     *          XP level for the fake participant
     * @since   1.0.0
     */
    private FakeParticipant(int id, String username, int xp) {
        this.ID = id;
        this.Username = username;
        this.XP = xp;
    }

    /**
     * Returns a Json string representing this fake participant.
     *
     * @return  Json string representing this fake participant
     */
    String toJson() {
        LOG.debug(GameClient.GSON.toJson(this));
        return GameClient.GSON.toJson(this);
    }

    /**
     * Generates a new random fake participant.
     *
     * @return  A new fake participant
     * @since   1.0.0
     */
    static FakeParticipant generate() {
        int id = random.nextInt(100000);
        return new FakeParticipant(id, "Participant" + id, random.nextInt(100));
    }

    /**
     * Returns a <code>String</code> representation of this <code>FakeParticipant</code>.
     *
     * @return  <code>String</code> representation of this <code>FakeParticipant</code>
     * @since   1.0.0
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("ID", ID)
                .add("Username", Username)
                .add("XP", XP)
                .toString();
    }
}