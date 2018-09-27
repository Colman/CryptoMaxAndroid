package com.maxtechnologies.cryptomax.Main.WalletFragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Adapters.CustomSpinnerAdapter;
import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Controllers.AlertController;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Objects.Book;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Callbacks.NetworkCallbacks;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import java.util.ArrayList;

public class AddWalletFragment2 extends Fragment implements NetworkCallbacks {

    //Wallet declaration
    private Wallet wallet;

    //UI declarations
    private Spinner currency;
    private ArrayAdapter<String> adapter;
    private ImageView logo;
    private TextView address;
    private TextView balance;
    private TextView value;
    private EditText name;
    private Button nextButton;


    public static AddWalletFragment2 newInstance(Wallet wallet) {
        AddWalletFragment2 fragment = new AddWalletFragment2();
        Bundle bundle = new Bundle();
        bundle.putSerializable("WALLET", wallet);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_add_wallet_2, container, false);
        getActivity().setTitle(R.string.add_a_wallet);
        setHasOptionsMenu(true);


        //Un-bundle the wallet
        Bundle bundle = getArguments();
        wallet = (Wallet) bundle.getSerializable("WALLET");

        //UI definitions
        currency = (Spinner) rootView.findViewById(R.id.currency);
        ArrayList<String> walletNames = new ArrayList<>();
        Wallet[] wallets = StaticVariables.getSupportedWallets();
        for(int i = 0; i < wallets.length; i++) {
            walletNames.add(Exchange.translateToName(wallets[i].exchangeSymbol));
        }
        adapter = new CustomSpinnerAdapter(getActivity(), walletNames);
        currency.setAdapter(adapter);
        logo = (ImageView) rootView.findViewById(R.id.logo);
        address = (TextView) rootView.findViewById(R.id.address);
        balance = (TextView) rootView.findViewById(R.id.balance);
        value = (TextView) rootView.findViewById(R.id.value);
        name = (EditText) rootView.findViewById(R.id.name);
        nextButton = (Button) rootView.findViewById(R.id.next_button);


        //Set the UI values
        for(int i = 0; i < walletNames.size(); i++) {
            if(Exchange.translateToName(wallet.exchangeSymbol).equals(walletNames.get(i))) {
                currency.setSelection(i);
                currency.setEnabled(false);
                break;
            }
        }
        String symbol = Exchange.translateToSymbol(wallet.exchangeSymbol).toLowerCase();
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
        address.setText(wallet.address);


        //Start the ticker subscription
        ArrayList<Integer> indexes = new ArrayList<>();
        int index = Exchange.findIndex(wallet.exchangeSymbol);
        indexes.add(index);
        StaticVariables.exchanges[Settings.exchangeIndex].subTickers(indexes);


        //Start the search for the wallet balance
        wallet.getBalance(new BasicCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Failed to get wallet balance for reason: " + reason);
                AlertController.networkError(getActivity(), true);
            }

            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int index = Exchange.findIndex(wallet.exchangeSymbol);
                        String balanceStr = Exchange.coinString(wallet.balance, index, true, true);
                        balance.setText(balanceStr);

                        float valueFloat = wallet.balance * StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).price;
                        String valueStr = Exchange.fiatString(valueFloat, true, true, true);
                        value.setText(valueStr);
                    }
                });
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
                        wallet.name = nameStr;
                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        AddWalletFragment3 fragment = AddWalletFragment3.newInstance(wallet);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.content, fragment);
                        fragmentTransaction.commit();
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
                if(wallet != null) {
                    int index = Exchange.findIndex(wallet.exchangeSymbol);
                    float valueFloat = wallet.balance * StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).price;
                    String valueStr = Exchange.fiatString(valueFloat, true, true, true);
                    value.setText(valueStr);
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
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }
}
