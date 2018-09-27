package com.maxtechnologies.cryptomax.Main.WalletFragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Adapters.TransactionAdapter;
import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Controllers.AlertController;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Main.MainActivity;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Colman on 18/03/2018.
 */

public class WalletTransactionsFragment extends Fragment {

    //Timer declarations
    private Timer timer;
    private TimerTask timerTask;

    //Index declaration
    private int index;

    //UI declarations
    private SwipeRefreshLayout transactionRefresh;
    private RecyclerView transactionList;
    private TransactionAdapter adapter;
    private LinearLayoutManager manager;
    private ProgressBar progress;
    private TextView noTransactions;


    public static WalletTransactionsFragment newInstance(int index) {
        WalletTransactionsFragment fragment = new WalletTransactionsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("INDEX", index);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_wallet_transactions, container, false);


        //Timer definitions
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                getTransactions();
            }
        };

        //Index definition
        index = getArguments().getInt("INDEX");

        //UI definitions
        transactionRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.transaction_refresh);
        transactionList = (RecyclerView) rootView.findViewById(R.id.transaction_list);
        transactionList.setHasFixedSize(false);
        manager = new LinearLayoutManager(getActivity());
        transactionList.setLayoutManager(manager);
        DividerItemDecoration decoration;
        if(Settings.theme == 0) {
            decoration = new DividerItemDecoration(
                    transactionList.getContext(),
                    manager.getOrientation()
            );
        }
        else {
            decoration = new DividerItemDecoration(
                    transactionList.getContext(),
                    manager.getOrientation()
            );
            Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                    "dracula_divider", "drawable", getActivity().getPackageName()));
            decoration.setDrawable(drawable);
        }
        transactionList.addItemDecoration(decoration);
        transactionList.setItemAnimator(new DefaultItemAnimator());
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        noTransactions = (TextView) rootView.findViewById(R.id.no_transactions);


        //Setup the listener for the list refresher
        transactionRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTransactions();
            }
        });


        return rootView;
    }



    private void getTransactions() {
        CryptoMaxApi.getWallet(index).getTransactions(new BasicCallback() {
            @Override
            public void onFailure(String reason) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        transactionRefresh.setRefreshing(false);
                    }
                });

                Log.e("Network", reason);
                AlertController.networkError(getActivity(), true);
            }

            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Wallet wallet = CryptoMaxApi.getWallet(index);
                        ArrayList<Transaction> txs = wallet.transactions;
                        ArrayList<Transaction> pending = CryptoMaxApi.pendingTxs;
                        ArrayList<Transaction> newPending = new ArrayList<>();

                        for(int i = 0; i < pending.size(); i++) {
                            boolean found = false;
                            for(int j = 0; j < txs.size(); j++) {
                                if(txs.get(j).hash.equals(pending.get(i).hash)) {
                                    int coinIndex = Exchange.findIndex(wallet.exchangeSymbol);
                                    String amountStr = Exchange.coinString(txs.get(j).amount, coinIndex, true, true);
                                    String toastStr = getResources().getString(R.string.transaction_complete, amountStr);
                                    Toast newToast = Toast.makeText(getContext(),
                                            toastStr,
                                            Toast.LENGTH_LONG);
                                    newToast.show();

                                    found = true;
                                    break;
                                }
                            }

                            if(!found) {
                                newPending.add(pending.get(i));
                            }
                        }
                        CryptoMaxApi.pendingTxs = newPending;
                        for(int i = 0; i < newPending.size(); i++) {
                            Transaction transaction = newPending.get(i);
                            if(transaction.fromAddress.equals(wallet.address)) {
                                wallet.transactions.add(0, newPending.get(i));
                            }
                        }

                        updateTransactions();

                        if(newPending.size() == 0) {
                            timer.cancel();
                        }
                    }
                });
            }
        });
    }



    private void updateTransactions() {
        progress.setVisibility(View.INVISIBLE);
        Wallet wallet = CryptoMaxApi.getWallet(index);
        if(wallet.transactions.size() == 0) {
            noTransactions.setVisibility(View.VISIBLE);
        }
        adapter = new TransactionAdapter(getActivity(), wallet);
        transactionList.setAdapter(adapter);
        transactionRefresh.setRefreshing(false);
    }



    @Override
    public void onResume() {
        super.onResume();

        timer.schedule(timerTask, 0, 10000);
    }



    @Override
    public void onPause() {
        super.onPause();

        timer.cancel();
    }
}
