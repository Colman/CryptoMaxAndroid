package com.maxtechnologies.cryptomax.exchange.candle;

import com.maxtechnologies.cryptomax.exchange.candle.Candle;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 03/07/2018.
 */

public class CandleUtils {

    public static long widthToMS(@Nonnull String width) {
        width = width.toLowerCase();
        String lastChar = width.substring(width.length() - 1, width.length());
        int widthNum = Integer.valueOf(width.substring(0, width.length() - 1));

        if(lastChar.equals("m")) {
            return 60L * widthNum * 1000;
        }
        if(lastChar.equals("h")) {
            return 3600L * widthNum * 1000;
        }
        if(lastChar.equals("d")) {
            return 86400L * widthNum * 1000;
        }
        return 604800L * widthNum * 1000;
    }



    public static Candle[] fillCandles(@Nonnull Candle[] candles, long width, long startTime, long endTime) {
        if (candles.length == 0)
            return candles;

        ArrayList<Candle> result = new ArrayList<>();

        BigDecimal open = candles[0].getOpen();
        for (long i = startTime; i < candles[0].getTime(); i += width) {
            Candle candle = new Candle(i, open, open, open, open, BigDecimal.ZERO);
            result.add(candle);
        }

        for (int i = 0; i < candles.length - 1; i++) {
            result.add(candles[i]);
            BigDecimal close = candles[i].getClose();

            for (long j = candles[i].getTime() + width; j < candles[i + 1].getTime(); j += width) {
                Candle candle = new Candle(j, close, close, close, close, BigDecimal.ZERO);
                result.add(candle);
            }
        }

        Candle lastCandle = candles[candles.length - 1];
        result.add(lastCandle);

        BigDecimal close = lastCandle.getClose();
        for (long i = lastCandle.getTime() + width; i < endTime; i += width) {
            Candle candle = new Candle(i, close, close, close, close, BigDecimal.ZERO);
            result.add(candle);
        }

        Candle[] resultArr = new Candle[result.size()];
        return result.toArray(resultArr);
    }



    public static Candle[] convertCandles(@Nonnull Candle[] entries, long newWidth) throws IllegalArgumentException {
        if(entries.length < 2)
            return entries;

        long oldWidth = entries[1].getTime() - entries[0].getTime();

        if(newWidth % oldWidth != 0)
            throw new IllegalArgumentException("The current width must be a factor of the new width");


        ArrayList<Candle> result = new ArrayList<>();
        int numSkip = (int) (newWidth / oldWidth);
        for(int i = 0; i < entries.length; i += numSkip) {
            long time = entries[i].getTime();
            BigDecimal low = entries[i].getLow();
            BigDecimal high = entries[i].getHigh();
            int end = i + numSkip;
            if(end > entries.length)
                end = entries.length;

            BigDecimal volume = BigDecimal.ZERO;
            for(int j = i; j < end; j++) {
                BigDecimal newLow = entries[j].getLow();
                BigDecimal newHigh = entries[j].getHigh();

                if(newLow.compareTo(low) < 0)
                    low = newLow;

                if(newHigh.compareTo(high) > 0)
                    high = newHigh;

                volume = volume.add(entries[j].getVolume());
            }
            BigDecimal open = entries[i].getOpen();
            BigDecimal close = entries[end - 1].getClose();
            result.add(new Candle(time, open, close, high, low, volume));
        }

        Candle[] resultArr = new Candle[result.size()];
        return result.toArray(resultArr);
    }
}
