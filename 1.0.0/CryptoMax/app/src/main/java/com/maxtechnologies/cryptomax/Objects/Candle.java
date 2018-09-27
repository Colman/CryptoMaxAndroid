package com.maxtechnologies.cryptomax.Objects;

/**
 * Created by Colman on 12/02/2018.
 */

public class Candle {
    public long time;
    public float open;
    public float close;
    public float high;
    public float low;


    public Candle(long time, float open, float close, float high, float low) {
        this.time = time;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }
}
