package com.maxtechnologies.cryptomax.exchange.asset;


import com.maxtechnologies.cryptomax.api.CryptoMaxApi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Colman on 04/07/2018.
 */

public class AssetPair {
    public final static Comparator<AssetPair> dailyChangeComparator = new Comparator<AssetPair>() {
        @Override
        public int compare(AssetPair pair, AssetPair pair2) {
            if (pair.change1d < pair2.change1d)
                return -1;
            else if (pair.change1d == pair2.change1d)
                return 0;
            return 1;
        }
    };
    private Asset asset1;
    private Asset asset2;
    private String exchangeSymbol;
    private BigDecimal price;
    private float change1d;


    public AssetPair(@Nonnull Asset asset1, @Nonnull Asset asset2, @Nonnull String exchangeSymbol) {
        this.asset1 = asset1;
        this.asset2 = asset2;
        this.exchangeSymbol = exchangeSymbol;
    }



    public Asset getAsset1() {
        return asset1;
    }



    public Asset getAsset2() {
        return asset2;
    }



    public String getExchangeSymbol() {
        return exchangeSymbol;
    }



    @Nullable
    public BigDecimal getPrice() {
        return price;
    }



    public void setPrice(BigDecimal price) {
        this.price = price;
    }



    public float getChange1d() {
        return change1d;
    }



    public String getPriceString() {

    }



    public void setChange1d(float change1d) {
        this.change1d = change1d;
    }
}
