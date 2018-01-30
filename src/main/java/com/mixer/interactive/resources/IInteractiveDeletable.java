package com.mixer.interactive.resources;

import com.google.common.util.concurrent.ListenableFuture;
import com.mixer.interactive.GameClient;

/**
 * The interface <code>IInteractiveCreatable</code> defines methods for deleting a resource on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public interface IInteractiveDeletable {

    /**
     * Asynchronously deletes <code>this</code> from the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          delete request completes with no errors
     *
     * @since   2.0.0
     */
    ListenableFuture<Boolean> delete(GameClient gameClient);
}
