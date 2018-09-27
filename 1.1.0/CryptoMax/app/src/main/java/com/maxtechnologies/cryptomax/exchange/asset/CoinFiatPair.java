package com.maxtechnologies.cryptomax.exchange.asset;

import com.maxtechnologies.cryptomax.api.CryptoMaxApi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 23/07/2018.
 */

public class CoinFiatPair extends AssetPair {
    private Coin coin;
    private Fiat fiat;


    public CoinFiatPair(@Nonnull Coin coin, @Nonnull Fiat fiat, @Nonnull String exchangeSymbol) {
        super(coin, fiat, exchangeSymbol);
    }


    public static CoinFiatPair[] makePairs(@Nonnull String[] symbols, @Nonnull String[] symbols2, @Nonnull String[] exchangeSymbols, CoinFiatPairsCallback callback) throws IllegalArgumentException {
        if(symbols.length != symbols2.length || symbols.length != exchangeSymbols.length)
            throw new IllegalArgumentException("Input arrays must be the same length");

        ArrayList<String> querySymbols = new ArrayList<>();
        for (int i = 0; i < symbols.length; i++) {
            String symbol = symbols[i].toUpperCase();
            if (!querySymbols.contains(symbol))
                querySymbols.add(symbol);

            symbol = symbols2[i].toUpperCase();
            if (!querySymbols.contains(symbol))
                querySymbols.add(symbol);
        }



        //Make into Okhttp call
        URL urlObj = new URL(CryptoMaxApi.domain + "api/data.php");
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = reader.readLine();

    }
}
