package com.mixer.interactive.manager;

import com.google.common.eventbus.Subscribe;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.event.connection.ConnectionClosedEvent;
import com.mixer.interactive.event.connection.ConnectionEstablishedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages collections of cached objects (scenes/controls/groups) for the game client associated with this.
 *
 * @author      Microsoft Corporation
 *
 * @since       2.1.0
 */
public class StateManager {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * Number of time samples to run to calculate the average delta
     */
    private static final int SAMPLE_RATE = 5;

    /**
     * Scheduled future for syncing time differences between the local system and the connected Interactive host
     */
    private ScheduledFuture timeSyncFuture;

    /**
     * The game client for this StateManager
     */
    private GameClient gameClient;

    /**
     * The most recently calculated amount of time, in milliseconds, to adjust cooldowns by to account for network
     * latency and differences in system time between the host running this game client and the Interactive service
     */
    private long timeAdjustment;

    /**
     * Constructs a new StateManager
     *
     * @param   gameClient
     *          The game client associated with this StateManager
     *
     * @since   2.1.0
     */
    public StateManager(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    /**
     * Upon successful connection to Mixer Interactive, requests initial state information for the Interactive session and
     * sets up a scheduled future to maintain the time difference between the locally running integration and the connected
     * Mixer Interactive host.
     *
     * @param   event
     *          Connection open event
     *
     * @since   2.1.0
     */
    @Subscribe
    public void onConnectionEstablished(ConnectionEstablishedEvent event) {
        if (gameClient.isConnected()) {
            timeSyncFuture = gameClient.getExecutorService().scheduleAtFixedRate(this::calculateTimeAdjustment, 0, 30, TimeUnit.SECONDS);
        }
    }

    /**
     * Shuts down the scheduled future for time differences, and clears all locally cached information.
     *
     * @param   event
     *          Connection close event
     *
     * @since   1.0.0
     */
    @Subscribe
    public void onConnectionClose(ConnectionClosedEvent event) {
        if (timeSyncFuture != null) {
            timeSyncFuture.cancel(true);
            timeSyncFuture = null;
        }
    }

    /**
     * Returns the most recently calculated amount of time, in milliseconds, to adjust cooldowns by to account for
     * network latency and differences in system time between the host running this game client and the Interactive
     * service.
     *
     * @return  The amount of time, in milliseconds, to adjust control cooldowns by
     *
     * @since   2.1.0
     */
    public long getTimeAdjustment() {
        return timeAdjustment;
    }

    /**
     * Calculate an approximate amount of time (in milliseconds) to adjust control cooldowns to accommodate for
     * network latency and clock differences.
     *
     * @since   2.1.0
     */
    private void calculateTimeAdjustment() {
        if (gameClient != null && gameClient.isConnected()) {
            long sumOfDeltas = 0;
            int successfulSamples = 0;
            for (int i = 0; i < SAMPLE_RATE; i++) {
                Instant transmitTime = Instant.now();
                try {
                    sumOfDeltas += gameClient.getTime().thenApply(serverTime -> {
                        Instant receiveTime = Instant.now();
                        long rtt = receiveTime.toEpochMilli() - transmitTime.toEpochMilli();
                        return serverTime - rtt / 2 - transmitTime.toEpochMilli();
                    }).get();
                    successfulSamples++;
                }
                catch (InterruptedException | ExecutionException e) {
                    LOG.error(e);
                }
            }
            timeAdjustment = Math.floorDiv(sumOfDeltas, successfulSamples);
        }
    }
}
