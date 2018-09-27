package com.maxtechnologies.cryptomax.ui.chart.utils;

import android.graphics.RectF;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Colman on 25/01/2018.
 */

public class MarkerUtils {

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
}