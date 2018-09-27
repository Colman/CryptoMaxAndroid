package com.maxtechnologies.cryptomax.exchange;

/**
 * Created by Colman on 15/07/2018.
 */

public interface ExchangeCallback {
    void onFailure(String reason);

    void onSuccess(Exchange exchange);
}
