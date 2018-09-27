package com.maxtechnologies.cryptomax.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;

import java.util.ArrayList;

/**
 * Created by Colman on 25/07/2018.
 */

public class ArrowSpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> entries;

    public ArrowSpinnerAdapter(Context context, ArrayList<String> entries) {
        super(context, R.layout.arrow_spinner, entries);
        this.context = context;
        this.entries = entries;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.arrow_spinner, parent, false);

        TextView textView = view.findViewById(R.id.text);
        textView.setText(entries.get(position));

        ImageView arrow = view.findViewById(R.id.arrow);
        if (position % 2 == 1) {
            Drawable drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                    "sort_arrow_down", "drawable", context.getPackageName()));
            arrow.setImageDrawable(drawable);
        }

        if (Settings.theme == 1) {
            arrow.setColorFilter(Color.parseColor("#FFFFFF"));
        }

        return view;
    }



    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.arrow_spinner, parent, false);

        TextView textView = view.findViewById(R.id.text);
        textView.setText(entries.get(position));

        ImageView arrow = view.findViewById(R.id.arrow);
        if (position % 2 == 1) {
            Drawable drawable = context.getResources().getDrawable(context.getResources().getIdentifier(
                    "sort_arrow_down", "drawable", context.getPackageName()));
            arrow.setImageDrawable(drawable);
        }

        if (Settings.theme == 1) {
            arrow.setColorFilter(Color.parseColor("#FFFFFF"));
        }

        return view;
    }
}