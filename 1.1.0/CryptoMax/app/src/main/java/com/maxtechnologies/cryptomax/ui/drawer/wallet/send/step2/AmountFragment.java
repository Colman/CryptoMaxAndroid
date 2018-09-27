package com.maxtechnologies.cryptomax.ui.drawer.wallet.send.step2;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.wallets.misc.FeeCallback;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.ui.misc.AlertController;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.wallets.Wallet;

import java.util.ArrayList;

/**
 * Created by Colman on 02/06/2018.
 */

public class AmountFragment extends Fragment {

    //Parent declaration
    private SendFragment2 parent;

    //Amount declarations
    private float amountVal;
    private float feeVal;
    private boolean calculate;

    //UI declarations
    private ConstraintLayout cardFrame;
    private RelativeLayout fromLayout;
    private Spinner from;
    private ArrayList<Integer> indices;
    private WalletSpinnerAdapter adapter;
    private TextView received;
    private TextView fee;
    private ProgressBar progress;
    private TextView total;
    private EditText amount;
    private Button nextButton;


    public static AmountFragment newInstance() {
        AmountFragment fragment = new AmountFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_amount, container, false);

        //Parent definition
        parent = (SendFragment2) getParentFragment();

        //Amount definitions
        amountVal = 0;
        feeVal = 0;
        calculate = false;

        //UI definitions
        cardFrame = (ConstraintLayout) rootView.findViewById(R.id.card_frame);
        if(Settings.theme == 0) {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border));
        }
        else {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border_d));
        }
        fromLayout = (RelativeLayout) rootView.findViewById(R.id.from_layout);
        if(Settings.theme == 0) {
            fromLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.spinner_border));
        }
        else {
            fromLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.spinner_border_d));
        }
        from = (Spinner) rootView.findViewById(R.id.from);
        Wallet wallet = null;
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            wallet = CryptoMaxApi.getWallet(i);
            if(wallet.isValidAddress(parent.toAddress)) {
                break;
            }
        }
        indices = new ArrayList<>();
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            Wallet wallet2 = CryptoMaxApi.getWallet(i);
            if(wallet2.exchangeSymbol.equals(wallet.exchangeSymbol)) {
                indices.add(i);
            }
        }
        adapter = new WalletSpinnerAdapter(getContext(), indices);
        from.setAdapter(adapter);
        from.setSelection(0);
        received = (TextView) rootView.findViewById(R.id.received);
        fee = (TextView) rootView.findViewById(R.id.fee);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        total = (TextView) rootView.findViewById(R.id.total);
        amount = (EditText) rootView.findViewById(R.id.amount);
        nextButton = (Button) rootView.findViewById(R.id.next_button);


        //Set the UI values
        parent.fromIndex = indices.get(0);
        setSummary();


        //Setup the listener for the from spinner
        from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                parent.fromIndex = indices.get(i);
                calculate = true;
                nextButton.setText(R.string.calculate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the amount field
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                amountVal = 0;
                calculate = true;
                nextButton.setText(R.string.calculate);

                try {
                    amountVal = Float.valueOf(charSequence.toString());
                }

                catch (java.lang.NumberFormatException e) {
                    //Do nothing
                }

                setSummary();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Do nothing
            }
        });


        //Setup the listener for the next button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (calculate) {
                    getFee();
                }

                else {
                    if (amountVal == 0) {
                        Toast newToast = Toast.makeText(getActivity(),
                                R.string.please_set_amount,
                                Toast.LENGTH_LONG);
                        newToast.show();
                        return;
                    }

                    Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
                    if (amountVal + feeVal <= wallet.balance) {
                        parent.amount = amountVal;
                        parent.fee = feeVal;
                        parent.nextStep();
                    }

                    else {
                        int index = Exchange.findIndex(wallet.exchangeSymbol);
                        String maxStr = Exchange.coinString(wallet.balance, index, true, true);
                        String fundsStr = getResources().getString(R.string.insufficient_funds, maxStr);
                        Toast newToast = Toast.makeText(getActivity(),
                                fundsStr,
                                Toast.LENGTH_LONG);
                        newToast.show();
                    }
                }
            }
        });


        return rootView;
    }



    private void getFee() {
        final float reqAmount = amountVal;
        final int reqIndex = parent.fromIndex;
        progress.setVisibility(View.VISIBLE);
        fee.setVisibility(View.INVISIBLE);

        Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
        wallet.getFee(amountVal, new FeeCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Failed to get fee for reason: " + reason);
                AlertController.networkError(getActivity(), true);
            }

            @Override
            public void onSuccess(final float fee) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(reqAmount == amountVal && reqIndex == parent.fromIndex) {
                            calculate = false;
                            nextButton.setText(R.string.next);
                        }

                        feeVal = fee;
                        progress.setVisibility(View.INVISIBLE);
                        AmountFragment.this.fee.setVisibility(View.VISIBLE);
                        setSummary();
                    }
                });
            }
        });
    }



    private void setSummary() {
        Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
        int index = Exchange.findIndex(wallet.exchangeSymbol);

        received.setText(Exchange.coinString(amountVal, index, true, true));
        fee.setText(Exchange.coinString(feeVal, index, true, true));
        total.setText(Exchange.coinString(amountVal + feeVal, index, true, true));
    }
}
