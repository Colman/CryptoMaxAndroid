package com.maxtechnologies.cryptomax.Main;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Controllers.AlertController;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Objects.Book;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Objects.Coin;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Callbacks.NetworkCallbacks;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class LoadingScreenActivity extends AppCompatActivity implements NetworkCallbacks {
    //Exchange declarations
    private boolean hasInit;
    private boolean hasPrices;
    private ArrayList<ArrayList<String>> pairsArray;
    private ArrayList<ArrayList<String>> symbolsArray;

    //UI declaration
    private ProgressBar progress;
    private Handler animHandler;

    //Network declarations
    private long startTime;
    private long openTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        Exchange.activity = this;
        Exchange.callback = this;


        //Exchange definitions
        hasInit = false;
        hasPrices = false;
        pairsArray = new ArrayList<>();
        pairsArray.add(new ArrayList<String>());
        symbolsArray = new ArrayList<>();
        symbolsArray.add(new ArrayList<String>());

        //UI declaration
        progress = findViewById(R.id.progress);
        animHandler = new Handler();

        //Network definitions
        startTime = System.currentTimeMillis();
        openTime = 1500;


        //Set progress bar color
        progress.getIndeterminateDrawable().setColorFilter(
                Color.parseColor("#cacfd3"), android.graphics.PorterDuff.Mode.SRC_IN);

        //Go fullscreen
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        //Load settings and wallets
        Settings.loadSettings(this);
        StaticVariables.init(this);
        CryptoMaxApi.init(this, new BasicCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Unable to init API for reason: " + reason);
                AlertController.networkError(LoadingScreenActivity.this, true);
            }

            @Override
            public void onSuccess() {
                Log.d("test", "Init");
                LoadingScreenActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StaticVariables.exchanges[Settings.exchangeIndex].findCoins();
                    }
                });

                hasInit = true;
                long netTime = System.currentTimeMillis() - startTime;
                if(netTime >= openTime && hasPrices) {
                    doneLoading();
                }
            }
        });


        //Setup the handler for waiting the minimum animation time
        animHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("test", hasInit + "");
                Log.d("test", hasPrices + "");
                if(hasInit && hasPrices) {
                    doneLoading();
                }
            }
        }, openTime);
    }



    @Override
    public void coinsCallback() {
        Log.d("test", "Coins");
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            CryptoMaxApi.getWallet(i).exchangeSymbol = Exchange.translateToExchangeSymbol(CryptoMaxApi.getWallet(i).exchangeSymbol);
        }

        ArrayList<Integer> indexes = new ArrayList<>();
        for(int i = 0; i < StaticVariables.exchanges[Settings.exchangeIndex].coins.size(); i++) {
            indexes.add(i);
        }
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            int index = Exchange.findIndex(CryptoMaxApi.getWallet(i).exchangeSymbol);
            if(!indexes.contains(index)) {
                indexes.add(index);
            }
        }

        StaticVariables.exchanges[Settings.exchangeIndex].subTickers(indexes);
    }



    @Override
    public void candlesCallback(ArrayList<Candle> entries, ArrayList<Float> volume) {
        //Do nothing
    }



    @Override
    public void tickersCallback() {
        Log.d("test", "Tickers");
        ArrayList<Coin> coins = StaticVariables.exchanges[Settings.exchangeIndex].coins;

        boolean containsZero = false;
        for(int i = 0; i < coins.size(); i++) {
            if(coins.get(i).price == 0) {
                containsZero = true;
                break;
            }
        }

        if(!containsZero) {
            hasPrices = true;
            long netTime = System.currentTimeMillis() - startTime;
            if(netTime >= openTime && hasInit) {
                doneLoading();
            }
        }
    }



    @Override
    public void bookCallback(Book book) {
        //Do nothing
    }



    private void doneLoading() {
        Log.d("test", "done");
        Intent intent = new Intent(LoadingScreenActivity.this, MainActivity.class);
        startActivity(intent);
    }
}