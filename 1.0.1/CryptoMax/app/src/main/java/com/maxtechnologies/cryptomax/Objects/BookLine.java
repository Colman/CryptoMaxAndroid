package com.maxtechnologies.cryptomax.Objects;

import java.io.Serializable;

/**
 * Created by Colman on 11/01/2018.
 */

public class BookLine implements Serializable {
    public float price;
    public float quantity;
    public float totalQuantity;


    public BookLine(float price, float quantity) {
        this.price = price;
        this.quantity = quantity;
    }
}
