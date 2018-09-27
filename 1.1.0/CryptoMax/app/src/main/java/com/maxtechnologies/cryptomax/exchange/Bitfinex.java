package com.maxtechnologies.cryptomax.exchange;

import com.maxtechnologies.cryptomax.exchange.asset.CoinFiatPairsCallback;
import com.maxtechnologies.cryptomax.exchange.asset.CoinFiatPair;
import com.maxtechnologies.cryptomax.misc.BasicCallback;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.exchange.asset.AssetPair;
import com.maxtechnologies.cryptomax.exchange.book.BookListener;
import com.maxtechnologies.cryptomax.exchange.candle.CandlesCallback;
import com.maxtechnologies.cryptomax.exchange.candle.Candle;
import com.maxtechnologies.cryptomax.exchange.book.Book;
import com.maxtechnologies.cryptomax.exchange.book.Order;
import com.maxtechnologies.cryptomax.exchange.candle.CandleUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import static com.maxtechnologies.cryptomax.exchange.candle.CandleUtils.widthToMS;

/**
 * Created by Colman on 01/01/2018.
 */

public class Bitfinex extends Exchange {

    //Url definition
    private final static String url = "https://api.bitfinex.com/";

    //Widths definition
    private final static String[] widths = new String[] {
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

    //WebSocket declarations
    private WebSocket webSocket;

    //Listener declaration
    private final WebSocketListener listener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            //Do nothing
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
            try {
                handleMessage(text);
            } catch (JSONException e) {
                for(ArrayList<PriceListener> listeners : priceListeners) {
                    for(PriceListener listener : listeners)
                        listener.onFailure("Websocket failed for reason: Invalid response JSON");
                }

                for(ArrayList<BookListener> listeners : bookListeners) {
                    for(BookListener listener : listeners)
                        listener.onFailure("Websocket failed for reason: Invalid response JSON");
                }
                close();
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            //Do nothing
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            for(ArrayList<PriceListener> listeners : priceListeners) {
                for(PriceListener listener : listeners)
                    listener.onFailure("Websocket failed for reason: " + t.toString());
            }

            for(ArrayList<BookListener> listeners : bookListeners) {
                for(BookListener listener : listeners)
                    listener.onFailure("Websocket failed for reason: " + t.toString());
            }
        }
    };

    //Channel declarations
    private int[] priceChannels;
    private int[] bookChannels;

    //Price listeners declaration
    private ArrayList<PriceListener>[] priceListeners;

    //Books declaration
    private Book[] books;

    //Sizes declaration
    private ArrayList<Integer>[] sizes;

    //Book listeners declaration
    private ArrayList<BookListener>[] bookListeners;


    private Bitfinex(@Nonnull CoinFiatPair[] pairs) {
        super("Bitfinex", pairs);

        int pairsSize = getAssetPairsSize();
        priceChannels = new int[pairsSize];
        bookChannels = new int[pairsSize];
        priceListeners = new ArrayList[pairsSize];
        books = new Book[pairsSize];
        sizes = new ArrayList[pairsSize];
        bookListeners = new ArrayList[pairsSize];
        for(int i = 0; i < pairsSize; i++) {
            priceChannels[i] = -1;
            bookChannels[i] = -1;
            priceListeners[i] = new ArrayList<>();
            sizes[i] = new ArrayList<>();
            bookListeners[i] = new ArrayList<>();
        }
    }



    public static void newInstance(final ExchangeCallback callback) {
        findPairs(new CoinFiatPairsCallback() {
            @Override
            public void onFailure(String reason) {
                callback.onFailure("Failed to find pairs for reason: " + reason);
            }

            @Override
            public void onSuccess(CoinFiatPair[] pairs) {
                callback.onSuccess(new Bitfinex(pairs));
            }
        });
    }



