package com.mixer.interactive.resources.transaction;

import com.mixer.interactive.GameClient;

import java.util.concurrent.CompletableFuture;

import static com.mixer.interactive.GameClient.TRANSACTION_SERVICE_PROVIDER;

/**
 * A <code>InteractiveTransaction</code> represents a Spark transaction on the Interactive service.
 *
 * @author      Microsoft Corporation
 *
 * @since       1.0.0
 */
public class InteractiveTransaction {

    /**
     * Identifier for <code>InteractiveTransaction</code>
     */
    private final String transactionID;

    /**
     * Initializes a new <code>InteractiveTransaction</code>.
     *
     * @param   transactionID
     *          Identifier for <code>InteractiveTransaction</code>
     *
     * @since   1.0.0
     */
    public InteractiveTransaction(String transactionID) {
        this.transactionID = transactionID;
    }

    /**
     * Returns the identifier for the <code>InteractiveTransaction</code>.
     *
     * @return  Identifier for the <code>InteractiveTransaction</code>
     *
     * @since   1.0.0
     */
    public String getTransactionID() {
        return transactionID;
    }

    /**
     * Asynchronously attempts to complete the Spark transaction on the Interactive service.
     *
     * @param   gameClient
     *          The <code>GameClient</code> to use for the capture operation
     *
     * @return  A <code>ListenableFuture</code> that when complete returns <code>true</code> if the transaction completed
     *          successfully, <code>false</code> otherwise
     *
     * @since   1.0.0
     */
    public CompletableFuture<Boolean> capture(GameClient gameClient) {
        return gameClient != null
                ? gameClient.using(TRANSACTION_SERVICE_PROVIDER).capture(transactionID)
                : CompletableFuture.completedFuture(false);
    }
}
