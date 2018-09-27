package com.maxtechnologies.cryptomax.exchange;

/**
 * Created by Colman on 03/07/2018.
 */

public interface PriceListener {
    void onFailure(String reason);

    void onPriceChanged(int index);
}
