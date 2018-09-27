package com.maxtechnologies.cryptomax.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.R;

import java.util.ArrayList;

/**
 * Created by Colman on 31/03/2018.
 */

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private ArrayList<String> entries;

    public CustomSpinnerAdapter(Context context, ArrayList<String> entries) {
        super(context, R.layout.basic_spinner, entries);
        this.context = context;
        this.entries = entries;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.basic_spinner, parent, false);

        TextView textView = view.findViewById(R.id.text);
        textView.setText(entries.get(position));

        return view;
    }



    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.basic_spinner_drop_down, parent, false);

        TextView textView = view.findViewById(R.id.text);
        textView.setText(entries.get(position));

        return view;
    }
}
