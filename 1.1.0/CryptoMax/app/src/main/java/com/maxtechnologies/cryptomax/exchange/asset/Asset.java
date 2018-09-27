package com.maxtechnologies.cryptomax.exchange.asset;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 09/12/2017.
 */

public abstract class Asset implements Serializable {
    private String symbol;
    private String name;
    private int precision;


    public Asset(@Nonnull String symbol, String name, int precision) {
        this.symbol = symbol;
        this.name = name;
        this.precision = precision;
    }



    protected String getSymbol() {
        return symbol;
    }



    public String getName() {
        return name;
    }



    public int getPrecision() {
        return precision;
    }



    public void setPrecision(int precision) {
        this.precision = precision;
    }



    public String assetString(BigDecimal amount, boolean showSymbol, boolean commas) {
        amount = amount.setScale(precision, BigDecimal.ROUND_HALF_EVEN);

        String format = "%";
        if(commas)
            format += ",";
        format += "f";
        String result = String.format(format, amount);

        if(showSymbol) {
            return result + " " + symbol;
        } else {
            return result;
        }
    }
}
