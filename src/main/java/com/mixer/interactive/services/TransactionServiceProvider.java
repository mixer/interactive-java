package com.mixer.interactive.services;

import com.google.gson.JsonObject;
import com.mixer.interactive.GameClient;
import com.mixer.interactive.exception.InteractiveReplyWithErrorException;
import com.mixer.interactive.exception.InteractiveRequestNoReplyException;
import com.mixer.interactive.protocol.InteractiveMethod;
import com.mixer.interactive.resources.control.InteractiveControlInput;
import com.mixer.interactive.resources.transaction.InteractiveTransaction;

import java.util.concurrent.CompletableFuture;

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
     * <p>Attempt to complete a spark transaction from the participant that initiated the transaction. The Interactive
     * service makes a best-effort to validate the charge before it’s created, blocking obviously invalid ones outright,
     * but when possible the client SHOULD await a successful reply before effecting any associated action.</p>
     *
     * <p>The result of the <code>CompletableFuture</code> may include checked exceptions that were thrown in the event
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
     * <code>CompletableFuture</code>.</p>
     *
     * @param   transactionID
     *          Identifier for a Spark transaction
     *
     * @return  A <code>CompletableFuture</code> that when complete returns {@link Boolean#TRUE true} if the
     *          {@link InteractiveMethod#CAPTURE capture} method call completes with no errors
     *
     * @see     InteractiveControlInput
     * @see     InteractiveTransaction
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> capture(String transactionID) {
        if (transactionID == null) {
            return CompletableFuture.completedFuture(false);
        }

        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(PARAM_KEY_TRANSACTION_ID, transactionID);
        return gameClient.using(RPC_SERVICE_PROVIDER).makeRequest(InteractiveMethod.CAPTURE, jsonParams);
    }
}
