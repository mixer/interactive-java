package com.mixer.interactive.services;

import com.mixer.interactive.GameClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A <code>AbstractServiceProvider</code> performs some function within the client.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public abstract class AbstractServiceProvider {

    /**
     * Logger
     */
    private static final Logger LOG = LogManager.getLogger();

    /**
     * <code>GameClient</code> that owns this service provider
     */
    protected final GameClient gameClient;

    /**
     * Initializes a new <code>AbstractServiceProvider</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this service provider
     *
     * @since   1.0.0
     */
    public AbstractServiceProvider(GameClient gameClient) {
        if (gameClient == null) {
            LOG.fatal("GameClient cannot be null");
            throw new IllegalArgumentException("GameClient cannot be null");
        }

        this.gameClient = gameClient;
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @since   1.0.0
     */
    @Override
    public boolean equals(Object object) {
        return object != null && this.getClass().equals(object.getClass());
    }
}
