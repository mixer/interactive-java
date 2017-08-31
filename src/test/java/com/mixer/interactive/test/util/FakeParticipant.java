package com.mixer.interactive.test.util;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;
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
    @SerializedName("ID")
    private final int id;

    /**
     * Username for the fake participant
     */
    @SerializedName("Username")
    private final String username;

    /**
     * XP level for the fake participant
     */
    @SerializedName("XP")
    private final int xp;

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
        this.id = id;
        this.username = username;
        this.xp = xp;
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
}