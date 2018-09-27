package com.maxtechnologies.cryptomax.Objects;

import com.maxtechnologies.cryptomax.Other.OtherTools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Colman on 09/12/2017.
 */

public class Coin implements Serializable {
    public String symbol;
    public String exchangeSymbol;
    public String name;
    public float price;
    public float marketCap;
    public float supply;
    public float volume1d;
    public float change1d;


    public Coin(String symbol, String exchangeSymbol, String name, float price, float marketCap, float supply) {
        this.symbol = symbol;
        this.exchangeSymbol = exchangeSymbol;
        this.name = name;
        this.price = price;
        this.marketCap = marketCap;
        this.supply = supply;
    }



    public String marketCapString() {
        String mCap = String.format("%.1f", marketCap);
        mCap = mCap.substring(0, mCap.length() - 2);
        if(mCap.length() == 4) {
            return mCap.substring(0, 1) + "." + mCap.substring(1, 3) + "K";
        }
        else if(mCap.length() == 5) {
            return mCap.substring(0, 2) + "." + mCap.substring(2, 3) + "K";
        }
        else if(mCap.length() == 6) {
            return mCap.substring(0, 3) + "K";
        }
        else if(mCap.length() == 7) {
            return mCap.substring(0, 1) + "." + mCap.substring(1, 3) + "M";
        }
        else if(mCap.length() == 8) {
            return mCap.substring(0, 2) + "." + mCap.substring(2, 3) + "M";
        }
        else if(mCap.length() == 9) {
            return mCap.substring(0, 3) + "M";
        }
        else if(mCap.length() == 10) {
            return mCap.substring(0, 1) + "." + mCap.substring(1, 3) + "B";
        }
        else if(mCap.length() == 11) {
            return mCap.substring(0, 2) + "." + mCap.substring(2, 3) + "B";
        }
        else if(mCap.length() == 12) {
            return mCap.substring(0, 3) + "B";
        }
        else if(mCap.length() == 13) {
            return mCap.substring(0, 1) + "." + mCap.substring(1, 3) + "T";
        }
        else if(mCap.length() == 14) {
            return mCap.substring(0, 2) + "." + mCap.substring(2, 3) + "T";
        }
        else if(mCap.length() == 15) {
            return mCap.substring(0, 3) + "T";
        }
        return "";
    }



    public static void sortByChange(ArrayList<Coin> coinList, final boolean descending) {
        Collections.sort(coinList, new Comparator<Coin>() {
            @Override
            public int compare(Coin c1, Coin c2) {
                if(descending) {
                    if(c1.change1d >= c2.change1d) {
                        return -1;
                    }

                    return 1;
                }

                else {
                    if(c1.change1d >= c2.change1d) {
                        return 1;
                    }

                    return -1;
                }
            }
        });
    }



    public Coin clone() {
        return (Coin) OtherTools.deepCopy(this);
    }
}
