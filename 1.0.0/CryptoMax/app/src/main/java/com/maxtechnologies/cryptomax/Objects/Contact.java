package com.maxtechnologies.cryptomax.Objects;

import android.graphics.Bitmap;

import com.maxtechnologies.cryptomax.Wallets.Wallet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Colman on 10/01/2018.
 */

public class Contact implements Serializable {
    public String symbol;
    public String address;
    public String memo;
    public Date dateAdded;


    public Contact(String symbol, String address, String memo, Date dateAdded) {
        this.symbol = symbol;
        this.address = address;
        this.memo = memo;
        this.dateAdded = dateAdded;
    }
}
