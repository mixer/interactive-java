package com.mixer.interactive.resources;

import com.google.common.util.concurrent.ListenableFuture;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;

/**
 * The interface <code>IInteractiveCreatable</code> defines methods for creating a resource on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public interface IInteractiveCreatable<T> {

    /**
     * Creates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the create operation
     *
     * @return  <code>this</code> for method chaining
     *
     * @throws  InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws  InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @since   1.0.0
     */
    T create(GameClient gameClient) throws InteractiveRequestNoReplyException, InteractiveReplyWithErrorException;

    /**
     * Asynchronously creates <code>this</code> on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the create operation
     *
     * @return  A <code>ListenableFuture</code> that when complete returns <code>this</code> for method chaining
     *
     * @since   1.0.0
     */
    ListenableFuture<T> createAsync(GameClient gameClient);
}
