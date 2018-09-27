package com.maxtechnologies.cryptomax.exchange.asset;

/**
 * Created by Colman on 15/07/2018.
 */

public interface CoinFiatPairsCallback {
    void onFailure(String reason);

    void onSuccess(CoinFiatPair[] pairs);
}
