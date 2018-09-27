package com.maxtechnologies.cryptomax.ui.drawer.market;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.exchange.asset.Asset;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.ui.chart.ChartActivity;

import java.util.ArrayList;

/**
 * Created by Colman on 09/12/2017.
 */

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.ViewHolder> {
    private Context context;
    public ArrayList<Asset> coinArrayList;


    public CoinAdapter(Context context, ArrayList<Asset> coinArrayList) {
        this.context = context;
        this.coinArrayList = new ArrayList<>();
        for(Asset coin : coinArrayList) {
            try {
                this.coinArrayList.add(coin.clone());
            }
            catch (Exception e) {
                //Do nothing
            }
        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View coinView = inflater.inflate(R.layout.coin_entry, parent, false);
        ViewHolder holder = new ViewHolder(coinView, this);


        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Asset coin = coinArrayList.get(position);

        //Set the logo of the coin
        Drawable drawable;
        if(Settings.theme == 0) {
            try {
                drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                        coin.symbol.toLowerCase(), "drawable", context.getPackageName()));
            }

            catch (Exception e) {
                drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                        "logo", "drawable", context.getPackageName()));
            }
        }

        else {
            try {
                drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                        coin.symbol.toLowerCase() + "_d", "drawable", context.getPackageName()));
            }

            catch (Exception e) {
                try {
                    drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                            coin.symbol.toLowerCase(), "drawable", context.getPackageName()));
                }

                catch (Exception e2) {
                    drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                            "logo_d", "drawable", context.getPackageName()));
                }
            }
        }
        holder.logo.setImageDrawable(drawable);

        //Set the name of the coin
        holder.name.setText(coin.name);

        //Set the market cap of the coin
        holder.cap.setText(coin.marketCapString());

        //Set the following status of the coin
        if(Settings.following.contains(coin.symbol)) {
            holder.follow.setVisibility(View.VISIBLE);
        }

        else {
            holder.follow.setVisibility(View.INVISIBLE);
        }

        //Set the price of the coin
        holder.price.setText(Exchange.fiatString(coin.price, false, true, false));

        //Set the daily change of the coin
        float change = coin.change1d;
        String changeStr = String.format("%.2f", change) + "%";
        boolean isGreen = true;
        if(change < 0) {
            changeStr = changeStr.substring(1, changeStr.length());
            isGreen = false;
        }
        holder.change.setText(changeStr);

        //Set the arrow for the daily change
        if(isGreen) {
            holder.greenArrow.setVisibility(View.VISIBLE);
            holder.redArrow.setVisibility(View.INVISIBLE);
        }
        else {
            holder.greenArrow.setVisibility(View.INVISIBLE);
            holder.redArrow.setVisibility(View.VISIBLE);
        }

        //Set the absolute change of the coin
        if(Settings.daily == 0) {
            float absVal = (change / 100) * coin.price;
            String absString = Exchange.changeString(coin.price, absVal);
            if(absVal >= 0) {
                holder.absChange.setText("+ " + absString);
            }

            else {
                holder.absChange.setText("- " + absString.substring(1, absString.length()));
            }
        }

        else {
            holder.absChange.setText("");
        }


        holder.itemView.setTag(coin);
    }

    @Override
    public int getItemCount() {
        return coinArrayList.size();
    }




    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CoinAdapter adapter;
        public ImageView logo;
        public TextView name;
        public TextView cap;
        public ImageView follow;
        public TextView price;
        public ImageView greenArrow;
        public ImageView redArrow;
        public TextView change;
        public TextView absChange;

        public ViewHolder(View itemView, CoinAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            logo = (ImageView) itemView.findViewById(R.id.logo);
            name = (TextView) itemView.findViewById(R.id.name);
            cap = (TextView) itemView.findViewById(R.id.market_cap);
            follow = (ImageView) itemView.findViewById(R.id.follow);
            price = (TextView) itemView.findViewById(R.id.price);
            greenArrow = (ImageView) itemView.findViewById(R.id.green_arrow);
            redArrow = (ImageView) itemView.findViewById(R.id.red_arrow);
            change = (TextView) itemView.findViewById(R.id.day_change);
            absChange = (TextView) itemView.findViewById(R.id.abs_change);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Context context = v.getContext();
            Intent intent = new Intent(v.getContext(), ChartActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("INDEX", Exchange.findIndex(adapter.coinArrayList.get(getAdapterPosition()).exchangeSymbol));
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}