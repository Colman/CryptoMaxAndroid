package com.maxtechnologies.cryptomax.ChartActivites;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Adapters.CustomSpinnerAdapter;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Objects.Book;
import com.maxtechnologies.cryptomax.Objects.Chart;
import com.maxtechnologies.cryptomax.Objects.Coin;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Callbacks.NetworkCallbacks;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.listener.OnDrawListener;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.ViewPortHandler;


import java.util.ArrayList;
import java.util.Arrays;


public class ChartActivity extends AppCompatActivity implements NetworkCallbacks {

    //Index declaration
    public int index;

    //Book declarations
    private int maxLines;

    //UI declarations
    private ConstraintLayout titleLayout;
    private ImageButton backButton;
    private ImageButton followButton;
    private ImageButton newsButton;
    private Spinner widthSpinner;
    private ArrayAdapter<String> adapter;
    private ImageButton settingsButton;
    private TextView exchangeName;
    private RelativeLayout chartLayout;
    private TextView pair;
    private Chart chart;
    private ProgressBar progress;
    private boolean resetAspect;

    //Fragments
    private boolean settingsShowing;
    private ChartSettingsFragment settingsFragment;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private BookFragment bookFragment;
    private StatsFragment statsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Exchange.activity = this;
        Exchange.callback = this;


