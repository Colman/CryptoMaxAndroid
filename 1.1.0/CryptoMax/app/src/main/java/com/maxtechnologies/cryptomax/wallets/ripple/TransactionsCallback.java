package com.maxtechnologies.cryptomax.wallets.ripple;


import com.ripple.core.types.known.tx.Transaction;

/**
 * Created by Colman on 11/07/2018.
 */

public interface TransactionsCallback {
    void onFailure(String reason);

    void onSuccess(Transaction[] transactions, int[] pending);
}
