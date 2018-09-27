package com.maxtechnologies.cryptomax.Exchanges;

import android.app.Activity;

import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Objects.Coin;
import com.maxtechnologies.cryptomax.Objects.FiatCurrency;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Callbacks.NetworkCallbacks;
import com.maxtechnologies.cryptomax.Other.StaticVariables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Colman on 01/01/2018.
 */

public abstract class Exchange implements Serializable {
    public static String[] chartWidths = new String[] {
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
    public static String[][] coinNames;
    public static String[][] translations = new String[][] {
            new String[] {"DASH", "DSH"},
            new String[] {"DATA", "DAT"},
            new String[] {"QASH", "QSH"},
            new String[] {"YOYOW", "YYW"},
            new String[] {"MANA", "MNA"},
            new String[] {"MIOTA", "IOT"},
            new String[] {"QTUM", "QTM"},
            new String[] {"SNGLS", "SNG"},
            new String[] {"SPANK", "SPK"},
            new String[] {"AION", "AIO"},
            new String[] {"SPANK", "SPK"},
            new String[] {"IOST", "IOS"},
            new String[] {"DADI", "DAD"},
            new String[] {"STORJ", "STJ"},
            new String[] {"MITH", "MIT"}
    };
    public static String[] isMined = new String[] {
            "BTC",
            "ETH",
            "BCH",
            "LTC",
            "XMR",
            "DASH",
            "ETC",
            "XVG",
            "ZEC",
            "ETP",
            "BTG",
            "SPK",
            "BCI"
    };
    public static String[] isNotMined = new String[] {
            "XRP",
            "EOS",
            "XLM",
            "NEO",
            "MIOTA",
            "TRX",
            "VEN",
            "QTUM",
            "OMG",
            "MKR",
            "SNT",
            "ZRX",
            "REP",
            "LRC",
            "BAT",
            "QASH",
            "GNT",
            "ELF",
            "FUN",
            "KNC",
            "REQ",
            "WAX",
            "POA",
            "AGI",
            "MANA",
            "RLC",
            "TNB",
            "RDN",
            "ANT",
            "SAN",
            "RCN",
            "VEE",
            "EDO",
            "DATA",
            "SNGLS",
            "UTK",
            "YOYOW",
            "CFI",
            "DAI",
            "MTN",
            "LYM",
            "AID",
            "AVT",
            "DTH",
            "ODE",
            "BFT",
            "AION",
            "IOS",
            "DAD",
            "STJ",
            "MITH",
            "DTH"
    };
    public static FiatCurrency[] fiats = new FiatCurrency[] {
            new FiatCurrency("AUD", "Australian Dollar", 0),
            new FiatCurrency("BGN", "Bulgarian Lev", 0),
            new FiatCurrency("BRL", "Brazilian Real", 0),
            new FiatCurrency("CAD", "Canadian Dollar", 0),
            new FiatCurrency("CHF", "Swiss Franc", 0),
            new FiatCurrency("CNY", "Chinese Yuan", 0),
            new FiatCurrency("CZK", "Czech Koruna", 0),
            new FiatCurrency("DKK", "Danish Krone", 0),
            new FiatCurrency("EUR", "Euro", 0),
            new FiatCurrency("GBP", "British Pound", 0),
            new FiatCurrency("HKD", "Hong Kong Dollar", 0),
            new FiatCurrency("HRK", "Croatian Kuna", 0),
            new FiatCurrency("HUF", "Hungarian Forint", 0),
            new FiatCurrency("IDR", "Indonesian Rupiah", 0),
            new FiatCurrency("ILS", "Israeli New Shekel", 0),
            new FiatCurrency("INR", "Indian Rupee", 0),
            new FiatCurrency("JPY", "Japanese Yen", 0),
            new FiatCurrency("KRW", "South Korean Won", 0),
            new FiatCurrency("MXN", "Mexican Peso", 0),
            new FiatCurrency("MYR", "Malaysian Ringgit", 0),
            new FiatCurrency("NOK", "Norwegian Krone", 0),
            new FiatCurrency("NZD", "New Zealand Dollar", 0),
            new FiatCurrency("PHP", "Philippine Piso", 0),
            new FiatCurrency("PLN", "Polish Zloty", 0),
            new FiatCurrency("RON", "Romanian Leu", 0),
            new FiatCurrency("RUB", "Russian Ruble", 0),
            new FiatCurrency("SEK", "Swedish Krona", 0),
            new FiatCurrency("SGD", "Singapore Dollar", 0),
            new FiatCurrency("THB", "Thai Baht", 0),
            new FiatCurrency("TRY", "Turkish Lira", 0),
            new FiatCurrency("USD", "US Dollar", 0),
            new FiatCurrency("ZAR", "South African Rand", 0)
    };
    public static Activity activity;
    public static NetworkCallbacks callback;
    public String name;
    public ArrayList<Coin> coins;
    public String[] widths;


    public static String translateToSymbol(String initialSymbol) {
        initialSymbol = initialSymbol.toUpperCase();
        for(int i = 0; i < translations.length; i++) {
            for(int j = 1; j < translations[i].length; j++) {
                if(initialSymbol.equals(translations[i][j])) {
                    return translations[i][0];
                }
            }
        }
        return initialSymbol;
    }



    public static String translateToName(String symbol) {
        symbol = symbol.toUpperCase();
        String transSymbol = symbol;
        boolean found = false;
        for(int i = 0; i < translations.length; i++) {
            for(int j = 1; j < translations[i].length; j++) {
                if(symbol.equals(translations[i][j])) {
                    transSymbol = translations[i][0];
                    found = true;
                    break;
                }
            }
            if(found) {
                break;
            }
        }

        for(int i = 0; i < coinNames.length; i++) {
            if(coinNames[i][0].equals(transSymbol)) {
                return coinNames[i][1];
            }
        }

        return transSymbol;
    }



    public static String translateToExchangeSymbol(String initialSymbol) {
        String mcSymbol = translateToSymbol(initialSymbol);

        ArrayList<Coin> coinList = StaticVariables.exchanges[Settings.exchangeIndex].coins;
        for(int i = 0; i < coinList.size(); i++) {
            if(mcSymbol.equals(coinList.get(i).symbol)) {
                return coinList.get(i).exchangeSymbol;
            }
        }

        return "";
    }



    protected Coin makeCoin(String exchangeSymbol) {
        String symbol = translateToSymbol(exchangeSymbol);
        float marketCap = -1;
        float supply = -1;
        for(int i = 0; i < coinNames.length; i++) {
            if(symbol.equals(coinNames[i][0])) {
                marketCap = Float.valueOf(coinNames[i][2]);
                supply = Float.valueOf(coinNames[i][3]);
            }
        }

        return new Coin(symbol, exchangeSymbol, translateToName(exchangeSymbol), 0, marketCap, supply);
    }



    protected void sortCoins() {
        Collections.sort(coins, new Comparator<Coin>() {
            @Override
            public int compare(Coin c1, Coin c2) {
                if(c1.marketCap >= c2.marketCap) {
                    return -1;
                }
                return 1;
            }
        });
    }



    public static int widthToSec(String width) {
        width = width.toLowerCase();
        String lastChar = width.substring(width.length() - 1, width.length());
        int widthNum = Integer.valueOf(width.substring(0, width.length() - 1));

        if(lastChar.equals("m")) {
            return 60 * widthNum;
        }
        if(lastChar.equals("h")) {
            return 3600 * widthNum;
        }
        if(lastChar.equals("d")) {
            return 86400 * widthNum;
        }
        return 604800 * widthNum;
    }



    public static String changeString(float price, float change) {
        change = change * fiats[Settings.currency].perUS;
        String changeStr;

        if(price == 0) {
            changeStr = String.format("%,.2f", change);
        }

        else {
            if (price < 1) {
                changeStr = String.format("%,.5f", change);
            }

            else if (price < 10) {
                changeStr = String.format("%,.4f", change);
            }

            else if (price < 100) {
                changeStr = String.format("%,.3f", change);
            }

            else {
                changeStr = String.format("%,.2f", change);
            }
        }

        return changeStr + " " + fiats[Settings.currency].symbol;
    }



    public static String fiatString(float price, boolean toCent, boolean withSymbol, boolean commas) {
        price = price * fiats[Settings.currency].perUS;
        String priceStr;

        String formatStr;
        if(commas) {
            formatStr = "%,.";
        }

        else {
            formatStr = "%.";
        }

        if(toCent) {
            priceStr = String.format(formatStr + "2f", price);
        }

        else {
            if (price < 1) {
                priceStr = String.format(formatStr + "5f", price);
            }

            else if (price < 10) {
                priceStr = String.format(formatStr + "4f", price);
            }

            else if (price < 100) {
                priceStr = String.format(formatStr + "3f", price);
            }

            else {
                priceStr = String.format(formatStr + "2f", price);
            }
        }

        if(withSymbol) {
            return priceStr + " " + fiats[Settings.currency].symbol;
        }

        else {
            return priceStr;
        }
    }



    public static String coinString(float amount, int index, boolean showSymbol, boolean commas) {
        float price = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).price;

        float penny = 0.01f / price;
        int numSig = 0;
        String pennyStr = String.format("%.25f", penny);
        int decIndex = pennyStr.indexOf(".");
        for(int i = decIndex + 1; i < pennyStr.length(); i++) {
            if(!pennyStr.substring(i, i + 1).equals("0")) {
                numSig = i - decIndex;
                break;
            }
        }


        String formatStr;
        if(commas) {
            formatStr = "%,.";
        }

        else {
            formatStr = "%.";
        }


        if(showSymbol){
            String symbolStr = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).symbol.toUpperCase();
            return String.format(formatStr + String.valueOf(numSig) + "f", amount) + " " + symbolStr;
        }

