package com.maxtechnologies.cryptomax.ui.drawer.market;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.exchange.book.Book;
import com.maxtechnologies.cryptomax.exchange.candle.Candle;
import com.maxtechnologies.cryptomax.exchange.asset.Asset;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.misc.CustomSpinnerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;


public class MarketsFragment extends Fragment implements NetworkCallbacks {

    //UI declarations
    private boolean following;
    private LinearLayout spinnerLayout;
    private Spinner exchangeSpinner;
    private ArrayAdapter<String> exchangeAdapter;
    private Spinner sortSpinner;
    private ArrayAdapter<String> sortAdapter;
    private Handler refreshHandler;
    private SwipeRefreshLayout coinRefresh;
    private RecyclerView coinList;
    private RecyclerView.OnScrollListener listener;
    private CoinAdapter adapter;
    private LinearLayoutManager manager;

    //List scrolling declarations
    private int listenerPadding;
    private long previousScrollTime;
    private int currentY;
    private int entryHeight;


    public static MarketsFragment newInstance() {
        MarketsFragment fragment = new MarketsFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_markets, container, false);
        getActivity().setTitle(R.string.markets);
        setHasOptionsMenu(true);


        //UI definitions
        following = false;
        spinnerLayout = (LinearLayout) rootView.findViewById(R.id.spinner_layout);
        if(Settings.theme == 0) {
            spinnerLayout.setBackgroundResource(android.R.color.white);
        }
        else {
            spinnerLayout.setBackgroundResource(R.color.colorDraculaPrimaryDark);
        }
        exchangeSpinner = (Spinner) rootView.findViewById(R.id.exchange_spinner);
        ArrayList<String> exchangeNames = new ArrayList<>();
        for(int i = 0; i < StaticVariables.exchanges.length; i++) {
            exchangeNames.add(StaticVariables.exchanges[i].name);
        }
        exchangeAdapter = new CustomSpinnerAdapter(getActivity(), exchangeNames);
        exchangeSpinner.setAdapter(exchangeAdapter);
        exchangeSpinner.setSelection(Settings.exchangeIndex);

        sortSpinner = (Spinner) rootView.findViewById(R.id.sort_spinner);
        ArrayList<String> sortTypes = new ArrayList<>();
        sortTypes.add("Market Cap");
        sortTypes.add("Market Cap");
        sortTypes.add("Daily Change");
        sortTypes.add("Daily Change");
        sortAdapter = new CustomSpinnerAdapter(getActivity(), sortTypes);
        sortSpinner.setAdapter(sortAdapter);
        sortSpinner.setSelection(Settings.sortType);

