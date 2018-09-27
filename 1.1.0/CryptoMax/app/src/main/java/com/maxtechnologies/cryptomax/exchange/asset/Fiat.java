package com.maxtechnologies.cryptomax.exchange.asset;


import javax.annotation.Nonnull;

/**
 * Created by Colman on 04/07/2018.
 */

public class Fiat extends Asset {

    public Fiat(@Nonnull String symbol, String name) {
        super(symbol, name);
    }
}
