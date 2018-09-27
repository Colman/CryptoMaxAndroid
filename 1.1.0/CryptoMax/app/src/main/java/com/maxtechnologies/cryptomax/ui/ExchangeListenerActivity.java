package com.maxtechnologies.cryptomax.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.maxtechnologies.cryptomax.exchange.Bitfinex;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.exchange.ExchangeCallback;

import java.util.ArrayList;

import javax.annotation.Nullable;

/**
 * Created by Colman on 24/07/2018.
 */

public abstract class ExchangeListenerActivity extends MainActivity {

    //Exchange declarations
    private static Exchange exchange;
    private static int selectedExchange = -1;
    private static ArrayList<ExchangeCallback> callbacks;


    @Nullable
    protected Exchange getExchange() {
        return exchange;
    }



    protected void setExchange(int index, final ExchangeCallback callback) {
        if (index != selectedExchange)
            switch (index) {
                case 0:
                    Bitfinex.newInstance(new ExchangeCallback() {
                        @Override
                        public void onFailure(String reason) {
                            callback.onFailure("Failed to instantiate Bitfinex for reason: " + reason);
                        }

                        @Override
                        public void onSuccess(Exchange exchange) {
                            ExchangeListenerActivity.exchange = exchange;
                            callback.onSuccess(exchange);
                        }
                    });
                    break;
            }

        selectedExchange = index;
        getPreferences(MODE_PRIVATE).edit().putInt("exchange", index).apply();
    }
}
