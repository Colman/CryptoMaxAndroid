package com.maxtechnologies.cryptomax.wallets.ethereum;

import org.ethereum.core.Transaction;

/**
 * Created by Colman on 11/07/2018.
 */

public interface TransactionsCallback {
    void onFailure(String reason);

    void onSuccess(CompletedTransaction[] completed, Transaction[] pending);
}
