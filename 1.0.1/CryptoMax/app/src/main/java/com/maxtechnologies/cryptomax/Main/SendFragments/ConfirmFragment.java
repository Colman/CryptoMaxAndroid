package com.maxtechnologies.cryptomax.Main.SendFragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Callbacks.ProfilesCallback;
import com.maxtechnologies.cryptomax.Callbacks.TransactionCallback;
import com.maxtechnologies.cryptomax.Controllers.AlertController;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Main.MainActivity;
import com.maxtechnologies.cryptomax.Main.WalletFragments.WalletTransactionsFragment;
import com.maxtechnologies.cryptomax.Objects.Contact;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import java.util.ArrayList;

/**
 * Created by Colman on 02/06/2018.
 */

public class ConfirmFragment extends Fragment {

    //Sent declaration
    private boolean sent;

    //Parent definition
    private SendFragment2 parent;

    //UI definitions
    private ConstraintLayout cardFrame;
    private RelativeLayout imageLayout;
    private ImageView imageBorder;
    private ImageView profileImage;
    private TextView address;
    private ProgressBar progress;
    private TextView received;
    private TextView fee;
    private TextView total;
    private Button confirmButton;


    public static ConfirmFragment newInstance() {
        ConfirmFragment fragment = new ConfirmFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_confirm, container, false);

        //Sent declaration
        sent = false;

        //Parent definition
        parent = (SendFragment2) getParentFragment();

        //UI definitions
        cardFrame = (ConstraintLayout) rootView.findViewById(R.id.card_frame);
        if(Settings.theme == 0) {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border));
        }
        else {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border_d));
        }
        imageLayout = (RelativeLayout) rootView.findViewById(R.id.image_layout);
        imageBorder = (ImageView) rootView.findViewById(R.id.image_border);
        profileImage = (ImageView) rootView.findViewById(R.id.profile_image);
        address = (TextView) rootView.findViewById(R.id.address);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        received = (TextView) rootView.findViewById(R.id.received);
        fee = (TextView) rootView.findViewById(R.id.fee);
        total = (TextView) rootView.findViewById(R.id.total);
        confirmButton = (Button) rootView.findViewById(R.id.confirm_button);


        //Setup the listener for the confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!sent) {
                    Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
                    wallet.send(parent.toAddress, parent.amount, parent.fee, parent.password, new TransactionCallback() {
                        @Override
                        public void onFailure(int code, String message) {
                            finish(null);
                        }

                        @Override
                        public void onSuccess(String hash) {
                            finish(hash);
                        }
                    });
                }
                sent = true;
            }
        });


        return rootView;
    }



    private void finish(String hash) {
        Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
        int index = Exchange.findIndex(wallet.exchangeSymbol);
        String amountStr = Exchange.coinString(parent.amount, index, true, true);
        int id = R.string.transaction_failed;
        if(hash != null) {
            id = R.string.transaction_pending;
            wallet.balance -= parent.amount + parent.fee;
            Transaction transaction = new Transaction(hash, wallet.address, parent.toAddress, parent.amount, parent.fee, null);
            wallet.transactions.add(0, transaction);
            CryptoMaxApi.saveWallets(getContext(), false);
            CryptoMaxApi.pendingTxs.add(0, transaction);
        }

        final String transactionStr = getResources().getString(id, amountStr);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast newToast = Toast.makeText(getActivity(),
                        transactionStr,
                        Toast.LENGTH_LONG);
                newToast.show();
            }
        });

        parent.nextStep();
    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser){
            setProfile();

            Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
            int index = Exchange.findIndex(wallet.exchangeSymbol);
            received.setText(Exchange.coinString(parent.amount, index, true, true));
            fee.setText(Exchange.coinString(parent.fee, index, true, true));
            total.setText(Exchange.coinString(parent.amount + parent.fee, index, true, true));
        }
    }



    private void setProfile() {
        ArrayList<String> addressList = new ArrayList<>();
        addressList.add(parent.toAddress);
        CryptoMaxApi.getProfiles(addressList, new ProfilesCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Failed to get profile for reason: " + reason);
                AlertController.networkError(getActivity(), true);
            }

            @Override
            public void onSuccess(final String[] names, final Bitmap[] images) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(names[0] != null && !names[0].equals("")) {
                            address.setText(names[0]);
                        }
                        else {
                            address.setText(parent.toAddress);
                        }

                        if(images[0] != null) {
                            profileImage.setImageBitmap(images[0]);
                        }

                        progress.setVisibility(View.INVISIBLE);
                        imageLayout.setVisibility(View.VISIBLE);
                        address.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}
