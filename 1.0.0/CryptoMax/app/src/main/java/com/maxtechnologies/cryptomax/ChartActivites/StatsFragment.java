package com.maxtechnologies.cryptomax.ChartActivites;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Objects.Coin;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;

import java.util.Timer;
import java.util.TimerTask;


public class StatsFragment extends Fragment {

    //Timer declarations
    private Timer timer;
    private Handler handler;

    //Index declaration
    private int index;

    //Price declarations
    public float price1h;
    private long hourTime;
    public float price7d;
    private long weekTime;

    //UI declarations
    public TextView marketCap;
    public TextView priceToTitle;
    public TextView priceTo;
    public TextView volume1d;
    public ImageView arrow1h;
    public TextView change1h;
    public TextView abs1h;
    public ImageView arrow1d;
    public TextView change1d;
    public TextView abs1d;
    public ImageView arrow7d;
    public TextView change7d;
    public TextView abs7d;
    public TextView supply;
    public TextView mined;


    public static StatsFragment newInstance(int index) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putInt("INDEX", index);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_stats, container, false);

        Bundle bundle = getArguments();
        index = bundle.getInt("INDEX");
        Coin coin = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index);

        //Timer declarations
        timer = new Timer();
        handler = new Handler();

        //Price definitions
        price1h = 0;
        price7d = 0;

        //UI definitions
        marketCap = (TextView) rootView.findViewById(R.id.market_cap);
        priceToTitle = (TextView) rootView.findViewById(R.id.price_to_title);
        priceTo = (TextView) rootView.findViewById(R.id.price_to);
        volume1d = (TextView) rootView.findViewById(R.id.volume);
        arrow1h = (ImageView) rootView.findViewById(R.id.arrow_1h);
        change1h = (TextView) rootView.findViewById(R.id.change_1h);
        abs1h = (TextView) rootView.findViewById(R.id.abs_1h);
        arrow1d = (ImageView) rootView.findViewById(R.id.arrow_1d);
        change1d = (TextView) rootView.findViewById(R.id.change_1d);
        abs1d = (TextView) rootView.findViewById(R.id.abs_1d);
        arrow7d = (ImageView) rootView.findViewById(R.id.arrow_7d);
        change7d = (TextView) rootView.findViewById(R.id.change_7d);
        abs7d = (TextView) rootView.findViewById(R.id.abs_7d);
        supply = (TextView) rootView.findViewById(R.id.supply);
        mined = (TextView) rootView.findViewById(R.id.mined);


        //Set UI values
        marketCap.setText(String.format("%,.0f", coin.marketCap * Exchange.fiats[Settings.currency].perUS) +
                " " + Exchange.fiats[Settings.currency].symbol);
        String compTitle = priceToTitle.getText().toString();
        priceToTitle.setText(compTitle + " " + StaticVariables.exchanges[Settings.exchangeIndex].coins.get(0).symbol.toUpperCase());
        supply.setText(String.format("%,.0f", coin.supply) + " " + coin.symbol);
        String minedStr = Exchange.findMined(coin.symbol);
        mined.setText(minedStr);


        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        long currTime = System.currentTimeMillis();
                        weekTime = (currTime / 1000) - 604800;
                        StaticVariables.exchanges[Settings.exchangeIndex].getCandles(index, 0, 1, weekTime);
                        hourTime = (currTime / 1000) - 3600;
                        StaticVariables.exchanges[Settings.exchangeIndex].getCandles(index, 0, 1, hourTime);
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 60000);

        return rootView;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        handler.removeCallbacksAndMessages(null);
    }



    public void updatePastPricePoints(Candle candle) {
        long timeDiff = Math.abs(candle.time - weekTime);
        if(timeDiff <= 60) {
            price7d = candle.close;
        }
        timeDiff = Math.abs(candle.time - hourTime);
        if(timeDiff <= 60) {
            price1h = candle.close;
        }
    }



    public void updateStats() {
        float price = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).price;

        float compPrice = price / StaticVariables.exchanges[Settings.exchangeIndex].coins.get(0).price;
        priceTo.setText(Exchange.coinString(compPrice, 0, true, false));

        String volumeStr = String.valueOf(StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).volume1d);
        String symbolStr = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).symbol;
        volume1d.setText(volumeStr + " " + symbolStr);

        float change1hF = (price - price1h) * 100f / price1h;
        String changeStr = String.format("%.2f", change1hF) + "%";
        String arrowPath = "green_arrow";
        if(change1hF < 0) {
            changeStr = changeStr.substring(1, changeStr.length());
            arrowPath = "red_arrow";
        }
        change1h.setText(changeStr);
        Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                arrowPath, "drawable", getContext().getPackageName()));
        arrow1h.setImageDrawable(drawable);
        if(Settings.daily == 0) {
            String absString = Exchange.changeString(price, price - price1h);
            if(price - price1h >= 0) {
                abs1h.setText("+ " + absString);
            }

            else {
                abs1h.setText("- " + absString.substring(1, absString.length()));
            }
        }

        else {
            abs1h.setText("");
        }


        float change1dF = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).change1d;
        changeStr = String.format("%.2f", change1dF) + "%";
        arrowPath = "green_arrow";
        if(change1dF < 0) {
            changeStr = changeStr.substring(1, changeStr.length());
            arrowPath = "red_arrow";
        }
        change1d.setText(changeStr);
        drawable = getResources().getDrawable(getResources().getIdentifier(
                arrowPath, "drawable", getContext().getPackageName()));
        arrow1d.setImageDrawable(drawable);
        if(Settings.daily == 0) {
            float abs1dVal = price * change1dF / (change1dF + 100);
            String absString = Exchange.changeString(price, abs1dVal);
            if(abs1dVal >= 0) {
                abs1d.setText("+ " + absString);
            }

            else {
                abs1d.setText("- " + absString.substring(1, absString.length()));
            }
        }

        else {
            abs1d.setText("");
        }


        float change7dF = (price - price7d) * 100f / price7d;
        changeStr = String.format("%.2f", change7dF) + "%";
        arrowPath = "green_arrow";
        if(change7dF < 0) {
            changeStr = changeStr.substring(1, changeStr.length());
            arrowPath = "red_arrow";
        }
        change7d.setText(changeStr);
        drawable = getResources().getDrawable(getResources().getIdentifier(
                arrowPath, "drawable", getContext().getPackageName()));
        arrow7d.setImageDrawable(drawable);
        if(Settings.daily == 0) {
            String absString = Exchange.changeString(price, price - price7d);
            if(price - price7d >= 0) {
                abs7d.setText("+ " + absString);
            }

            else {
                abs7d.setText("- " + absString.substring(1, absString.length()));
            }
        }

        else {
            abs7d.setText("");
        }
    }
}
