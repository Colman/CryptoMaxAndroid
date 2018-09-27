package com.maxtechnologies.cryptomax.Exchanges;

import android.util.Log;

import com.maxtechnologies.cryptomax.Controllers.AlertController;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Objects.Book;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import com.maxtechnologies.cryptomax.Objects.BookLine;
import com.maxtechnologies.cryptomax.Objects.Coin;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.github.mikephil.charting.data.CandleEntry;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

/**
 * Created by Colman on 01/01/2018.
 */

public class Bitstamp extends Exchange {

    //Pusher declarations
    private Pusher pusher;
    private ArrayList<Integer> subbed;

    //Book declaration
    private Book book;


    public Bitstamp() {
        name = "Bitstamp";
        subbed = new ArrayList<>();
    }



    @Override
    public void findCoins() {
        Request request = new Request.Builder().url("https://www.bitstamp.net/api/v2/trading-pairs-info/").build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Network", e.toString());
                e.printStackTrace();
                AlertController.networkError(activity, true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                coins = new ArrayList<>();

                try {
                    JSONArray symArr = new JSONArray(responseStr);
                    for(int i = 0; i < symArr.length(); i++) {
                        String pairStr = symArr.getJSONObject(i).getString("url_symbol");
                        if(pairStr.substring(3, 6).equals("usd")) {
                            String symbol = pairStr.substring(0, 3);
                            boolean isFiat = false;
                            for(int j = 0; j < fiats.length; j++) {
                                if(fiats[j].symbol.toLowerCase().equals(symbol)) {
                                    isFiat = true;
                                    break;
                                }
                            }

                            if(!isFiat) {
                                coins.add(makeCoin(symbol));
                            }
                        }
                    }

                    sortCoins();
                }

                catch(org.json.JSONException e) {
                    Log.e("Network", e.toString());
                    e.printStackTrace();
                    AlertController.networkError(activity, true);
                }

                callback.coinsCallback();
            }
        });
    }



    @Override
    public void getCandles(int index, final int widthIndex, int limit, long startTime) {
        Request request = new Request.Builder().url("https://api.cryptowat.ch/markets/bitstamp/" + coins.get(index).exchangeSymbol + "usd/ohlc").build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Network", e.toString());
                e.printStackTrace();
                AlertController.networkError(activity, true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                ArrayList<CandleEntry> entries = new ArrayList<>();
                ArrayList<Float> volumes = new ArrayList<>();
                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    jObj = jObj.getJSONObject("result");
                    JSONArray jArr = jObj.getJSONArray(String.valueOf(widthToSec(widths[widthIndex])));
                    for(int i = 0; i < jArr.length(); i++) {
                        JSONArray jArr2 = jArr.getJSONArray(i);
                        float open = BigDecimal.valueOf(jArr2.getDouble(1)).floatValue();
                        float close = BigDecimal.valueOf(jArr2.getDouble(4)).floatValue();
                        float high = BigDecimal.valueOf(jArr2.getDouble(2)).floatValue();
                        float low = BigDecimal.valueOf(jArr2.getDouble(3)).floatValue();
                        entries.add(new CandleEntry(i, high, low, open, close));
                        float volume = BigDecimal.valueOf(jArr2.getDouble(5)).floatValue();
                        volumes.add(volume);
                    }
                }

                catch (org.json.JSONException e) {
                    Log.e("Network", e.toString());
                    e.printStackTrace();
                    AlertController.networkError(activity, true);
                }

                callback.candlesCallback(new ArrayList<Candle>(), volumes);
            }
        });
    }



    @Override
    public void subTickers(ArrayList<Integer> indexes) {
        ArrayList<Integer> unSubFrom = new ArrayList<>();
        ArrayList<Integer> subTo = new ArrayList<>();
        for(int i = 0; i < subbed.size(); i++) {
            if(!indexes.contains(subbed.get(i))) {
                unSubFrom.add(subbed.get(i));
            }
        }
        for(int i = 0; i < indexes.size(); i++) {
            if(!subbed.contains(indexes.get(i))) {
                subTo.add(indexes.get(i));
            }
        }

        if(pusher == null) {
            pusher = new Pusher("de504dc5763aeef9ff52");
            pusher.connect();
        }

        for(int i = 0; i < unSubFrom.size(); i++) {
            pusher.unsubscribe("live_trades_" + coins.get(unSubFrom.get(i)).exchangeSymbol + "usd");
            subbed.remove(unSubFrom.get(i));
        }

        for(int i = 0; i < subTo.size(); i++) {
            final int coinIndex = subTo.get(i);
            Channel channel = pusher.subscribe("live_trades_" + coins.get(coinIndex).exchangeSymbol + "usd");
            channel.bind("trade", new SubscriptionEventListener() {
                @Override
                public void onEvent(String s, String s1, String s2) {
                    try {
                        JSONObject jObj = new JSONObject(s2);
                        float price = BigDecimal.valueOf(jObj.getDouble("price")).floatValue();
                        Coin coin = coins.get(coinIndex);
                        coin.price = price;
                        coins.set(coinIndex, coin);
                    }

                    catch(org.json.JSONException e) {
                        Log.e("Network", e.toString());
                        e.printStackTrace();
                        AlertController.networkError(activity, true);
                    }

                    callback.tickersCallback();
                }
            });
            subbed.add(coinIndex);
        }
    }



    @Override
    public void subBook(int index, final int max) {

        if(pusher == null) {
            pusher = new Pusher("de504dc5763aeef9ff52");
            pusher.connect();
        }

        if(book == null) {
            Channel channel = pusher.subscribe("order_book_" + coins.get(index).exchangeSymbol + "usd");
            channel.bind("data", new SubscriptionEventListener() {
                @Override
                public void onEvent(String s, String s1, String s2) {
                    book = new Book(new ArrayList<BookLine>(), new ArrayList<BookLine>());
                    try {
                        JSONObject jObj = new JSONObject(s2);
                        JSONArray jArr = jObj.getJSONArray("asks");
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONArray jArr2 = jArr.getJSONArray(i);
                            String price = jArr2.getString(0);
                            String amount = jArr2.getString(1);
                            BookLine line = new BookLine(Float.valueOf(price), Float.valueOf(amount));
                            book.asks.add(line);
                        }
                        jArr = jObj.getJSONArray("bids");
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONArray jArr2 = jArr.getJSONArray(i);
                            String price = jArr2.getString(0);
                            String amount = jArr2.getString(1);
                            BookLine line = new BookLine(Float.valueOf(price), Float.valueOf(amount));
                            book.bids.add(line);
                        }
                    }

                    catch(org.json.JSONException e) {
                        Log.e("Network", e.toString());
                        e.printStackTrace();
                        AlertController.networkError(activity, true);
                    }

                    book.sortAndTrimLists(max);
                    pusher.unsubscribe("order_book_");
                    callback.bookCallback(book);
                }
            });
        }

        Channel channel = pusher.subscribe("diff_order_book_" + coins.get(index).exchangeSymbol + "usd");
        channel.bind("data", new SubscriptionEventListener() {
            @Override
            public void onEvent(String s, String s1, String s2) {
                try {
                    if(book != null) {
                        JSONObject jObj = new JSONObject(s2);
                        JSONArray jArr = jObj.getJSONArray("asks");
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONArray jArr2 = jArr.getJSONArray(i);
                            String price = jArr2.getString(0);
                            String amount = jArr2.getString(1);
                            if(amount.equals("0")) {
                                for(int j = 0; j < book.asks.size(); i++) {
                                    if(book.asks.get(j).price == Float.valueOf(price)) {
                                        book.asks.remove(j);
                                    }
                                }
                            }

                            else {
                                book.asks.add(new BookLine(Float.valueOf(price), Float.valueOf(amount)));
                            }
                        }
                        jArr = jObj.getJSONArray("bids");
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONArray jArr2 = jArr.getJSONArray(i);
                            String price = jArr2.getString(0);
                            String amount = jArr2.getString(1);
                            if(amount.equals("0")) {
                                for(int j = 0; j < book.bids.size(); i++) {
                                    if(book.bids.get(j).price == Float.valueOf(price)) {
                                        book.bids.remove(j);
                                    }
                                }
                            }

                            else {
                                book.bids.add(new BookLine(Float.valueOf(price), Float.valueOf(amount)));
                            }
                        }

                        book.sortAndTrimLists(max);
                        callback.bookCallback(book);
                    }
                }

                catch(org.json.JSONException e) {
                    Log.e("Network", e.toString());
                    e.printStackTrace();
                    AlertController.networkError(activity, true);
                }
            }
        });
    }



    @Override
    public void unSubBook() {
        pusher.unsubscribe("diff_order_book_");

        book = null;
    }



    @Override
    public void close() {
        book = null;
        pusher.disconnect();
        pusher = null;
    }
}
