package com.maxtechnologies.cryptomax.ui.drawer.wallet.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.api.ProfilesCallback;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.drawer.contact.AddContactFragment2;
import com.maxtechnologies.cryptomax.ui.misc.AlertController;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.wallets.Wallet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ViewTransactionFragment extends Fragment {

    //Wallet declaration
    private Wallet wallet;

    //UI declarations
    private ImageView logo;
    private TextView currency;
    private TextView date;
    private ImageView fromImage;
    private ImageView fromBorder;
    private TextView fromAddress;
    private ImageView toImage;
    private ImageView toBorder;
    private TextView toAddress;
    private TextView sent;
    private TextView sentValue;
    private TextView fee;
    private TextView received;
    private TextView receivedValue;



    public static ViewTransactionFragment newInstance(Wallet wallet, int index) {
        ViewTransactionFragment fragment = new ViewTransactionFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("WALLET", wallet);
        bundle.putInt("INDEX", index);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_view_transaction, container, false);
        getActivity().setTitle(R.string.transaction);
        setHasOptionsMenu(true);


        //Wallet and TX definition
        Bundle bundle = getArguments();
        wallet = (Wallet) bundle.getSerializable("WALLET");
        Transaction transaction = wallet.transactions.get(bundle.getInt("INDEX"));

        //UI definitions
        logo = (ImageView) rootView.findViewById(R.id.logo);
        currency = (TextView) rootView.findViewById(R.id.currency);
        date = (TextView) rootView.findViewById(R.id.date);
        fromImage = (ImageView) rootView.findViewById(R.id.from_image);
        fromBorder = (ImageView) rootView.findViewById(R.id.from_border);
        fromAddress = (TextView) rootView.findViewById(R.id.from_address);
        toImage = (ImageView) rootView.findViewById(R.id.to_image);
        toBorder = (ImageView) rootView.findViewById(R.id.to_border);
        toAddress = (TextView) rootView.findViewById(R.id.to_address);
        sent = (TextView) rootView.findViewById(R.id.sent);
        sentValue = (TextView) rootView.findViewById(R.id.sent_value);
        fee = (TextView) rootView.findViewById(R.id.fee);
        received = (TextView) rootView.findViewById(R.id.received);
        receivedValue = (TextView) rootView.findViewById(R.id.received_value);


        //Fill in the UI values
        Drawable drawable;
        String symbol = Exchange.translateToSymbol(wallet.exchangeSymbol).toLowerCase();
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

        currency.setText(Exchange.translateToName(wallet.exchangeSymbol));

        SimpleDateFormat format;
        if(Settings.times == 0) {
            format = new SimpleDateFormat("MMM dd YYYY h:mm a", Locale.US);
        }

        else {
            format = new SimpleDateFormat("MMM dd YYYY H:mm", Locale.US);
        }

        try {
            date.setText(format.format(transaction.timeMined));
        }

        catch (Exception e) {
            //Do nothing
        }


        boolean fromMe = false;
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            if(CryptoMaxApi.getWallet(i).address.equals(transaction.fromAddress)) {
                fromMe = true;
                break;
            }
        }
        if(fromMe) {
            if(CryptoMaxApi.getImage() != null) {
                fromImage.setImageBitmap(CryptoMaxApi.getImage());
            }
            fromAddress.setText(R.string.me);
            fromAddress.setTypeface(null, Typeface.ITALIC);

            toAddress.setText(transaction.toAddress);
            getProfile(transaction.toAddress, true);
        }
        else {
            if(CryptoMaxApi.getImage() != null) {
                toImage.setImageBitmap(CryptoMaxApi.getImage());
            }
            toAddress.setText(R.string.me);
            toAddress.setTypeface(null, Typeface.ITALIC);

            fromAddress.setText(transaction.fromAddress);
            getProfile(transaction.fromAddress, false);
        }


        if(Settings.theme == 0) {
            drawable = getResources().getDrawable(getResources().getIdentifier(
                    "image_border_black", "drawable", getActivity().getPackageName()));
        }

        else {
            drawable = getResources().getDrawable(getResources().getIdentifier(
                    "image_border_white", "drawable", getActivity().getPackageName()));
        }
        fromBorder.setImageDrawable(drawable);
        toBorder.setImageDrawable(drawable);



        int index = Exchange.findIndex(wallet.exchangeSymbol);
        String sentStr = Exchange.coinString(transaction.amount + transaction.fee, index, true, true);
        sent.setText(getResources().getString(R.string.sent) + ": " + sentStr);

        sentValue.setText(getResources().getString(R.string.value) + ": " +  Exchange.fiatString((float) 0, true, true, true));

        fee.setText(getResources().getString(R.string.fee) + ": " +  Exchange.coinString(transaction.fee, index, true, true));

        received.setText(getResources().getString(R.string.received) + ": " +  Exchange.coinString(transaction.amount, index, true, true));

        float price = 0;
        receivedValue.setText(getResources().getString(R.string.value) + ": " +  Exchange.fiatString(price * transaction.amount, true, true, true));


        return rootView;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view_transaction, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.add_button:
                for(int i = 0; i < StaticVariables.getContactsSize(); i++) {
                    if(wallet.address.equals(StaticVariables.getContact(i).address)) {
                        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        if(vibrator != null) {
                            if (Build.VERSION.SDK_INT >= 26) {
                                vibrator.vibrate(VibrationEffect.createOneShot(StaticVariables.errorLength, StaticVariables.errorAmplitude));
                            }

                            else {
                                vibrator.vibrate(StaticVariables.errorLength);
                            }
                        }

                        Toast newToast = Toast.makeText(getActivity(),
                                R.string.same_contact_message,
                                Toast.LENGTH_LONG);
                        newToast.show();
                        return true;
                    }
                }

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                String symbol = Exchange.translateToSymbol(wallet.exchangeSymbol);
                AddContactFragment2 fragment = AddContactFragment2.newInstance(symbol, wallet.address);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void getProfile(String address, final boolean fromMe) {
        ArrayList<String> addresses = new ArrayList<>();
        addresses.add(address);

        CryptoMaxApi.getProfiles(addresses, new ProfilesCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Failed to get profiles for reason: " + reason);
                AlertController.networkError(getActivity(), true);
            }

            @Override
            public void onSuccess(final String[] names, final Bitmap[] images) {
                if(names.length == 1 && images.length == 1) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!names[0].equals("")) {
                                if(fromMe) {
                                    toAddress.setText(names[0]);
                                }
                                else {
                                    fromAddress.setText(names[0]);
                                }
                            }

                            if(images[0] != null) {
                                if (fromMe) {
                                    toImage.setImageBitmap(images[0]);
                                }
                                else {
                                    fromImage.setImageBitmap(images[0]);
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
