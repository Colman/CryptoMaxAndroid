package com.maxtechnologies.cryptomax.Controllers;

import android.graphics.RectF;
import android.util.Log;

import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.highlight.StaticHighlight;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Colman on 25/01/2018.
 */

public class ChartController {

    public static void getHighlightPositions(StaticHighlight[] highlights, CombinedData data, float markerWidth, float markerHeight) {
        for(StaticHighlight highlight: highlights) {
            float radius = 1;
            boolean found = false;
            while(!found) {
                for(int j = 0; j < 360; j += 10) {
                    float radians = BigDecimal.valueOf(j * Math.PI / 180).floatValue();
                    float x = highlight.pointerChartX + BigDecimal.valueOf(radius * Math.cos(radians)).floatValue();
                    float y = highlight.pointerChartY + BigDecimal.valueOf(radius * Math.sin(radians)).floatValue();

                    RectF rect = new RectF(x - markerWidth / 2, y - markerHeight / 2, x + markerWidth / 2, y + markerHeight / 2);
                    if(!overlapsData(rect, data)) {
                        highlight.chartX = x - markerWidth / 2;
                        highlight.chartY = y - markerHeight / 2;
                        found = true;
                        break;
                    }
                }
                radius += 0.1f;
            }
        }
    }



    private static boolean overlapsData(RectF rect, CombinedData data) {
        CandleDataSet candles = (CandleDataSet) data.getCandleData().getDataSetByLabel("Candles", false);
        float candleWidth = candles.getShadowWidth();

        int startX = (int) Math.floor(rect.left);
        int endX = (int) Math.ceil(rect.right);
        for(int i = startX; i <= endX; i++) {
            if(i >= candles.getEntryCount()) {
                break;
            }
            CandleEntry candle = candles.getEntryForIndex(i);
            if(candleInRect(candle, rect, candleWidth)) {
                return true;
            }
        }

        LineData lineData = data.getLineData();
        if(lineData != null) {
            for(int i = 0; i < lineData.getDataSetCount(); i++) {
                LineDataSet lineSet = (LineDataSet) lineData.getDataSetByIndex(i);

                for(int j = startX; j <= endX - 1; j++) {
                    if(j + 1 >= lineSet.getEntryCount()) {
                        break;
                    }

                    Entry entry1 = lineSet.getEntryForIndex(j);
                    Entry entry2 = lineSet.getEntryForIndex(j + 1);
                    if(lineRectIntersect(entry1.getX(), entry1.getY(), entry2.getX(), entry2.getY(), rect)) {
                        return true;
                    }
                }
            }
        }

       ScatterData scatterData = data.getScatterData();
        if(scatterData != null) {
            for(int i = 0; i < scatterData.getDataSetCount(); i++) {
                ScatterDataSet scatterSet = (ScatterDataSet) scatterData.getDataSetByIndex(i);

                for(int j = startX; j <= endX; j++) {
                    if(j >= scatterSet.getEntryCount()) {
                        break;
                    }

                    Entry entry = scatterSet.getEntryForIndex(j);
                    if(rect.contains(entry.getX(), entry.getY())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }



    private static boolean candleInRect(CandleEntry candle, RectF rect, float width) {
        if(candle.getClose() > candle.getOpen()) {
            RectF bodyRect = new RectF(candle.getX() - width / 2, candle.getClose(), candle.getX() + width / 2, candle.getOpen());
            if(rectOverlaps(rect, bodyRect)) {
                return true;
            }
        }
        else {
            RectF bodyRect = new RectF(candle.getX() - width / 2, candle.getOpen(), candle.getX() + width / 2, candle.getClose());
            if(rectOverlaps(rect, bodyRect)) {
                return true;
            }
        }

        return rect.contains(candle.getX(), candle.getHigh()) || rect.contains(candle.getX(), candle.getLow());
    }



    private static boolean lineRectIntersect(float x1, float y1, float x2, float y2, RectF rect) {
        boolean left = lineLineIntersect(x1, y1, x2, y2, rect.left, rect.top, rect.left, rect.bottom);
        boolean right = lineLineIntersect(x1, y1, x2, y2, rect.right, rect.top, rect.right, rect.bottom);
        boolean top = lineLineIntersect(x1, y1, x2, y2, rect.left, rect.top, rect.right, rect.top);
        boolean bottom = lineLineIntersect(x1, y1, x2, y2, rect.left, rect.bottom, rect.right, rect.bottom);

        if (left || right || top || bottom) {
            return true;
        }

        return false;
    }



    private static boolean lineLineIntersect(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        float uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
        float uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));

        if (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1) {
            return true;
        }

        return false;
    }


    private static boolean rectOverlaps(RectF r1, RectF r2) {
        if(r1.contains(r2.left, r2.top)) {
            return true;
        }
        if(r1.contains(r2.right, r2.top)) {
            return true;
        }
        if(r1.contains(r2.right, r2.bottom)) {
            return true;
        }
        if(r1.contains(r2.left, r2.bottom)) {
            return true;
        }
        if(r2.contains(r1)) {
            return true;
        }

        return false;
    }



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