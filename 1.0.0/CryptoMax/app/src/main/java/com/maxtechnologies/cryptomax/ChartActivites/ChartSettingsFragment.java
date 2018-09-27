package com.maxtechnologies.cryptomax.ChartActivites;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;

/**
 * Created by Colman on 20/04/2018.
 */

public class ChartSettingsFragment extends Fragment {

    //UI declarations
    private CheckBox smaCheckbox;
    private EditText sma1;
    private EditText sma2;
    private EditText sma3;
    private CheckBox emaCheckbox;
    private EditText ema1;
    private EditText ema2;
    private EditText ema3;
    private CheckBox bolCheckbox;
    private EditText bol1;
    private EditText bol2;
    private CheckBox sarCheckbox;
    private EditText sar1;
    private EditText sar2;
    private CheckBox volumeCheckbox;


    public static ChartSettingsFragment newInstance() {
        ChartSettingsFragment fragment = new ChartSettingsFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_chart_settings, container, false);

        //UI definitions
        smaCheckbox = (CheckBox) rootView.findViewById(R.id.sma);
        sma1 = (EditText) rootView.findViewById(R.id.sma_1);
        sma2 = (EditText) rootView.findViewById(R.id.sma_2);
        sma3 = (EditText) rootView.findViewById(R.id.sma_3);
        emaCheckbox = (CheckBox) rootView.findViewById(R.id.ema);
        ema1 = (EditText) rootView.findViewById(R.id.ema_1);
        ema2 = (EditText) rootView.findViewById(R.id.ema_2);
        ema3 = (EditText) rootView.findViewById(R.id.ema_3);
        bolCheckbox = (CheckBox) rootView.findViewById(R.id.bol);
        bol1 = (EditText) rootView.findViewById(R.id.bol_1);
        bol2 = (EditText) rootView.findViewById(R.id.bol_2);
        sarCheckbox = (CheckBox) rootView.findViewById(R.id.sar);
        sar1 = (EditText) rootView.findViewById(R.id.sar_1);
        sar2 = (EditText) rootView.findViewById(R.id.sar_2);
        volumeCheckbox = (CheckBox) rootView.findViewById(R.id.volume);


        //Set the UI values
        setSettingsUI();


        return rootView;
    }



    public void saveSettings() {
        Settings.smaChecked = smaCheckbox.isChecked();
        Settings.sma[0] = Integer.valueOf(sma1.getText().toString());
        Settings.sma[1] = Integer.valueOf(sma2.getText().toString());
        Settings.sma[2] = Integer.valueOf(sma3.getText().toString());
        Settings.emaChecked = emaCheckbox.isChecked();
        Settings.ema[0] = Integer.valueOf(ema1.getText().toString());
        Settings.ema[1] = Integer.valueOf(ema2.getText().toString());
        Settings.ema[2] = Integer.valueOf(ema3.getText().toString());
        Settings.bolChecked = bolCheckbox.isChecked();
        Settings.bol[0] = Integer.valueOf(bol1.getText().toString());
        Settings.bol[1] = Integer.valueOf(bol2.getText().toString());
        Settings.sarChecked = sarCheckbox.isChecked();
        Settings.sar[0] = Float.valueOf(sar1.getText().toString());
        Settings.sar[1] = Float.valueOf(sar2.getText().toString());
        Settings.volumeChecked = volumeCheckbox.isChecked();
        Settings.saveSettings(getContext());
    }



    private void setSettingsUI() {
        if(Settings.smaChecked) {
            smaCheckbox.setChecked(true);
        }
        sma1.setText(String.valueOf(Settings.sma[0]));
        sma2.setText(String.valueOf(Settings.sma[1]));
        sma3.setText(String.valueOf(Settings.sma[2]));
        if(Settings.emaChecked) {
            emaCheckbox.setChecked(true);
        }
        ema1.setText(String.valueOf(Settings.ema[0]));
        ema2.setText(String.valueOf(Settings.ema[1]));
        ema3.setText(String.valueOf(Settings.ema[2]));
        if(Settings.bolChecked) {
            bolCheckbox.setChecked(true);
        }
        bol1.setText(String.valueOf(Settings.bol[0]));
        bol2.setText(String.valueOf(Settings.bol[1]));
        if(Settings.sarChecked) {
            sarCheckbox.setChecked(true);
        }
        sar1.setText(String.format("%.3f", Settings.sar[0]));
        sar2.setText(String.format("%.3f", Settings.sar[1]));
        if(Settings.volumeChecked) {
            volumeCheckbox.setChecked(true);
        }
    }
}
