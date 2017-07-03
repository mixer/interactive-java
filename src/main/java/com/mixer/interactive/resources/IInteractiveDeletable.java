package com.mixer.interactive.resources;

import com.google.common.util.concurrent.ListenableFuture;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;

/**
 * The interface <code>IInteractiveCreatable</code> defines methods for deleting a resource on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public interface IInteractiveDeletable {

    /**
     * Deletes <code>this</code> from the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @since   1.0.0
     */
    void delete(GameClient gameClient) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException;

    /**
     * Asynchronously deletes <code>this</code> from the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the delete operation
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          delete operation completes with no errors
     *
     * @since   1.0.0
     */
    ListenableFuture<Boolean> deleteAsync(GameClient gameClient);
}
