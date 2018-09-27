package com.maxtechnologies.cryptomax.wallets.ripple;

import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;

import java.math.BigDecimal;

/**
 * Created by Colman on 15/05/2018.
 */

public interface TransactionListener {
    enum Error {
        INSUFFICIENT_FUNDS,
        INVALID_DEST,
        NETWORK_ERROR
    }

    void onFailed(Error error, TransactionType type);

    void onPending(Transaction transaction);

    void onValidated(Transaction transaction);
}
