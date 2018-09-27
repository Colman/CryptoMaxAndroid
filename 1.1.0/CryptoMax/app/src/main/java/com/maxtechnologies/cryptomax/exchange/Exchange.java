package com.maxtechnologies.cryptomax.exchange;

import com.maxtechnologies.cryptomax.exchange.asset.AssetPair;
import com.maxtechnologies.cryptomax.exchange.asset.CoinFiatPair;
import com.maxtechnologies.cryptomax.exchange.book.BookListener;
import com.maxtechnologies.cryptomax.exchange.candle.CandlesCallback;
import com.maxtechnologies.cryptomax.misc.BasicCallback;

import java.io.Serializable;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 01/01/2018.
 */

public abstract class Exchange implements Serializable {
    public final static String[] widths = new String[] {
            "1m",
            "3m",
            "5m",
            "15m",
            "30m",
            "1h",
            "2h",
            "4h",
            "6h",
            "12h",
            "1d",
            "3d",
            "1w"
    };
    private String name;
    private CoinFiatPair[] pairs;


    Exchange(String name, @Nonnull CoinFiatPair[] pairs) {
        this.name = name;
        this.pairs = pairs;
    }



    public String getName() {
        return name;
    }



    public AssetPair getAssetPair(int index) {
        return pairs[index];
    }



    public int getAssetPairsSize() {
        return pairs.length;
    }



    public abstract void getCandles(int index, int widthIndex, long startTime, long endTime, @Nonnull CandlesCallback callback);

    public abstract void subTickers(@Nonnull int[] indexes, @Nonnull PriceListener listener);

    public abstract void unSubTickers(@Nonnull PriceListener listener);

    public abstract void subBooks(@Nonnull int[] indexes, @Nonnull int[] maxes, @Nonnull BookListener listener) throws IllegalArgumentException;

    public abstract void unSubBooks(@Nonnull BookListener listener);

    public abstract void close();
}
