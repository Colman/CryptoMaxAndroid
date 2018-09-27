package com.maxtechnologies.cryptomax.ui.drawer.wallet.misc;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.exchange.asset.Asset;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.view.ViewWalletFragment;
import com.maxtechnologies.cryptomax.wallets.Wallet;

import java.util.ArrayList;

/**
 * Created by Colman on 09/12/2017.
 */

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {
    private FragmentActivity activity;
    public boolean editMode;
    public ArrayList<Integer> selectedEntries;


    public WalletAdapter(FragmentActivity activity) {
        this.activity = activity;
        this.editMode = false;
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View walletView = inflater.inflate(R.layout.wallet_entry, parent, false);

        return new ViewHolder(walletView, this);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Wallet wallet = CryptoMaxApi.getWallet(position);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup itemGroup = (ViewGroup) holder.itemView;


        if(itemGroup.getChildCount() == 2) {
            itemGroup.removeViewAt(0);
        }

        if(editMode) {
            View editView = inflater.inflate(R.layout.wallet_entry_edit, itemGroup, false);
            itemGroup.addView(editView, 0);
        }

        else {
            View editView = inflater.inflate(R.layout.wallet_entry_logo, itemGroup, false);
            itemGroup.addView(editView, 0);

            //Set the logo of the wallet
            holder.logo = (ImageView) itemGroup.findViewById(R.id.logo);
            Drawable drawable;
            String symbol = Exchange.translateToSymbol(wallet.exchangeSymbol).toLowerCase();
            if(Settings.theme == 0) {
                try {
                    drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                            symbol, "drawable", activity.getPackageName()));
                }

                catch (Exception e) {
                    drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                            "logo", "drawable", activity.getPackageName()));
                }
            }

            else {
                try {
                    drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                            symbol + "_d", "drawable", activity.getPackageName()));
                }

                catch (Exception e) {
                    try {
                        drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                                symbol, "drawable", activity.getPackageName()));
                    }

                    catch (Exception e2) {
                        drawable = activity.getResources().getDrawable(activity.getResources().getIdentifier(
                                "logo_d", "drawable", activity.getPackageName()));
                    }
                }
            }
            holder.logo.setImageDrawable(drawable);

            //Set the name of the currency in the wallet
            holder.coinName = (TextView) itemGroup.findViewById(R.id.c_name);
            holder.coinName.setText(Exchange.translateToName(wallet.exchangeSymbol));
        }


        //Set the name of the wallet
        holder.name.setText(wallet.name);

        //Set the balance of the wallet
        int index = Exchange.findIndex(wallet.exchangeSymbol);
        if(wallet.balance >= 0) {
            holder.balance.setText(Exchange.coinString(wallet.balance, index, true, true));
            holder.balance.setTypeface(null, Typeface.NORMAL);

            //Set the value of the wallet
            ArrayList<Asset> coins = StaticVariables.exchanges[Settings.exchangeIndex].coins;
            float price = coins.get(Exchange.findIndex(wallet.exchangeSymbol)).price;
            holder.value.setText(Exchange.fiatString(wallet.balance * price, true, true, true));
        }

        else {
            holder.balance.setText(R.string.unknown_2);
            holder.balance.setTypeface(null, Typeface.ITALIC);

            holder.value.setText(R.string.unknown_2);
        }
    }



    @Override
    public int getItemCount() {
        return CryptoMaxApi.getWalletsSize();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View itemView;
        public ImageView logo;
        public TextView coinName;
        public TextView name;
        public TextView balance;
        public TextView value;
        private WalletAdapter adapter;

        public ViewHolder(View itemView, WalletAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            name = (TextView) itemView.findViewById(R.id.name);
            balance = (TextView) itemView.findViewById(R.id.balance);
            value = (TextView) itemView.findViewById(R.id.value);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(adapter.editMode) {
                RadioButton selectedButton = (RadioButton) v.findViewById(R.id.selected_button);
                int pos = getAdapterPosition();
                if(selectedButton.isChecked()) {
                    int index = adapter.selectedEntries.indexOf(pos);
                    adapter.selectedEntries.remove(index);
                    selectedButton.setChecked(false);
                }
                else {
                    adapter.selectedEntries.add(pos);
                    selectedButton.setChecked(true);
                }
            }
            else {
                FragmentManager fragmentManager = adapter.activity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ViewWalletFragment fragment = ViewWalletFragment.newInstance(getAdapterPosition());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();
            }
        }
    }
}