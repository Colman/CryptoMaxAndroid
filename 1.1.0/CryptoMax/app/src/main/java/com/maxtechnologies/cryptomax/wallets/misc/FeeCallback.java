package com.maxtechnologies.cryptomax.wallets.misc;

import java.math.BigDecimal;

/**
 * Created by Colman on 02/06/2018.
 */

public interface FeeCallback {
    enum Code {
        NETWORK_ERROR,
        INVALID_RESPONSE,
        INSUFFICIENT_FUNDS
    }

    void onFailure(Code code, String reason);

    void onSuccess(BigDecimal fee);
}
