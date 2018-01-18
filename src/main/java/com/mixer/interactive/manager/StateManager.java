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
 * TODO Finish Javadoc.
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
     * TODO Finish Javadoc
     */
    private GameClient gameClient;

    /**
     * TODO Finish Javadoc
     */
    private long timeAdjustment;

    /**
     * TODO Finish Javadoc
     *
     * @param   gameClient
     *          TODO
     *
     * @since   2.1.0
     */
    public StateManager(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    /**
     * TODO Finish Javadoc
     *
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
        LOG.debug("event");
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
     * TODO Finish Javadoc
     *
     * @return  TODO
     *
     * @since   2.1.0
     */
    public long getTimeAdjustment() {
        return timeAdjustment;
    }

    /**
     * TODO Finish Javadoc
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
            LOG.debug(timeAdjustment);
        }
    }
}
