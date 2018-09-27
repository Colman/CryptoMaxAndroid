package com.maxtechnologies.cryptomax.Main.WalletFragments;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

/**
 * Created by Colman on 18/03/2018.
 */

public class WalletSettingsFragment extends Fragment {

    //Wallet declaration
    private Wallet wallet;

    //UI declarations
    private EditText name;
    private ConstraintLayout keyLayout;
    private ConstraintLayout keyFrame;
    private TextView keyName;
    private ImageView trashButton;
    private Button saveButton;


    public static WalletSettingsFragment newInstance(int walletIndex) {
        WalletSettingsFragment fragment = new WalletSettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("INDEX", walletIndex);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_wallet_settings, container, false);

        //Wallet definition
        wallet = CryptoMaxApi.getWallet(getArguments().getInt("INDEX"));

        //UI definitions
        name = (EditText) rootView.findViewById(R.id.name);
        keyLayout = (ConstraintLayout) rootView.findViewById(R.id.key_layout);
        keyFrame = (ConstraintLayout) rootView.findViewById(R.id.key_card_frame);
        if(Settings.theme == 0) {
            keyFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border));
        }
        else {
            keyFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border_d));
        }
        keyName = (TextView) rootView.findViewById(R.id.key_name);
        trashButton = (ImageView) rootView.findViewById(R.id.trash_button);
        saveButton = (Button) rootView.findViewById(R.id.save_button);


        //Fill in UI values
        name.setText(wallet.name);
        if(wallet.privateKey == null) {
            keyLayout.setVisibility(View.GONE);
        }

        else {
            keyLayout.setVisibility(View.VISIBLE);
            keyName.setText(getResources().getString(R.string.key, Exchange.translateToName(wallet.exchangeSymbol)));
        }


        //Setup the listener for the trash button
        trashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyLayout.setVisibility(View.GONE);
                wallet.privateKey = null;
                CryptoMaxApi.saveWallets(getContext(), false);

                Toast newToast = Toast.makeText(getActivity(),
                        R.string.private_key_deleted,
                        Toast.LENGTH_LONG);
                newToast.show();
            }
        });


        //Setup the listener for the save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameStr = name.getText().toString();
                if(nameStr.length() != 0) {
                    wallet.name = nameStr;
                    CryptoMaxApi.saveWallets(getContext(), false);
                    getActivity().setTitle(wallet.name);

                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.name_changed,
                            Toast.LENGTH_SHORT);
                    newToast.show();
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
