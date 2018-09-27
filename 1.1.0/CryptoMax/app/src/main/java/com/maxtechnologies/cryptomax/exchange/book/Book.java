package com.maxtechnologies.cryptomax.exchange.book;

/**
 * Created by Colman on 11/01/2018.
 */

public class Book {
    private Order[] asks;
    private Order[] bids;


    public Book(int size) {
        this.asks = new Order[size];
        this.bids = new Order[size];
    }



    public int getSize() {
        return asks.length;
    }



    public Order getAsk(int index) {
        return asks[index];
    }



    public void setAsk(int index, Order ask) {
        asks[index] = ask;
    }



    public Order getBid(int index) {
        return bids[index];
    }



    public void setBid(int index, Order bid) {
        bids[index] = bid;
    }
}
