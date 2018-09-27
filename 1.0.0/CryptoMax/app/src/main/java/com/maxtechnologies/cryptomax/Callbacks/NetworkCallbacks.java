package com.maxtechnologies.cryptomax.Callbacks;


import com.maxtechnologies.cryptomax.Objects.Book;
import com.maxtechnologies.cryptomax.Objects.Candle;


import java.util.ArrayList;


/**
 * Created by Colman on 22/01/2018.
 */


public interface NetworkCallbacks {
    void coinsCallback();

    void candlesCallback(ArrayList<Candle> entries, ArrayList<Float> volume);

    void tickersCallback();

    void bookCallback(Book book);
}