        else {
            return String.format(formatStr + String.valueOf(numSig) + "f", amount);
        }
    }



    public static String findMined(String symbol) {
        for(int i = 0; i < isMined.length; i++) {
            if(isMined[i].equals(symbol.toUpperCase())) {
                return "Yes";
            }
        }

        for(int i = 0; i < isNotMined.length; i++) {
            if(isNotMined[i].equals(symbol.toUpperCase())) {
                return "No";
            }
        }

        return "N/A";
    }



    public static int findIndex(String exchangeSymbol) {
        ArrayList<Coin> coins = StaticVariables.exchanges[Settings.exchangeIndex].coins;
        for(int j = 0; j < coins.size(); j++) {
            if(coins.get(j).exchangeSymbol.equals(exchangeSymbol)) {
                return j;
            }
        }
        return -1;
    }



    public static ArrayList<Candle> convertCandles(ArrayList<Candle> entries, int newWidth) {
        if(entries.size() == 1) {
            return entries;
        }

        //Get the old and new candle widths in seconds
        long oldSec = entries.get(1).time - entries.get(0).time;
        int newSec = widthToSec(chartWidths[newWidth]);

        //If the conversion is not possible, return null
        if(newSec % oldSec != 0) {
            return null;
        }

        //Find the first candle whose time lands on a factor of the new width
        int startIndex = 0;
        if(oldSec <= 3600) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long currTime = calendar.getTime().getTime() / 1000;
            startIndex = -1;
            for (int i = 0; i < entries.size(); i++) {
                if (Math.abs(entries.get(i).time - currTime) % newSec == 0) {
                    startIndex = i;
                    break;
                }
            }
            if (startIndex == -1) {
                return null;
            }
        }

        ArrayList<Candle> newEntries = new ArrayList<>();
        int numSkip = (int) (newSec / oldSec);
        int end;
        for(int i = startIndex; i < entries.size(); i += numSkip) {
            long time = entries.get(i).time;
            float low = entries.get(i).low;
            float high = entries.get(i).high;
            end = i + numSkip;
            if(end > entries.size()) {
                end = entries.size();
            }
            for(int j = i; j < end; j++) {
                float newLow = entries.get(j).low;
                float newHigh = entries.get(j).high;
                if(newLow < low) {
                    low = newLow;
                }
                if(newHigh > high) {
                    high = newHigh;
                }
            }
            float open = entries.get(i).open;
            float close = entries.get(end - 1).close;
            newEntries.add(new Candle(time, open, close, high, low));
        }

        return newEntries;
    }



    public static ArrayList<Float> convertVolumes(ArrayList<Float> entries, String oldWidth, int newWidth) {
        int oldSec = widthToSec(oldWidth);
        int newSec = widthToSec(chartWidths[newWidth]);

        if(newSec % oldSec != 0) {
            return null;
        }

        if(newSec == oldSec) {
            return entries;
        }

        ArrayList<Float> newEntries = new ArrayList<>();
        int numSkip = newSec / oldSec;
        for(int i = 0; i < entries.size(); i += numSkip) {
            float totalVol = 0;
            for(int j = i; j < i + numSkip; j++) {
                totalVol += entries.get(j);
            }
            newEntries.add(totalVol);
        }

        return newEntries;
    }



    public abstract void findCoins();

    public abstract void getCandles(int index, int widthIndex, int limit, long startTime);

    public abstract void subTickers(ArrayList<Integer> indexes);

    public abstract void subBook(int index, int max);

    public abstract void unSubBook();

    public abstract void close();
}
