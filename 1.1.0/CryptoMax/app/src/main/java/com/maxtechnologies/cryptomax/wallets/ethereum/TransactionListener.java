package com.maxtechnologies.cryptomax.wallets.ethereum;


import org.ethereum.core.Transaction;

import java.math.BigDecimal;

/**
 * Created by Colman on 11/07/2018.
 */

public interface TransactionListener {
    enum Error {
        INSUFFICIENT_FUNDS,
        NETWORK_ERROR
    }

    void onFailed(Error error, BigDecimal amount);

    void onPending(Transaction transaction);

    void onConfirmed(CompletedTransaction transaction);
}
