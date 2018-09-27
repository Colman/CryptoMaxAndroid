package com.maxtechnologies.cryptomax.exchange.book;

/**
 * Created by Colman on 03/07/2018.
 */

public interface BookListener {
    void onFailure(String reason);

    void onBookChanged(Book book);
}
