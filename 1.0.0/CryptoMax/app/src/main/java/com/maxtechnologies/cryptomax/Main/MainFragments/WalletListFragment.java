package com.maxtechnologies.cryptomax.Main.MainFragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Main.WalletFragments.AddWalletFragment1;
import com.maxtechnologies.cryptomax.Objects.Book;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Objects.Coin;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Callbacks.NetworkCallbacks;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.Other.WalletsActionCallback;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Adapters.WalletAdapter;

import java.util.ArrayList;


public class WalletListFragment extends Fragment implements NetworkCallbacks {

    //UI declarations
    public ActionMode actionMode;
    private ConstraintLayout noWalletsLayout;
    private ImageView noWalletsImage;
    private TextView noWalletsTitle;
    private ImageView noWalletsArrow;
    private SwipeRefreshLayout walletRefresh;
    private RecyclerView walletList;
    private FloatingActionButton addButton;
    public WalletAdapter adapter;
    private LinearLayoutManager manager;

    //List scrolling declarations
    private int listenerPadding;
    private long previousScrollTime;
    private int currentY;
    private int entryHeight;


    public static WalletListFragment newInstance() {
        WalletListFragment fragment = new WalletListFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_wallet_list, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.wallets);

        //UI definitions
        noWalletsLayout = (ConstraintLayout) rootView.findViewById(R.id.no_wallets_layout);
        noWalletsImage = (ImageView) rootView.findViewById(R.id.no_wallets_image);
        noWalletsTitle = (TextView) rootView.findViewById(R.id.no_wallets_title);
        noWalletsArrow = (ImageView) rootView.findViewById(R.id.no_wallets_arrow);
        walletRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.wallet_refresh);
        walletList = (RecyclerView) rootView.findViewById(R.id.wallets);
        walletList.setHasFixedSize(false);
        adapter = new WalletAdapter(getActivity());
        walletList.setAdapter(adapter);
        manager = new LinearLayoutManager(getActivity());
        walletList.setLayoutManager(manager);
        DividerItemDecoration decoration;
        if(Settings.theme == 0) {
            decoration = new DividerItemDecoration(
                    walletList.getContext(),
                    manager.getOrientation()
            );

            noWalletsImage.setColorFilter(Color.parseColor("#444444"));
            noWalletsTitle.setTextColor(Color.parseColor("#444444"));
            noWalletsArrow.setColorFilter(Color.parseColor("#444444"));
        }

        else {
            decoration = new DividerItemDecoration(
                    walletList.getContext(),
                    manager.getOrientation()
            );
            Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                    "dracula_divider", "drawable", getActivity().getPackageName()));
            decoration.setDrawable(drawable);

            noWalletsImage.setColorFilter(Color.parseColor("#99999D"));
            noWalletsTitle.setTextColor(Color.parseColor("#99999D"));
            noWalletsArrow.setColorFilter(Color.parseColor("#99999D"));
        }
        walletList.addItemDecoration(decoration);
        addButton = (FloatingActionButton) rootView.findViewById(R.id.add_button);


        //Set the UI visibility
        setOverlayVisibility();


        //List scrolling definitions
        listenerPadding = 1;
        previousScrollTime = System.currentTimeMillis();
        currentY = 0;


        //Setup the add button listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AddWalletFragment1 fragment = AddWalletFragment1.newInstance();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();
            }
        });


        //Un-sub tickers
        StaticVariables.exchanges[Settings.exchangeIndex].subTickers(new ArrayList<Integer>());


        //Setup the scrolling listener for the wallet list
        walletList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = manager.findFirstVisibleItemPosition();
                int lastVisible = manager.findLastVisibleItemPosition();
                currentY = currentY + dy;
                if(entryHeight == 0) {
                    entryHeight = recyclerView.getChildAt(0).getHeight();
                }

                long currTime = System.currentTimeMillis();
                float timeSecs = (currTime - previousScrollTime) / 1000f;
                float entriesMoved = Float.valueOf(dy) / Float.valueOf(entryHeight);
                float speed = Math.abs(entriesMoved / timeSecs);

                if (speed < 5) {
                    int startIndex = firstVisible - listenerPadding;
                    if(startIndex < 0) {
                        startIndex = 0;
                    }
                    int endIndex = lastVisible + listenerPadding;
                    int len = CryptoMaxApi.getWalletsSize() - 1;
                    if(endIndex > len) {
                        endIndex = len;
                    }

                    ArrayList<Integer> indexes = new ArrayList<>();
                    for (int i = startIndex; i <= endIndex; i++) {
                        int index = Exchange.findIndex(CryptoMaxApi.getWallet(i).exchangeSymbol);
                        if(!indexes.contains(index)) {
                            indexes.add(index);
                        }
                    }
                    StaticVariables.exchanges[Settings.exchangeIndex].subTickers(indexes);
                }
                previousScrollTime = currTime;
            }
        });


        //Setup the listener for the list refresher
        walletRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
                    CryptoMaxApi.getWallet(i).getBalance(new BasicCallback() {
                        @Override
                        public void onFailure(String reason) {
                            Log.e("Network", "Failed to get balance for reason: " + reason);
                        }

                        @Override
                        public void onSuccess() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    walletRefresh.setRefreshing(false);
                                }
                            });
                        }
                    });
                }
            }
        });


        return rootView;
    }



    @Override
    public void coinsCallback() {
        //Do nothing
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
                ArrayList<Coin> coins = StaticVariables.exchanges[Settings.exchangeIndex].coins;
                int firstVisible = manager.findFirstVisibleItemPosition();
                for(int i = 0; i < walletList.getChildCount(); i++) {
                    if(CryptoMaxApi.getWallet(i).balance != -1) {
                        TextView value = (TextView) walletList.getChildAt(firstVisible + i).findViewById(R.id.value);
                        float price = coins.get(Exchange.findIndex(CryptoMaxApi.getWallet(i).exchangeSymbol)).price;
                        value.setText(Exchange.fiatString(CryptoMaxApi.getWallet(i).balance * price, true, true, true));
                    }
                }
            }
        });
    }



    @Override
    public void bookCallback(Book book) {
        //Do nothing
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.edit_button:
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                WalletsActionCallback callback = new WalletsActionCallback(activity, this);
                actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(callback);
                adapter.editMode = true;
                adapter.selectedEntries = new ArrayList<>();
                adapter.notifyDataSetChanged();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void setOverlayVisibility() {
        if(CryptoMaxApi.getWalletsSize() == 0) {
            noWalletsLayout.setVisibility(View.VISIBLE);
            walletRefresh.setVisibility(View.INVISIBLE);
        }

        else {
            noWalletsLayout.setVisibility(View.INVISIBLE);
            walletRefresh.setVisibility(View.VISIBLE);
        }
    }
}
