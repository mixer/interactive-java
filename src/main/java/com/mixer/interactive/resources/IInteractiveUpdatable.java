package com.mixer.interactive.resources;

import com.mixer.interactive.GameClient;

import java.util.concurrent.CompletableFuture;

/**
 * The interface <code>IInteractiveCreatable</code> defines methods for updating a resource on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public interface IInteractiveUpdatable {

    /**
     * Asynchronously updates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the update operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          update request completes with no errors
     *
     * @since   2.0.0
     */
    CompletableFuture<Boolean> update(GameClient gameClient);
}
