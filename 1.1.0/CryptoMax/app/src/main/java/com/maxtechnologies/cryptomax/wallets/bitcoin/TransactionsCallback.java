package com.maxtechnologies.cryptomax.wallets.bitcoin;


import org.bitcoinj.core.Transaction;

/**
 * Created by Colman on 11/07/2018.
 */

public interface TransactionsCallback {
    void onFailure(String reason);

    void onSuccess(Transaction[] transactions);
}
