package com.maxtechnologies.cryptomax.ui.chart;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by Colman on 25/04/2018.
 */

public class ChartTransaction {
    public int candleIndex;
    public Bitmap otherImage;
    public boolean fromMe;
    public float amount;


    public ChartTransaction(int candleIndex, Bitmap otherImage, boolean fromMe, float amount) {
        this.candleIndex = candleIndex;
        this.otherImage = otherImage;
        this.fromMe = fromMe;
        this.amount = amount;
    }
}