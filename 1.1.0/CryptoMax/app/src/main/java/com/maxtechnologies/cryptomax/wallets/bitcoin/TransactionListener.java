package com.maxtechnologies.cryptomax.wallets.bitcoin;

import org.bitcoinj.core.Transaction;

import java.math.BigDecimal;

/**
 * Created by Colman on 11/07/2018.
 */

public interface TransactionListener {
    enum Error {
        INSUFFICIENT_FUNDS,
        INVALID_DEST,
        NETWORK_ERROR
    }

    void onFailed(Error error, BigDecimal value);

    void onPending(Transaction transaction);

    void onConfirmed(Transaction transaction);
}
