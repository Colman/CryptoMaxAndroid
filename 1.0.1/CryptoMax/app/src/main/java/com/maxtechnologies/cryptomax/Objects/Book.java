package com.maxtechnologies.cryptomax.Objects;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Colman on 11/01/2018.
 */

public class Book {
    public ArrayList<BookLine> asks;
    public ArrayList<BookLine> bids;

    public Book(ArrayList<BookLine> asks, ArrayList<BookLine> bids) {
        this.asks = asks;
        this.bids = bids;
    }


    public void sortAndTrimLists(int max) {
        Collections.sort(asks, new Comparator<BookLine>() {
            public int compare(BookLine line1, BookLine line2) {
                if(line1.price < line2.price) {
                    return -1;
                }
                else if (line1.price > line2.price) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        });

        Collections.sort(bids, new Comparator<BookLine>() {
            public int compare(BookLine line1, BookLine line2) {
                if(line1.price < line2.price) {
                    return 1;
                }
                else if (line1.price > line2.price) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        });

        int askSize = asks.size();
        for(int i = max; i < askSize; i++) {
            asks.remove(max);
        }

        int bidSize = bids.size();
        for(int i = max; i < bidSize; i++) {
            bids.remove(max);
        }

        for(int i = 0; i < asks.size(); i++) {
            if(i == 0) {
                asks.get(0).totalQuantity = asks.get(0).quantity;
            }
            else {
                asks.get(i).totalQuantity = asks.get(i - 1).totalQuantity + asks.get(i).quantity;
            }
        }

        for(int i = 0; i < bids.size(); i++) {
            if(i == 0) {
                bids.get(0).totalQuantity = bids.get(0).quantity;
            }
            else {
                bids.get(i).totalQuantity = bids.get(i - 1).totalQuantity + bids.get(i).quantity;
            }
        }
    }
}
