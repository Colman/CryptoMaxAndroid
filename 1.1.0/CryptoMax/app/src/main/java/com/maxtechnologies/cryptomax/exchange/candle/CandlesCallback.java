package com.maxtechnologies.cryptomax.exchange.candle;

import com.maxtechnologies.cryptomax.exchange.candle.Candle;

/**
 * Created by Colman on 03/07/2018.
 */

public interface CandlesCallback {
    void onFailure(String reason);

    void onSuccess(Candle[] candles);
}