        refreshHandler = new Handler();
        coinRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.coin_refresh);
        coinList = (RecyclerView) rootView.findViewById(R.id.coin_list);
        coinList.setHasFixedSize(false);
        adapter = new CoinAdapter(getActivity(), StaticVariables.exchanges[Settings.exchangeIndex].coins);
        coinList.setAdapter(adapter);
        manager = new LinearLayoutManager(getActivity());
        coinList.setLayoutManager(manager);
        DividerItemDecoration decoration;
        if(Settings.theme == 0) {
            decoration = new DividerItemDecoration(
                    coinList.getContext(),
                    manager.getOrientation()
            );
        }

        else {
            decoration = new DividerItemDecoration(
                    coinList.getContext(),
                    manager.getOrientation()
            );
            Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                    "dracula_divider", "drawable", getActivity().getPackageName()));
            decoration.setDrawable(drawable);
        }
        coinList.addItemDecoration(decoration);
        coinList.setItemAnimator(new DefaultItemAnimator());

        //List scrolling definitions
        listenerPadding = 1;
        previousScrollTime = System.currentTimeMillis();
        currentY = 0;


        //Setup the listener for the exchange spinner
        exchangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != Settings.exchangeIndex) {
                    StaticVariables.exchanges[Settings.exchangeIndex].close();
                    Settings.exchangeIndex = i;
                    if(StaticVariables.exchanges[Settings.exchangeIndex].coins == null) {
                        StaticVariables.exchanges[Settings.exchangeIndex].findCoins();
                    }
                    else {
                        coinsCallback();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the sort spinner
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(manager.findLastVisibleItemPosition() != -1) {
                    sortAndFilter();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the list refresher
        coinRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        coinRefresh.setRefreshing(false);
                    }
                }, 300);
            }
        });


        //Setup the scroll listener for the list of coins
        listener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int offset = coinList.computeVerticalScrollOffset();
                    int dy = offset - currentY;
                    updateListFrame(dy, true);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateListFrame(dy, false);
            }
        };
        coinList.addOnScrollListener(listener);

        return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();

        sortAndFilter();
        if(coinList.getChildAt(0) != null) {
            listener.onScrolled(coinList, 0, 0);
        }
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_market_summary, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.all_button:
                following = false;
                sortAndFilter();
                return true;

            case R.id.following_button:
                following = true;
                sortAndFilter();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void coinsCallback() {
        adapter = new CoinAdapter(getActivity(), StaticVariables.exchanges[Settings.exchangeIndex].coins);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                coinList.setAdapter(adapter);
            }
        });

        int lastIndex = manager.findLastVisibleItemPosition();
        int len = StaticVariables.exchanges[Settings.exchangeIndex].coins.size() - 1;
        if(lastIndex + listenerPadding > len) {
            lastIndex = len;
        }

        ArrayList<Integer> indexes = new ArrayList<>();
        for(int j = 0; j <= lastIndex; j++) {
            indexes.add(j);
        }

        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            CryptoMaxApi.getWallet(i).exchangeSymbol = Exchange.translateToExchangeSymbol(CryptoMaxApi.getWallet(i).exchangeSymbol);
            int index = Exchange.findIndex(CryptoMaxApi.getWallet(i).exchangeSymbol);
            if(!indexes.contains(index)) {
                indexes.add(index);
            }
        }

        Settings.saveSettings(getContext());
        CryptoMaxApi.saveWallets(getContext(), false);
        StaticVariables.exchanges[Settings.exchangeIndex].subTickers(indexes);
    }



    @Override
    public void candlesCallback(ArrayList<Candle> entries, ArrayList<Float> volume) {
        //Do nothing
    }



    @Override
    public void tickersCallback() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Asset> coins = StaticVariables.exchanges[Settings.exchangeIndex].coins;
                int firstVisible = manager.findFirstVisibleItemPosition();
                for(int i = 0; i < coinList.getChildCount(); i++) {
                    Asset coin = coins.get(Exchange.findIndex(adapter.coinArrayList.get(firstVisible + i).exchangeSymbol));
                    TextView priceView = (TextView) coinList.getChildAt(i).findViewById(R.id.price);

                    float oldPrice = Float.valueOf(priceView.getText().toString().split(" ")[0]);
                    String newPriceStr = Exchange.fiatString(coin.price, false, false, false);
                    float newPrice = Float.valueOf(newPriceStr);

                    priceView.setText(Exchange.fiatString(coin.price, false, true, false));
                    if(Settings.priceFlash) {
                        if(newPrice < oldPrice && oldPrice != 0) {
                            priceAnimation(priceView, false);
                        }
                        else if(newPrice > oldPrice && oldPrice != 0) {
                            priceAnimation(priceView, true);
                        }
                    }

                    float change = coin.change1d;
                    String changeStr = String.format(Locale.US, "%.2f", change) + "%";
                    boolean isGreen = true;
                    if(change < 0) {
                        changeStr = changeStr.substring(1, changeStr.length());
                        isGreen = false;
                    }
                    TextView changeView = (TextView) coinList.getChildAt(i).findViewById(R.id.day_change);
                    changeView.setText(changeStr);

                    if(isGreen) {
                        coinList.getChildAt(i).findViewById(R.id.green_arrow).setVisibility(View.VISIBLE);
                        coinList.getChildAt(i).findViewById(R.id.red_arrow).setVisibility(View.INVISIBLE);
                    }
                    else {
                        coinList.getChildAt(i).findViewById(R.id.green_arrow).setVisibility(View.INVISIBLE);
                        coinList.getChildAt(i).findViewById(R.id.red_arrow).setVisibility(View.VISIBLE);
                    }

                    TextView absView = (TextView) coinList.getChildAt(i).findViewById(R.id.abs_change);
                    if(Settings.daily == 0) {
                        float absVal = newPrice * change / (change + 100);
                        String absString = Exchange.changeString(newPrice, absVal);
                        if(absVal >= 0) {
                            absView.setText("+ " + absString);
                        }

                        else {
                            absView.setText("- " + absString.substring(1, absString.length()));
                        }
                    }

                    else {
                        absView.setText("");
                    }
                }
            }
        });
    }



    @Override
    public void bookCallback(Book book) {
        //Do nothing
    }



    private void sortAndFilter() {
        adapter = new CoinAdapter(getActivity(), StaticVariables.exchanges[Settings.exchangeIndex].coins);

        if(following) {
            ArrayList<Asset> tempList = new ArrayList<>();
            for(Asset coin : adapter.coinArrayList) {
                if(Settings.following.contains(coin.symbol)) {
                    tempList.add(coin);
                }
            }
            adapter.coinArrayList = tempList;
        }

        int index = sortSpinner.getSelectedItemPosition();
        if(index == 1) {
            Collections.reverse(adapter.coinArrayList);
        }

        else if(index == 2) {
            Asset.sortByChange(adapter.coinArrayList, true);
        }

        else if(index == 3) {
            Asset.sortByChange(adapter.coinArrayList, false);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                coinList.setAdapter(adapter);
            }
        });
    }



    private void updateListFrame(int dy, boolean ignoreSpeed) {
        int firstVisible = manager.findFirstVisibleItemPosition();
        int lastVisible = manager.findLastVisibleItemPosition();
        currentY = currentY + dy;
        if(entryHeight == 0) {
            entryHeight = coinList.getChildAt(0).getHeight();
        }

        long currTime = System.currentTimeMillis();
        float timeSecs = (currTime - previousScrollTime) / 1000f;
        float entriesMoved = Float.valueOf(dy) / Float.valueOf(entryHeight);

        float speed = 1;
        if(!ignoreSpeed) {
            speed = Math.abs(entriesMoved / timeSecs);
        }

        if (speed < 5) {
            int startIndex = firstVisible - listenerPadding;
            if(startIndex < 0) {
                startIndex = 0;
            }
            int endIndex = lastVisible + listenerPadding;
            int len = adapter.coinArrayList.size() - 1;
            if(endIndex > len) {
                endIndex = len;
            }

            ArrayList<Integer> indexes = new ArrayList<>();
            for(int i = startIndex; i <= endIndex; i++) {
                indexes.add(Exchange.findIndex(adapter.coinArrayList.get(i).exchangeSymbol));
            }

            for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
                int index = Exchange.findIndex(CryptoMaxApi.getWallet(i).exchangeSymbol);
                if(!indexes.contains(index)) {
                    indexes.add(index);
                }
            }

            StaticVariables.exchanges[Settings.exchangeIndex].subTickers(indexes);
        }
        previousScrollTime = currTime;
    }



    private void priceAnimation(final View v, boolean isUp) {
        int colorStart = Color.parseColor("#00FF0000");
        int colorEnd = Color.parseColor("#FFFF0000");
        if(isUp) {
            colorStart = Color.parseColor("#0000FF00");
            colorEnd = Color.parseColor("#FF00FF00");
        }

        ValueAnimator colorAnim = ObjectAnimator.ofInt(v, "backgroundColor", colorStart, colorEnd);
        colorAnim.setDuration(300);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.setRepeatCount(1);
        colorAnim.setRepeatMode(ValueAnimator.REVERSE);
        colorAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                //Do nothing
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Do nothing
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                //Do nothing
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                //Do nothing
            }
        });
        colorAnim.start();
    }
}
