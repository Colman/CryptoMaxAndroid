package com.maxtechnologies.cryptomax.ui.chart.utils;

import com.github.mikephil.charting.data.CandleEntry;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Colman on 29/06/2018.
 */

public class IndicatorUtils {

    public static float[] getSMA(ArrayList<CandleEntry> entries, int period) {
        if(period > entries.size()) {
            return null;
        }

        float[] sma = new float[entries.size()];
        for(int i = 0; i < sma.length; i++) {
            sma[i] = 0;
        }
        for(int i = period - 1; i < entries.size(); i++) {
            float sum = 0;
            for(int j = period - 1; j >= 0; j--) {
                sum += entries.get(i - j).getClose();
            }
            sma[i] = sum / period;
        }

        return sma;
    }



    public static float[] getEMA(ArrayList<CandleEntry> entries, int period) {
        if(period > entries.size()) {
            return null;
        }

        float[] sma = getSMA(entries, period);
        float mult = 2f / (period + 1f);
        float[] ema = new float[entries.size()];

        for(int i = period - 1; i < entries.size(); i++) {
            if(i == period) {
                ema[i] = sma[i];
            }

            else {
                ema[i] = (entries.get(i).getClose() - ema[i - 1]) * mult + ema[i - 1];
            }
        }

        return ema;
    }


    public static float[][] getBollinger(ArrayList<CandleEntry> entries, int period, float numSTD) {
        float[][] bol = new float[3][entries.size()];
        float[] sma = getSMA(entries, period);
        float[] std = getSTD(entries, period);
        for(int i = 0; i < bol[0].length; i++) {
            bol[0][i] = sma[i] - numSTD * std[i];
            bol[1][i] = sma[i];
            bol[2][i] = sma[i] + numSTD * std[i];
        }

        return bol;
    }



    public static float[] getSAR(ArrayList<CandleEntry> entries, float step, float maxStep) {
        if(entries.size() < 3) {
            return null;
        }
        float[] sar = new float[entries.size()];
        sar[0] = 0;
        sar[1] = 0;

        boolean downTrend = false;
        float pep = 0;
        float ep = 0;
        float paf = step;
        float af = step;
        for(int i = 2; i < sar.length; i++) {
            if(downTrend) {
                float point = entries.get(i).getLow();
                if(point < ep) {
                    ep = point;
                    af += step;
                    if(af > maxStep) {
                        af = maxStep;
                    }
                }

                sar[i] = sar[i - 1] - paf * (sar[i - 1] - pep);

                float high1 = entries.get(i - 2).getHigh();
                float high2 = entries.get(i - 1).getHigh();
                if(sar[i] < high1 || sar[i] < high2) {
                    if(high1 > high2) {
                        sar[i] = high1;
                    }
                    else {
                        sar[i] = high2;
                    }
                }

                if(sar[i] <= entries.get(i).getHigh()) {
                    downTrend = false;
                    float low1 = entries.get(i - 2).getLow();
                    float low2 = entries.get(i - 1).getLow();
                    if(low1 < low2) {
                        sar[i] = low1;
                        ep = low1;
                    }
                    else {
                        sar[i] = low2;
                        ep = low2;
                    }
                    af = step;
                }

                pep = ep;
                paf = af;
            }

            else {
                float point = entries.get(i).getHigh();
                if(point > ep) {
                    ep = point;
                    af += step;
                    if(af > maxStep) {
                        af = maxStep;
                    }
                }

                sar[i] = sar[i - 1] + paf * (pep - sar[i - 1]);

                float low1 = entries.get(i - 2).getLow();
                float low2 = entries.get(i - 1).getLow();
                if(sar[i] > low1 || sar[i] > low2) {
                    if(low1 < low2) {
                        sar[i] = low1;
                    }
                    else {
                        sar[i] = low2;
                    }
                }

                if(sar[i] >= entries.get(i).getLow()) {
                    downTrend = true;
                    float high1 = entries.get(i - 2).getHigh();
                    float high2 = entries.get(i - 1).getHigh();
                    if(high1 > high2) {
                        sar[i] = high1;
                        ep = high1;
                    }
                    else {
                        sar[i] = high2;
                        ep = high2;
                    }
                    af = step;
                }

                pep = ep;
                paf = af;
            }
        }

        return sar;
    }



    public static float[] getRSI(ArrayList<CandleEntry> entries, int period) {
        if(period > entries.size()) {
            return null;
        }

        float[] rsi = new float[entries.size()];

        float[] gains = new float[rsi.length];
        float[] losses = new float[rsi.length];
        for(int i = 0; i < rsi.length; i++) {
            float open = entries.get(i).getOpen();
            float close = entries.get(i).getClose();
            if(open >= close) {
                losses[i] = open - close;
            }
            else {
                gains[i] = close - open;
            }
        }

        float[] avgGains = new float[rsi.length];
        float[] avgLosses = new float[rsi.length];
        float tGain = 0;
        float tLoss = 0;
        for(int i = 0; i < period; i++) {
            tGain += gains[i];
            tLoss += losses[i];
        }
        avgGains[period - 1] = tGain / period;
        avgLosses[period - 1] = tLoss / period;

        for(int i = period; i < rsi.length; i++) {
            avgGains[i] = (avgGains[i - 1] * (period - 1) + gains[i]) / period;
            avgLosses[i] = (avgLosses[i - 1] * (period - 1) + losses[i]) / period;
        }

        for(int i = period - 1; i < rsi.length; i++) {
            rsi[i] = 100f - (100f / (avgGains[i] / avgLosses[i] + 1));
        }

        return rsi;
    }



    public static float[] getSTD(ArrayList<CandleEntry> entries, int period) {
        if(period >= entries.size()) {
            return null;
        }

        float[] sma = getSMA(entries, period);
        float[] std = new float[entries.size()];
        for(int i = period - 1; i < entries.size(); i++) {
            float totalSq = 0;
            for(int j = period - 1; j >= 0; j--) {
                totalSq += Math.pow(entries.get(i - j).getClose() - sma[i], 2);
            }
            std[i] = BigDecimal.valueOf(Math.sqrt(totalSq / period)).floatValue();
        }

        return std;
    }
}
