package com.maxtechnologies.cryptomax.exchange.candle;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 12/02/2018.
 */

public class Candle {
    private long time;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal volume;


    public Candle(long time, @Nonnull BigDecimal open, @Nonnull BigDecimal close, @Nonnull BigDecimal high, @Nonnull BigDecimal low, @Nonnull BigDecimal volume) {
        this.time = time;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }



    public long getTime() {
        return time;
    }



    public void setTime(long time) {
        this.time = time;
    }



    public BigDecimal getOpen() {
        return open;
    }



    public void setOpen(BigDecimal open) {
        this.open = open;
    }



    public BigDecimal getClose() {
        return close;
    }



    public void setClose(BigDecimal close) {
        this.close = close;
    }



    public BigDecimal getHigh() {
        return high;
    }



    public void setHigh(BigDecimal high) {
        this.high = high;
    }



    public BigDecimal getLow() {
        return low;
    }



    public void setLow(BigDecimal low) {
        this.low = low;
    }



    public BigDecimal getVolume() {
        return volume;
    }



    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }
}
