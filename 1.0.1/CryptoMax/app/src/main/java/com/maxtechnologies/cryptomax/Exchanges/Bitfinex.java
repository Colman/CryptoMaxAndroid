package com.maxtechnologies.cryptomax.Exchanges;

import android.util.Log;

import com.maxtechnologies.cryptomax.Controllers.AlertController;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Objects.Book;
import com.maxtechnologies.cryptomax.Objects.BookLine;
import com.maxtechnologies.cryptomax.Objects.Coin;
import com.maxtechnologies.cryptomax.Other.StaticVariables;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by Colman on 01/01/2018.
 */

public class Bitfinex extends Exchange {

    //WebSocket declarations
    public static final int NORMAL_CLOSURE_STATUS = 1000;
    public WebSocket webSocket;

    //Channel declarations
    private int[] channels;
    private int bookChannel;

    //Book declarations
    private Book book;
    private int max;


    public Bitfinex() {
        name = "Bitfinex";
        widths = new String[] {
                "1m",
                "5m",
                "15m",
                "30m",
                "1h",
                "3h",
                "6h",
                "12h",
                "1D"
        };
        bookChannel = -1;
    }



    @Override
    public void findCoins() {
        Request request = new Request.Builder().url("https://api.bitfinex.com/v1/symbols").build();
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
                        String pairStr = symArr.getString(i);
                        if(pairStr.substring(pairStr.length() - 3, pairStr.length()).equals("usd")) {
                            coins.add(makeCoin(pairStr.substring(0, 3)));
                        }
                    }
                    sortCoins();
                    channels = new int[coins.size()];
                    for(int i = 0; i < channels.length; i++) {
                        channels[i] = -1;
                    }
                }

                catch(Exception e) {
                    Log.e("Network", e.toString());
                    e.printStackTrace();
                    AlertController.networkError(activity, true);
                }

                callback.coinsCallback();
            }
        });
    }



    @Override
    public void getCandles(int index, final int widthIndex, int limit, long endTime) {
        int widthSec = widthToSec(chartWidths[widthIndex]);
        int requestIndex = 0;
        for(int i = widths.length - 1; i >= 0; i--) {
            if(widthSec % widthToSec(widths[i]) == 0) {
                requestIndex = i;
                break;
            }
        }
        final String widthStr = widths[requestIndex];

        endTime = endTime * 1000;

        String url;
        if(endTime == 0) {
            url = "https://api.bitfinex.com/v2/candles/trade:" + widthStr + ":t" +
                    coins.get(index).exchangeSymbol.toUpperCase() + "USD/hist?limit=" + String.valueOf(limit);
        }
        else {
            url = "https://api.bitfinex.com/v2/candles/trade:" + widthStr + ":t" +
                    coins.get(index).exchangeSymbol.toUpperCase() + "USD/hist?end=" + String.valueOf(endTime) +
                    "&limit=" + String.valueOf(limit);
        }


        Request candleRequest = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(candleRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Network", e.toString());
                e.printStackTrace();
                AlertController.networkError(activity, true);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonString = response.body().string();

                try {
                    JSONArray jArr = new JSONArray(jsonString);
                    ArrayList<Candle> candleEntries = new ArrayList<>();
                    ArrayList<Float> volumeEntries = new ArrayList<>();

                    for(int i = jArr.length() - 1; i >= 0; i--) {
                        JSONArray jArr2 = jArr.getJSONArray(i);
                        long time = jArr2.getLong(0) / 1000;
                        float open = BigDecimal.valueOf(jArr2.getDouble(1)).floatValue();
                        float close = BigDecimal.valueOf(jArr2.getDouble(2)).floatValue();
                        float high = BigDecimal.valueOf(jArr2.getDouble(3)).floatValue();
                        float low = BigDecimal.valueOf(jArr2.getDouble(4)).floatValue();
                        candleEntries.add(new Candle(time, open, close, high, low));
                        float volume = BigDecimal.valueOf(jArr2.getDouble(5)).floatValue();
                        volumeEntries.add(volume);
                    }

                    ArrayList<Candle> newCandles = Exchange.convertCandles(candleEntries, widthIndex);
                    //ArrayList<Float> newVolumes = Exchange.convertVolumes(volumeEntries, widthStr, widthIndex);
                    callback.candlesCallback(newCandles, volumeEntries);
                }

                catch (Exception e) {
                    Log.e("Network", e.toString());
                    e.printStackTrace();
                    AlertController.networkError(activity, true);
                }

                response.close();
            }
        });
    }



    @Override
    public void subTickers(final ArrayList<Integer> indexes) {
        if(webSocket == null && indexes.size() != 0) {
            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    //Do nothing
                }

                @Override
                public void onMessage(WebSocket webSocket, final String text) {
                    handleMessage(text);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Log.i("Network", "Websocket closing for reason: " + reason);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    Log.e("Network", t.toString());
                }
            };
            Request request = new Request.Builder().url("wss://api.bitfinex.com/ws/2").build();
            webSocket = StaticVariables.client.newWebSocket(request, listener);
        }

        for(int i = 0 ; i < channels.length; i++) {
            if(indexes.contains(i) && channels[i] == -1) {
                JSONObject jObj = new JSONObject();
                try {
                    jObj.put("event", "subscribe");
                    jObj.put("channel", "ticker");
                    jObj.put("pair", "t" + coins.get(i).exchangeSymbol.toUpperCase() + "USD");
                }

                catch(Exception e) {
                    //Do nothing
                }
                webSocket.send(jObj.toString());
                channels[i] = 0;
            }

            else if(!indexes.contains(i) && channels[i] > 0) {
                JSONObject jObj = new JSONObject();
                try {
                    jObj.put("event", "unsubscribe");
                    jObj.put("chanId", channels[i]);
                }

                catch (Exception e) {
                    //Do nothing
                }
                webSocket.send(jObj.toString());
                channels[i] = -1;
            }
        }

        if(indexes.size() == 0) {
            for(int i = 0; i < channels.length; i++) {
                channels[i] = -1;
            }

            if(webSocket != null) {
                webSocket.close(NORMAL_CLOSURE_STATUS, "Not subbed to any tickers");
            }

            webSocket = null;
        }
    }



    @Override
    public void subBook(final int index, int max) {
        book = new Book(new ArrayList<BookLine>(), new ArrayList<BookLine>());
        this.max = max;

        if(webSocket == null) {
            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    //Do nothing
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    handleMessage(text);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    Log.i("Network", "Websocket closing for reason: " + reason);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    Log.e("Network", t.toString());
                }
            };
            Request request = new Request.Builder().url("wss://api.bitfinex.com/ws/2").build();
            webSocket = StaticVariables.client.newWebSocket(request, listener);
        }

        if(bookChannel != -1) {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("event", "unsubscribe");
                jObj.put("chanId", bookChannel);
            }
            catch(Exception e) {
                //Do nothing
            }

            webSocket.send(jObj.toString());
        }

        JSONObject jObj = new JSONObject();
        try {
            jObj.put("event", "subscribe");
            jObj.put("channel", "book");
            jObj.put("symbol", coins.get(index).exchangeSymbol.toUpperCase() + "USD");
        }
        catch(Exception e) {
            //Do nothing
        }
        webSocket.send(jObj.toString());
    }



    @Override
    public void unSubBook() {
        if(bookChannel != -1) {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("event", "unsubscribe");
                jObj.put("chanId", bookChannel);
            }

            catch (Exception e) {
                //Do nothing
            }

            webSocket.send(jObj.toString());
        }

        bookChannel = -1;
        book = null;
    }



    private void handleMessage(String text) {
        try {
            JSONObject jObj = new JSONObject(text);
            if (jObj.getString("event").equals("subscribed")) {
                if (jObj.getString("channel").equals("ticker")) {
                    for (int i = 0; i < coins.size(); i++) {
                        String pair = coins.get(i).exchangeSymbol.toUpperCase() + "USD";
                        if (pair.equals(jObj.getString("pair"))) {
                            channels[i] = jObj.getInt("chanId");
                            break;
                        }
                    }
                }

                else {
                    bookChannel = jObj.getInt("chanId");
                }
            }
        }

        catch (Exception e) {
            try {
                JSONArray jArr = new JSONArray(text);
                int channel = jArr.getInt(0);
                for (int i = 0; i < channels.length; i++) {
                    if (channels[i] == channel) {
                        JSONArray jArr2 = jArr.getJSONArray(1);
                        float price = BigDecimal.valueOf(jArr2.getDouble(6)).floatValue();
                        float change = BigDecimal.valueOf(jArr2.getDouble(5) * 100).floatValue();
                        float volume = BigDecimal.valueOf(jArr2.getDouble(7)).floatValue();
                        Coin coin = coins.get(i);
                        coin.price = price;
                        coin.change1d = change;
                        coin.volume1d = volume;
                        coins.set(i, coin);
                        callback.tickersCallback();
                        return;
                    }
                }

                Book book2 = findBook(text, max);
                if(book2 != null) {
                    callback.bookCallback(book2);
                }
            }

            catch (Exception e2) {
                //Do nothing
            }
        }
    }



    private Book findBook(String jsonString, int max) {
        try {
            JSONArray jArr = new JSONArray(jsonString);
            jArr = jArr.getJSONArray(1);
            float[][] entries;
            try {
                JSONArray jArr2 = jArr.getJSONArray(0);
                book = new Book(new ArrayList<BookLine>(), new ArrayList<BookLine>());
                entries = new float[jArr.length()][3];
                for(int i = 0; i < jArr.length(); i++) {
                    entries[i][0] = BigDecimal.valueOf(jArr.getJSONArray(i).getDouble(0)).floatValue();
                    entries[i][1] = BigDecimal.valueOf(jArr.getJSONArray(i).getDouble(1)).floatValue();
                    entries[i][2] = BigDecimal.valueOf(jArr.getJSONArray(i).getDouble(2)).floatValue();
                }
            }
            catch (Exception e) {
                entries = new float[1][3];
                entries[0][0] = BigDecimal.valueOf(jArr.getDouble(0)).floatValue();
                entries[0][1] = BigDecimal.valueOf(jArr.getDouble(1)).floatValue();
                entries[0][2] = BigDecimal.valueOf(jArr.getDouble(2)).floatValue();
            }


            for(int i = 0; i < entries.length; i++) {
                float price = entries[i][0];
                float count = entries[i][1];
                float amount = entries[i][2];

                if (count > 0) {
                    if (amount < 0) {
                        boolean notIn = true;
                        for (int j = 0; j < book.asks.size(); j++) {
                            if (book.asks.get(j).price == price) {
                                BookLine bLine = book.asks.get(j);
                                bLine.quantity = -1 * amount;
                                book.asks.set(j, bLine);
                                notIn = false;
                                break;
                            }
                        }
                        if (notIn) {
                            BookLine bLine = new BookLine(price, -1 * amount);
                            book.asks.add(bLine);
                        }
                    }

                    else if (amount > 0) {
                        boolean notIn = true;
                        for (int j = 0; j < book.bids.size(); j++) {
                            if (book.bids.get(j).price == price) {
                                BookLine bLine = book.bids.get(j);
                                bLine.quantity = amount;
                                book.bids.set(j, bLine);
                                notIn = false;
                                break;
                            }
                        }
                        if (notIn) {
                            BookLine bLine = new BookLine(price, amount);
                            book.bids.add(bLine);
                        }
                    }
                }

                else if (count == 0) {
                    if(amount == -1) {
                        for(int j = 0; j < book.asks.size(); j++) {
                            if(book.asks.get(j).price == price) {
                                book.asks.remove(j);
                                break;
                            }
                        }
                    }
                    else if (amount == 1) {
                        for(int j = 0; j < book.bids.size(); j++) {
                            if(book.bids.get(j).price == price) {
                                book.bids.remove(j);
                                break;
                            }
                        }
                    }
                }
            }

            book.sortAndTrimLists(max);
            return book;
        }

        catch(Exception e) {
            //Do nothing
        }

        return book;
    }



    @Override
    public void close() {
        for(int i = 0; i < channels.length; i++) {
            channels[i] = -1;
        }
        bookChannel = -1;

        if(webSocket != null) {
            webSocket.cancel();
        }
        webSocket = null;
        book = null;
    }
}
