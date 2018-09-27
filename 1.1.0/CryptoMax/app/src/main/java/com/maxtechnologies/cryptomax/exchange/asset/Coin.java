package com.maxtechnologies.cryptomax.exchange.asset;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Colman on 03/07/2018.
 */

public class Coin extends Asset {
    public final static Comparator<Coin> marketCapComparator = new Comparator<Coin>() {
        @Override
        public int compare(Coin coin, Coin coin2) {
            BigDecimal marketCap = coin.marketCapUsd;
            BigDecimal marketCap2 = coin2.marketCapUsd;

            if (marketCap == null && marketCap2 != null)
                return -1;
            if (marketCap != null && marketCap2 == null)
                return 1;
            if(marketCap == null && marketCap2 == null)
                return 0;

            return marketCap.compareTo(marketCap2);
        }
    };
    private BigDecimal marketCapUsd;
    private BigDecimal supply;
    private int isMined;


    public Coin(@Nonnull String symbol, String name, BigDecimal marketCap, BigDecimal supply, int isMined) {
        super(symbol, name);
        this.marketCapUsd = marketCap;
        this.supply = supply;
        this.isMined = isMined;
    }


    public Coin(@Nonnull String symbol, String name, BigDecimal marketCap, BigDecimal supply) {
        super(symbol, name);
        this.marketCapUsd = marketCap;
        this.supply = supply;
        this.isMined = -1;
    }


    @Nullable
    public BigDecimal getMarketCapUsd() {
        return marketCapUsd;
    }



    @Nullable
    public BigDecimal getSupply() {
        return supply;
    }



    public int getIsMined() {
        return isMined;
    }



    @Nullable
    public String marketCapUsdString() {
        BigDecimal marketCap = this.marketCapUsd;
        if (marketCap == null)
            return null;

        if (marketCap.compareTo(new BigDecimal(10)) < 0) {
            marketCap = marketCap.setScale(-2, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.2f", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(100)) < 0) {
            marketCap = marketCap.setScale(-1, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.1f", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(1_000)) < 0) {
            marketCap = marketCap.setScale(0, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%d", marketCap.intValue());
        }

        else if (marketCap.compareTo(new BigDecimal(10_000)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000), BigDecimal.ROUND_DOWN);
            marketCap = marketCap.setScale(-2, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.2f K", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(100_000)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000), BigDecimal.ROUND_DOWN);
            marketCap = marketCap.setScale(-1, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.1f K", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(1_000_000)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000), BigDecimal.ROUND_DOWN);
            marketCap = marketCap.setScale(0, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%d K", marketCap.intValue());
        }

        else if (marketCap.compareTo(new BigDecimal(10_000_000)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(-2, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.2f M", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(100_000_000)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(-1, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.1f M", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(1_000_000_000)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(0, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%d M", marketCap.intValue());
        }

        else if (marketCap.compareTo(new BigDecimal(10_000_000_000L)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000_000), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(-2, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.2f B", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(100_000_000_000L)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000_000), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(-1, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.1f B", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(1_000_000_000_000L)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000_000), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(0, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%d B", marketCap.longValue());
        }

        else if (marketCap.compareTo(new BigDecimal(10_000_000_000_000L)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000_000_000L), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(-2, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.2f T", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(100_000_000_000_000L)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000_000_000L), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(-1, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%.1f T", marketCap);
        }

        else if (marketCap.compareTo(new BigDecimal(1_000_000_000_000_000L)) < 0) {
            marketCap = marketCap.divide(new BigDecimal(1_000_000_000_000L), BigDecimal.ROUND_FLOOR);
            marketCap = marketCap.setScale(0, BigDecimal.ROUND_DOWN);
            return String.format(Locale.US, "%d T", marketCap.longValue());
        }

        else {
            return null;
        }
    }
}
