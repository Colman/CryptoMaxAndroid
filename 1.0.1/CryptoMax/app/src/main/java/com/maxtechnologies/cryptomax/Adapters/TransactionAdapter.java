package com.maxtechnologies.cryptomax.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Main.MainFragments.ViewTransactionFragment;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Colman on 09/12/2017.
 */

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private FragmentActivity activity;
    private Wallet wallet;


    public TransactionAdapter(FragmentActivity activity, Wallet wallet){
        this.activity = activity;
        this.wallet = wallet;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View transactionView = inflater.inflate(R.layout.transaction_entry, parent, false);


        return new ViewHolder(transactionView, this, wallet);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction tx = wallet.transactions.get(position);
        String amount = Exchange.coinString(tx.amount, Exchange.findIndex(wallet.exchangeSymbol), true, true);
        if(tx.toAddress.equals(wallet.address)) {
            holder.amount.setText("+ " + amount);
            holder.amount.setTextColor(Color.parseColor("#008000"));
            holder.fromTo.setText("from");
            if(tx.fromAddress.length() > 12) {
                holder.address.setText(tx.fromAddress.substring(0, 12) + "...");
            }
            else {
                String message = activity.getResources().getString(R.string.blank_string, tx.fromAddress);
                holder.address.setTypeface(null, Typeface.ITALIC);
                holder.address.setText(message);
            }
        }

        else {
            holder.amount.setText("- " + amount);
            holder.amount.setTextColor(Color.parseColor("#CC0000"));
            holder.fromTo.setText("to");
            if(tx.toAddress.length() > 12) {
                holder.address.setText(tx.toAddress.substring(0, 12) + "...");
            }
            else {
                String message = activity.getResources().getString(R.string.blank_string, tx.toAddress);
                holder.address.setTypeface(null, Typeface.ITALIC);
                holder.address.setText(message);
            }
        }


        if(tx.timeMined == null) {
            holder.date.setText(R.string.pending);
            holder.date.setTypeface(null, Typeface.ITALIC);
        }

        else {
            Date curDate = new Date();
            long monthsBetween = (curDate.getTime() - tx.timeMined.getTime()) / (1000 * 60 * 60 * 24 * 365);
            long daysBetween = (curDate.getTime() - tx.timeMined.getTime()) / (1000 * 60 * 60 * 24);
            long hoursBetween = (curDate.getTime() - tx.timeMined.getTime()) / (1000 * 60 * 60);
            if (monthsBetween > 12) {
                String date = new SimpleDateFormat("YYYY", Locale.US).format(tx.timeMined);
                holder.date.setText(date);
            } else if (daysBetween > 7) {
                String date = new SimpleDateFormat("MMM dd", Locale.US).format(tx.timeMined);
                holder.date.setText(date);
            } else if (hoursBetween > 24) {
                String date = new SimpleDateFormat("EEE", Locale.US).format(tx.timeMined);
                holder.date.setText(date);
            } else {
                SimpleDateFormat format;
                if (Settings.times == 0) {
                    format = new SimpleDateFormat("h:mm a", Locale.US);
                } else {
                    format = new SimpleDateFormat("H:mm", Locale.US);
                }

                String date = format.format(tx.timeMined);
                holder.date.setText(date);
            }
        }
    }


    @Override
    public int getItemCount() {
        return wallet.transactions.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Wallet wallet;
        private TextView amount;
        private TextView fromTo;
        private TextView address;
        private TextView date;

        private TransactionAdapter adapter;

        public ViewHolder(View itemView, TransactionAdapter adapter, Wallet wallet) {
            super(itemView);
            amount = itemView.findViewById(R.id.amount);
            fromTo = itemView.findViewById(R.id.from_to);
            address = itemView.findViewById(R.id.address);
            date = itemView.findViewById(R.id.date);
            this.adapter = adapter;
            this.wallet = wallet;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();

            FragmentManager fragmentManager = adapter.activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ViewTransactionFragment fragment = ViewTransactionFragment.newInstance(wallet, position);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.content, fragment);
            fragmentTransaction.commit();
        }
    }
}
