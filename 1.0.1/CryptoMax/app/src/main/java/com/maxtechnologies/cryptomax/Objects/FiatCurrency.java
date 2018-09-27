package com.maxtechnologies.cryptomax.Objects;

/**
 * Created by Colman on 13/01/2018.
 */

public class FiatCurrency {
    public String symbol;
    public String name;
    public float perUS;


    public FiatCurrency(String symbol, String name, float perUS) {
        this.symbol = symbol;
        this.name = name;
        this.perUS = perUS;
    }
}
