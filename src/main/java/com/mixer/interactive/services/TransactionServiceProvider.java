package com.mixer.interactive.services;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.control.InteractiveControlInput;
import com.mixer.interactive.resources.transaction.InteractiveTransaction;

import static com.mixer.interactive.GameClient.RPC_SERVICE_PROVIDER;

/**
 * Provides all functionality relating to making requests and interpreting replies from the Interactive service
 * regarding Spark transactions.
 *
 * @author      Microsoft Corporation
 *
 * @see         InteractiveTransaction
 *
 * @since       1.0.0
 */
public class TransactionServiceProvider extends AbstractServiceProvider {

    /**
     *
     */
    private static final String PARAM_KEY_TRANSACTION_ID = "transactionID";

    /**
     * Initializes a new <code>TransactionServiceProvider</code>.
     *
     * @param   gameClient
     *          The <code>GameClient</code> that owns this service provider
     *
     * @since   1.0.0
     */
    public TransactionServiceProvider(GameClient gameClient) {
        super(gameClient);
    }

    /**
     * Attempt to complete a spark transaction from the participant that initiated the transaction. The Interactive
     * service makes a best-effort to validate the charge before it’s created, blocking obviously invalid ones outright,
     * but when possible the client SHOULD await a successful reply before effecting any associated action.
     *
     * @param   transactionID
     *          Identifier for a Spark transaction
     *
     * @return  <code>true</code> if the transaction completed successfully, <code>false</code> otherwise
     *
     * @throws InteractiveReplyWithErrorException
     *          If the reply received from the Interactive service contains an <code>InteractiveError</code>
     * @throws InteractiveRequestNoReplyException
     *          If no reply is received from the Interactive service
     *
     * @see     InteractiveControlInput
     * @see     InteractiveTransaction
     *
     * @since   1.0.0
     */
    public Boolean capture(String transactionID) throws InteractiveReplyWithErrorException, InteractiveRequestNoReplyException {
        if (transactionID == null) {
            return false;
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_TRANSACTION_ID, transactionID);
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.CAPTURE, jsonParams);
    }

    /**
     * <p>Attempt to complete a spark transaction from the participant that initiated the transaction. The Interactive
     * service makes a best-effort to validate the charge before it’s created, blocking obviously invalid ones outright,
     * but when possible the client SHOULD await a successful reply before effecting any associated action.</p>
     *
     * <p>The result of the <code>ListenableFuture</code> may include checked exceptions that were thrown in the event
     * that there was a problem with the reply from the Interactive service. Specifically, two types of checked
     * exceptions may be thrown:</p>
     *
     * <ul>
     *  <li>{@link InteractiveRequestNoReplyException} may be thrown if no reply is received from the Interactive
     *  service.</li>
     *  <li>{@link InteractiveReplyWithErrorException} may be thrown if the reply received from the Interactive service
     *  contains an <code>InteractiveError</code>.</li>
     * </ul>
     *
     * <p>Considerations should be made for these possibilities when interpreting the results of the returned
     * <code>ListenableFuture</code>.</p>
     *
     * @param   transactionID
     *          Identifier for a Spark transaction
     *
     * @return  A <code>ListenableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CAPTURE capture} method call completes with no errors
     *
     * @see     InteractiveControlInput
     * @see     InteractiveTransaction
     *
     * @since   1.0.0
     */
    public ListenableFuture<Boolean> captureAsync(String transactionID) {
        if (transactionID == null) {
            return Futures.immediateFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_TRANSACTION_ID, transactionID);
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequestAsync(InteractiveMethod.CAPTURE, jsonParams);
    }
}
