package com.mixer.interactive.resources;

import com.mixer.interactive.GameClient;

import java.util.concurrent.CompletableFuture;

/**
 * The interface <code>IInteractiveCreatable</code> defines methods for creating a resource on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public interface IInteractiveCreatable {

    /**
     * Asynchronously creates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the create operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          create request completes with no errors
     *
     * @since   2.0.0
     */
    CompletableFuture<Boolean> create(GameClient gameClient);
}
