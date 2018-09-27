package com.maxtechnologies.cryptomax.exchange.book;


import java.math.BigDecimal;
import java.util.Comparator;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 11/01/2018.
 */

public class Order {
    private BigDecimal price;
    private BigDecimal quantity;


    public Order(@Nonnull BigDecimal price, @Nonnull BigDecimal quantity) {
        this.price = price;
        this.quantity = quantity;
    }



    public BigDecimal getPrice() {
        return price;
    }



    public BigDecimal getQuantity() {
        return quantity;
    }



    public void setQuantity(@Nonnull BigDecimal quantity) {
        this.quantity = quantity;
    }
}
