package com.maxtechnologies.cryptomax.exchange;

import com.maxtechnologies.cryptomax.exchange.asset.CoinFiatPairsCallback;
import com.maxtechnologies.cryptomax.exchange.asset.CoinFiatPair;
import com.maxtechnologies.cryptomax.exchange.book.BookListener;
import com.maxtechnologies.cryptomax.exchange.candle.CandlesCallback;
import com.maxtechnologies.cryptomax.exchange.book.Book;

import java.util.ArrayList;

import com.pusher.client.Pusher;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 01/01/2018.
 */

public class Bitstamp extends Exchange {

    //Url declaration
    private final static String url = "https://www.bitstamp.net/api/";

    //Pusher declarations
    private Pusher pusher;
    private ArrayList<Integer> subbed;

    //Book declaration
    private Book book;


    private Bitstamp(CoinFiatPair[] pairs) {
        super("Bitstamp", pairs);
        subbed = new ArrayList<>();
    }



    public static void findPairs(CoinFiatPairsCallback callback) {
        /*
        URL urlObj = new URL(url + "v2/trading-pairs-info");
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = reader.readLine();

        coins = new ArrayList<>();
        try {
            JSONArray symArr = new JSONArray(line);
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
        */
    }



    @Override
    public void getCandles(int index, int widthIndex, long startTime, long endTime, @Nonnull CandlesCallback callback) {
        /*
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
        */
    }



    @Override
    public void subTickers(@Nonnull int[] indexes, @Nonnull PriceListener listener) {
        /*
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
                        Asset coin = coins.get(coinIndex);
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
        */
    }



    public void unSubTickers(@Nonnull PriceListener listener) {

    }



    @Override
    public void subBooks(@Nonnull int[] indexes, @Nonnull int[] maxes, @Nonnull BookListener listener) throws IllegalArgumentException {
        /*
        if(pusher == null) {
            pusher = new Pusher("de504dc5763aeef9ff52");
            pusher.connect();
        }

        if(book == null) {
            Channel channel = pusher.subscribe("order_book_" + coins.get(index).exchangeSymbol + "usd");
            channel.bind("data", new SubscriptionEventListener() {
                @Override
                public void onEvent(String s, String s1, String s2) {
                    book = new Book(new ArrayList<Order>(), new ArrayList<Order>());
                    try {
                        JSONObject jObj = new JSONObject(s2);
                        JSONArray jArr = jObj.getJSONArray("asks");
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONArray jArr2 = jArr.getJSONArray(i);
                            String price = jArr2.getString(0);
                            String amount = jArr2.getString(1);
                            Order line = new Order(Float.valueOf(price), Float.valueOf(amount));
                            book.asks.add(line);
                        }
                        jArr = jObj.getJSONArray("bids");
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONArray jArr2 = jArr.getJSONArray(i);
                            String price = jArr2.getString(0);
                            String amount = jArr2.getString(1);
                            Order line = new Order(Float.valueOf(price), Float.valueOf(amount));
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
                                book.asks.add(new Order(Float.valueOf(price), Float.valueOf(amount)));
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
                                book.bids.add(new Order(Float.valueOf(price), Float.valueOf(amount)));
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
        */
    }



    @Override
    public void unSubBooks(@Nonnull BookListener listener) {
        /*
        pusher.unsubscribe("diff_order_book_");

        book = null;
        */
    }



    @Override
    public void close() {
        /*
        book = null;
        pusher.disconnect();
        pusher = null;
        */
    }
}
