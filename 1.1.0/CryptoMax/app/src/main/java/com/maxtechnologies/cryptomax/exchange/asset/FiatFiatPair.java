package com.maxtechnologies.cryptomax.exchange.asset;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 23/07/2018.
 */

public class FiatFiatPair extends AssetPair {
    private Fiat fiat;
    private Fiat fiat2;


    public FiatFiatPair(@Nonnull Fiat fiat, @Nonnull Fiat fiat2, @Nonnull String exchangeSymbol) {
        super(fiat, fiat2, exchangeSymbol);
    }
}