    private static void findPairs(final CoinFiatPairsCallback callback) {
        Request request = new Request.Builder().url(url + "v1/symbols").build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONArray symbolArr = new JSONArray(responseStr);
                    String[] symbols = new String[symbolArr.length()];
                    String[] symbols2 = new String[symbolArr.length()];
                    String[] exchangeSymbols = new String[symbolArr.length()];
                    for(int i = 0; i < symbolArr.length(); i++) {
                        String pairStr = symbolArr.getString(i);

                        symbols[i] = pairStr.substring(0, 3);
                        symbols2[i] = pairStr.substring(3, 6);
                        exchangeSymbols[i] = pairStr;
                    }

                    callback.onSuccess(CoinFiatPair.makePairs(symbols, symbols2, exchangeSymbols, callback));
                } catch (JSONException e) {
                    callback.onFailure("Invalid response JSON");
                }
            }
        });
    }



    @Override
    public void getCandles(int index, int widthIndex, final long startTime, final long endTime, @Nonnull final CandlesCallback callback) {
        final long widthMS = widthToMS(Exchange.widths[widthIndex]);
        int requestIndex = 0;
        for(int i = widths.length - 1; i >= 0; i--) {
            if(widthMS % widthToMS(widths[i]) == 0) {
                requestIndex = i;
                break;
            }
        }
        final String requestWidthStr = widths[requestIndex];


        String url = Bitfinex.url + "v2/candles/trade:" + requestWidthStr + ":t" +
                getAssetPair(index).getExchangeSymbol().toUpperCase() + "/hist?limit=1000&sort=1";
        if(startTime != -1) {
            url += "&start=" + String.valueOf(startTime);
        }

        if(endTime != -1) {
            url += "&end=" + String.valueOf(endTime);
        }


        Request candleRequest = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(candleRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonString = response.body().string();

                try {
                    JSONArray jArr = new JSONArray(jsonString);
                    ArrayList<Candle> candles = new ArrayList<>();

                    for(int i = 0; i < jArr.length(); i++) {
                        JSONArray jArr2 = jArr.getJSONArray(i);
                        long time = jArr2.getLong(0);
                        BigDecimal open = new BigDecimal(jArr2.getString(1));
                        BigDecimal close = new BigDecimal(jArr2.getString(2));
                        BigDecimal high = new BigDecimal(jArr2.getString(3));
                        BigDecimal low = new BigDecimal(jArr2.getString(4));
                        BigDecimal volume = new BigDecimal(jArr2.getString(5));
                        candles.add(new Candle(time, open, close, high, low, volume));
                    }

                    Candle[] candlesArr = candles.toArray(new Candle[candles.size()]);
                    long requestWidth = CandleUtils.widthToMS(requestWidthStr);
                    candlesArr = CandleUtils.fillCandles(candlesArr, requestWidth, startTime, endTime);
                    candlesArr = CandleUtils.convertCandles(candlesArr, widthMS);
                    callback.onSuccess(candlesArr);

                } catch (JSONException e) {
                    callback.onFailure("Invalid response JSON");
                }
            }
        });
    }



    @Override
    public void subTickers(@Nonnull final int[] indexes, @Nonnull final PriceListener listener) {
        if(webSocket == null && indexes.length != 0) {
            Request request = new Request.Builder().url("wss://api.bitfinex.com/ws/2").build();
            webSocket = StaticVariables.client.newWebSocket(request, this.listener);
        }

        for(int index : indexes) {
            priceListeners[index].add(listener);

            if(priceChannels[index] == -1) {
                JSONObject jObj = new JSONObject();
                try {
                    jObj.put("event", "subscribe");
                    jObj.put("channel", "ticker");
                    jObj.put("pair", "t" + getAssetPair(index).getExchangeSymbol().toUpperCase());
                } catch(JSONException e) {
                    //Do nothing
                }
                webSocket.send(jObj.toString());
                priceChannels[index] = 0;
            }
        }
    }



    @Override
    public void unSubTickers(@Nonnull PriceListener listener) {
        for(int i = 0; i < priceListeners.length; i++) {
            int previousSize = priceListeners[i].size();
            ArrayList<PriceListener> newListeners = new ArrayList<>();
            for(PriceListener priceListener : priceListeners[i]) {
                if(priceListener != listener)
                    newListeners.add(priceListener);
            }
            priceListeners[i] = newListeners;

            if(priceListeners[i].size() == 0 && previousSize != 0 ) {
                JSONObject jObj = new JSONObject();
                try {
                    jObj.put("event", "unsubscribe");
                    jObj.put("chanId", priceChannels[i]);
                } catch (JSONException e) {
                    //Do nothing
                }
                webSocket.send(jObj.toString());
                priceChannels[i] = -1;
            }
        }
    }



    @Override
    public void subBooks(@Nonnull int[] indexes, @Nonnull int[] maxes, @Nonnull BookListener listener) throws IllegalArgumentException {
        if(indexes.length != maxes.length)
            throw new IllegalArgumentException("Indexes and maxes must be the same length");

        if(webSocket == null) {
            Request request = new Request.Builder().url("wss://api.bitfinex.com/ws/2").build();
            webSocket = StaticVariables.client.newWebSocket(request, this.listener);
        }


        for(int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            bookListeners[index].add(listener);
            sizes[index].add(maxes[i]);

            if(bookChannels[index] == -1) {
                JSONObject jObj = new JSONObject();
                try {
                    jObj.put("event", "subscribe");
                    jObj.put("channel", "book");
                    jObj.put("symbol", getAssetPair(index).getExchangeSymbol().toUpperCase());
                } catch(JSONException e) {
                    //Do nothing
                }
                webSocket.send(jObj.toString());
                bookChannels[index] = 0;
            }
        }
    }



    @Override
    public void unSubBooks(@Nonnull BookListener listener) {
        for(int i = 0; i < bookListeners.length; i++) {
            int previousSize = bookListeners[i].size();
            ArrayList<BookListener> newListeners = new ArrayList<>();
            ArrayList<Integer> newSizes = new ArrayList<>();
            for(int j = 0; j < bookListeners[i].size(); j++) {
                if(bookListeners[i].get(j) != listener) {
                    newListeners.add(bookListeners[i].get(j));
                    newSizes.add(sizes[i].get(j));
                }
            }
            bookListeners[i] = newListeners;
            sizes[i] = newSizes;

            if(bookListeners[i].size() == 0 && previousSize != 0 ) {
                JSONObject jObj = new JSONObject();
                try {
                    jObj.put("event", "unsubscribe");
                    jObj.put("chanId", bookChannels[i]);
                } catch (JSONException e) {
                    //Do nothing
                }
                webSocket.send(jObj.toString());
                bookChannels[i] = -1;
                books[i] = null;
            }
        }
    }



    @Override
    public void close() {
        if(webSocket != null) {
            webSocket.cancel();
        }
        webSocket = null;

        for(int i = 0; i < priceChannels.length; i++) {
            priceChannels[i] = -1;
            bookChannels[i] = -1;
            priceListeners[i] = new ArrayList<>();
            sizes[i] = new ArrayList<>();
            bookListeners[i] = new ArrayList<>();
        }
    }



    private void handleMessage(String text) throws JSONException {
        try {
            JSONObject jObj = new JSONObject(text);
            if (jObj.getString("event").equals("subscribed")) {
                int pairSize = getAssetPairsSize();
                for (int i = 0; i < pairSize; i++) {
                    String pair = getAssetPair(i).getExchangeSymbol().toUpperCase();
                    if (pair.equals(jObj.getString("pair"))) {
                        if (jObj.getString("channel").equals("ticker")) {
                            priceChannels[i] = jObj.getInt("chanId");
                        } else {
                            bookChannels[i] = jObj.getInt("chanId");
                        }

                        break;
                    }
                }
            }
        } catch (JSONException e) {
            JSONArray jArr = new JSONArray(text);
            int channel = jArr.getInt(0);
            for (int i = 0; i < priceChannels.length; i++) {
                if (priceChannels[i] == channel) {
                    JSONArray jArr2 = jArr.getJSONArray(1);
                    BigDecimal price = new BigDecimal(jArr2.getString(6));
                    float change = BigDecimal.valueOf(jArr2.getDouble(5) * 100).floatValue();

                    AssetPair pair = getAssetPair(i);
                    pair.setPrice(price);
                    pair.setChange1d(change);

                    for(PriceListener listener : priceListeners[i])
                        listener.onPriceChanged(i);

                    return;
                }
            }

            for (int i = 0; i < bookChannels.length; i++) {
                if (bookChannels[i] == channel) {
                    updateBook(jArr.getJSONArray(1).toString(), i);

                    for (int j = 0; j < bookListeners[i].size(); j++) {
                        Book newBook = new Book(sizes[i].get(j));
                        int minSize = sizes[i].get(j);
                        if(minSize > books[i].getSize())
                            minSize = books[i].getSize();
                        for (int k = 0; k < minSize; k++) {
                            newBook.setAsk(k, books[i].getAsk(k));
                            newBook.setBid(k, books[i].getBid(k));
                        }
                        bookListeners[i].get(j).onBookChanged(newBook);
                    }

                    break;
                }
            }
        }
    }



    private void updateBook(String bookJson, int index) throws JSONException {
        JSONArray jArr = new JSONArray(bookJson);

        ArrayList<BigDecimal> prices = new ArrayList<>();
        ArrayList<Integer> counts = new ArrayList<>();
        ArrayList<BigDecimal> amounts = new ArrayList<>();
        for (int i = 0; i < jArr.length(); i++) {
            try {
                JSONArray jArr2 = jArr.getJSONArray(i);
                prices.add(new BigDecimal(jArr2.getString(0)));
                counts.add(jArr2.getInt(1));
                amounts.add(new BigDecimal(jArr2.getString(2)));
            } catch (JSONException e2) {
                prices.add(new BigDecimal(jArr.getString(0)));
                counts.add(jArr.getInt(1));
                amounts.add(new BigDecimal(jArr.getString(2)));
                break;
            }
        }

        Book book = books[index];
        for (int i = 0; i < prices.size(); i++) {
            BigDecimal price = prices.get(i);
            int count = counts.get(i);
            BigDecimal amount = amounts.get(i);

            if (count > 0) {
                if (amount.compareTo(new BigDecimal(0)) > 0) {
                    for (int j = 0; j < book.getSize(); j++) {
                        if (book.getBid(j) == null || book.getBid(j).getPrice().compareTo(price) >= 0) {
                            for (int k = book.getSize() - 2; k >= j; k--)
                                book.setBid(k + 1, book.getBid(k));
                            Order order = new Order(price, amount);
                            book.setBid(j, order);
                            break;
                        }
                    }
                } else  {
                    for (int j = 0; j < book.getSize(); j++) {
                        if (book.getAsk(j) == null || book.getAsk(j).getPrice().compareTo(price) <= 0) {
                            for (int k = book.getSize() - 2; k >= j; k--)
                                book.setAsk(k + 1, book.getAsk(k));
                            Order order = new Order(price, amount.multiply(new BigDecimal(1)));
                            book.setAsk(j, order);
                            break;
                        }
                    }
                }
            } else {
                if (amount.compareTo(new BigDecimal(1)) == 0) {
                    for (int j = 0; j < book.getSize(); j++) {
                        if (book.getBid(j).getPrice().compareTo(price) == 0) {
                            for (int k = j; k < book.getSize() - 1; k++)
                                book.setBid(k, book.getBid(k + 1));
                            break;
                        }
                    }
                } else {
                    for (int j = 0; j < book.getSize(); j++) {
                        if (book.getAsk(j).getPrice().compareTo(price) == 0) {
                            for (int k = j; k < book.getSize() - 1; k++)
                                book.setAsk(k, book.getAsk(k + 1));
                            break;
                        }
                    }
                }
            }
        }
    }
}
