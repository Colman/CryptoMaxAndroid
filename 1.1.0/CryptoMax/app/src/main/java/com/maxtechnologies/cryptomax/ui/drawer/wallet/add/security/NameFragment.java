package com.maxtechnologies.cryptomax.ui.drawer.wallet.add.security;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.misc.CustomSpinnerAdapter;
import com.maxtechnologies.cryptomax.wallets.Wallet;

import java.util.ArrayList;

/**
 * Created by Colman on 15/05/2018.
 */

public class NameFragment extends Fragment {

    //UI declarations
    private ConstraintLayout cardFrame;
    private Spinner currency;
    private ArrayAdapter<String> adapter;
    private ImageView logo;
    private EditText name;
    private Button nextButton;


    public static NameFragment newInstance() {
        NameFragment fragment = new NameFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_name, container, false);

        //UI definitions
        cardFrame = (ConstraintLayout) rootView.findViewById(R.id.card_frame);
        if(Settings.theme == 0) {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border));
        }
        else {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border_d));
        }
        currency = (Spinner) rootView.findViewById(R.id.currency);
        ArrayList<String> walletNames = new ArrayList<>();
        final Wallet[] wallets = StaticVariables.getSupportedWallets();
        for(int i = 0; i < wallets.length; i++) {
            walletNames.add(Exchange.translateToName(wallets[i].exchangeSymbol));
        }
        adapter = new CustomSpinnerAdapter(getActivity(), walletNames);
        currency.setAdapter(adapter);
        logo = (ImageView) rootView.findViewById(R.id.logo);
        name = (EditText) rootView.findViewById(R.id.name);
        nextButton = (Button) rootView.findViewById(R.id.next_button);


        //Setup the listener for the currency spinner
        currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Wallet selectedWallet = wallets[i];
                String symbol = Exchange.translateToSymbol(selectedWallet.exchangeSymbol).toLowerCase();
                Drawable drawable;
                if(Settings.theme == 0) {
                    try {
                        drawable = getResources().getDrawable(getResources().getIdentifier(
                                symbol, "drawable", getActivity().getPackageName()));
                    }

                    catch (Exception e) {
                        drawable = getResources().getDrawable(getResources().getIdentifier(
                                "logo", "drawable", getActivity().getPackageName()));
                    }
                }

                else {
                    try {
                        drawable = getResources().getDrawable(getResources().getIdentifier(
                                symbol + "_d", "drawable", getActivity().getPackageName()));
                    }

                    catch (Exception e) {
                        try {
                            drawable = getResources().getDrawable(getResources().getIdentifier(
                                    symbol, "drawable", getActivity().getPackageName()));
                        }

                        catch (Exception e2) {
                            drawable = getResources().getDrawable(getResources().getIdentifier(
                                    "logo_d", "drawable", getActivity().getPackageName()));
                        }
                    }
                }
                logo.setImageDrawable(drawable);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the next button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameStr = name.getText().toString();
                if(!nameStr.equals("")) {
                    boolean matches = false;
                    for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
                        if(nameStr.equals(CryptoMaxApi.getWallet(i).name)) {
                            matches = true;
                            break;
                        }
                    }
                    if(!matches) {
                        SecurityFragment fragment = (SecurityFragment) getParentFragment();
                        Wallet wallet = wallets[currency.getSelectedItemPosition()];

                        fragment.wallet = wallet.clone();

                        fragment.wallet.name = nameStr;
                        fragment.wallet.generateWallet("TEMP");
                        fragment.pager.setCurrentItem(1, false);
                    }
                    else {
                        Toast newToast = Toast.makeText(getActivity(),
                                R.string.same_name_message,
                                Toast.LENGTH_LONG);
                        newToast.show();
                    }
                }
                else {
                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.add_name_message,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }
        });


        return rootView;
    }
}
