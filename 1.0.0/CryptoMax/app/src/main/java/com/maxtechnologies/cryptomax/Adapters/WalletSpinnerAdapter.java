package com.maxtechnologies.cryptomax.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import java.util.ArrayList;

/**
 * Created by Colman on 02/06/2018.
 */

public class WalletSpinnerAdapter extends ArrayAdapter<Integer> {
    private Context context;
    private ArrayList<Integer> indices;

    public WalletSpinnerAdapter(Context context, ArrayList<Integer> indices) {
        super(context, R.layout.wallet_spinner, indices);
        this.context = context;
        this.indices = indices;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wallet_spinner, parent, false);
        return getEntry(position, view);
    }



    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wallet_spinner_drop_down, parent, false);
        return getEntry(position, view);
    }



    private View getEntry(int position, View view) {
        Wallet wallet = CryptoMaxApi.getWallet(indices.get(position));


        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(wallet.name);


        ImageView logo = (ImageView) view.findViewById(R.id.logo);
        Drawable drawable;
        String symbol = Exchange.translateToSymbol(wallet.exchangeSymbol);
        if(Settings.theme == 0) {
            try {
                drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                        symbol.toLowerCase(), "drawable", context.getPackageName()));
            }

            catch(Exception e) {
                drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                        "logo", "drawable", context.getPackageName()));
            }
        }

        else {
            try {
                drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                        symbol.toLowerCase() + "_d", "drawable", context.getPackageName()));
            }

            catch (Exception e) {
                try {
                    drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                            symbol.toLowerCase(), "drawable", context.getPackageName()));
                }

                catch (Exception e2) {
                    drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                            "logo_d", "drawable", context.getPackageName()));
                }
            }
        }
        logo.setImageDrawable(drawable);



        return view;
    }
}