        //Un-bundle the exchange and index objects
        Bundle bundle = getIntent().getExtras();
        index = bundle.getInt("INDEX");
        final Coin coin = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index);

        //Book definition
        maxLines = 25;

        //UI definitions
        titleLayout = (ConstraintLayout) findViewById(R.id.title_layout);
        backButton = (ImageButton) findViewById(R.id.back_button);
        followButton = (ImageButton) findViewById(R.id.follow_button);
        newsButton = (ImageButton) findViewById(R.id.news_button);
        widthSpinner = (Spinner) findViewById(R.id.width_spinner);
        final ArrayList<String> widthStrings = new ArrayList<>(Arrays.asList(Exchange.chartWidths));
        adapter = new CustomSpinnerAdapter(this, widthStrings);
        widthSpinner.setAdapter(adapter);
        widthSpinner.setSelection(3);
        settingsButton = (ImageButton) findViewById(R.id.settings_button);
        exchangeName = (TextView) findViewById(R.id.exchange);
        chartLayout = (RelativeLayout) findViewById(R.id.chart_layout);
        pair = (TextView) findViewById(R.id.pair);
        chart = (Chart) findViewById(R.id.chart);
        progress = (ProgressBar) findViewById(R.id.progress);
        resetAspect = false;
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);


        //Set the UI values
        if(Settings.following.contains(coin.symbol)) {
            followButton.setImageDrawable(getResources().getDrawable(R.drawable.follow_on));
        }
        else {
            followButton.setImageDrawable(getResources().getDrawable(R.drawable.follow_off));
        }
        exchangeName.setText(StaticVariables.exchanges[Settings.exchangeIndex].name);
        pair.setText(StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).symbol + "/" + Exchange.fiats[Settings.currency].symbol);


        //Setup the draw listener for the chart
        chart.setOnDrawListener(new OnDrawListener() {
            @Override
            public void onEntryAdded(Entry entry) {
                //Do nothing
            }

            @Override
            public void onEntryMoved(Entry entry) {
                //Do nothing
            }

            @Override
            public void onDrawFinished(DataSet<?> dataSet) {
                progress.setVisibility(View.INVISIBLE);
                if(resetAspect) {
                    chart.setAspect(false);
                    resetAspect = false;
                }
            }
        });


        //Setup the chart
        chart.initChart();


        //Start the network callbacks
        ArrayList<Integer> chan = new ArrayList<>();
        chan.add(0);
        chan.add(index);
        StaticVariables.exchanges[Settings.exchangeIndex].subTickers(chan);


        //Setup the listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        //Setup the listener for the follow button
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Coin coin = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index);
                    String message;
                    Drawable drawable;
                    if(Settings.following.contains(coin.symbol)) {
                        Settings.following.remove(coin.symbol);
                        drawable = getResources().getDrawable(getResources().getIdentifier(
                                "follow_off", "drawable", getPackageName()));
                        message = getResources().getString(R.string.unfollow_msg);
                    }

                    else {
                        Settings.following.add(coin.symbol);
                        drawable = getResources().getDrawable(getResources().getIdentifier(
                                "follow_on", "drawable", getPackageName()));
                        message = getResources().getString(R.string.follow_msg);
                    }

                    Toast newToast = Toast.makeText(ChartActivity.this,
                            message + " " + coin.symbol,
                            Toast.LENGTH_SHORT);
                    newToast.show();
                    followButton.setImageDrawable(drawable);
                }

                catch (Exception e) {
                    //Do nothing
                }

                Settings.saveSettings(ChartActivity.this);
            }
        });


        //Setup the listener for the news button
        newsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChartActivity.this, NewsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("COIN", coin);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        //Setup the listener for the width spinner
        widthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                progress.setVisibility(View.VISIBLE);
                chart.clear();
                chart.widthSec = Exchange.widthToSec(Exchange.chartWidths[i]);
                StaticVariables.exchanges[Settings.exchangeIndex].getCandles(index, i, 500, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the settings button
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!settingsShowing) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    settingsFragment = ChartSettingsFragment.newInstance();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.settings, settingsFragment, ChartSettingsFragment.class.toString());
                    fragmentTransaction.commit();
                    settingsShowing = true;
                }

                else {
                    settingsFragment.saveSettings();
                    chart.setData(false);
                    onBackPressed();
                    settingsShowing = false;
                }
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();

        StaticVariables.exchanges[Settings.exchangeIndex].subBook(index, maxLines);
    }



    @Override
    public void onPause() {
        super.onPause();

        StaticVariables.exchanges[Settings.exchangeIndex].unSubBook();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        chart.handler.removeCallbacksAndMessages(null);
    }



    @Override
    public void coinsCallback() {
        //Do nothing
    }



    @Override
    public void candlesCallback(final ArrayList<Candle> candleEntries, final ArrayList<Float> volumeEntries) {
        ChartActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(candleEntries.size() == 1) {
                    statsFragment.updatePastPricePoints(candleEntries.get(0));
                }

                else {
                    chart.setCandles(candleEntries, volumeEntries);
                }

                statsFragment.updateStats();
            }
        });
    }



    @Override
    public void tickersCallback() {
        ChartActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                float price = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).price;
                chart.updateData(price);
                bookFragment.updateTicker(price);
                statsFragment.updateStats();
            }
        });
    }



    @Override
    public void bookCallback(final Book book) {
        ChartActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bookFragment.updateBook(book);
            }
        });
    }



    private class CustomPagerAdapter extends FragmentPagerAdapter {
        public CustomPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    bookFragment = BookFragment.newInstance(index);
                    return bookFragment;
                case 1:
                    statsFragment = StatsFragment.newInstance(index);
                    return statsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }



    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LinearLayout.LayoutParams chartParams = (LinearLayout.LayoutParams) chartLayout.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            chartParams.weight = 0;
            chartLayout.setLayoutParams(chartParams);
            if(settingsShowing) {
                onBackPressed();
                settingsShowing = false;
            }
            titleLayout.setVisibility(View.GONE);
        }

        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            chartParams.weight = 50;
            chartLayout.setLayoutParams(chartParams);
            titleLayout.setVisibility(View.VISIBLE);
        }

        ViewPortHandler viewPortHandler = chart.getViewPortHandler();
        MPPointD topLeft = chart.getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), YAxis.AxisDependency.RIGHT);
        MPPointD bottomRight = chart.getValuesByTouchPoint(viewPortHandler.contentRight(), viewPortHandler.contentBottom(), YAxis.AxisDependency.RIGHT);
        chart.centerX = (float) (topLeft.x + bottomRight.x) / 2;
        chart.centerY = (float) (topLeft.y + bottomRight.y) / 2;
        chart.stopScrolling = true;

        chart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                resetAspect = true;
                chart.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }
}
