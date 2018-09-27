package com.maxtechnologies.cryptomax.ui.drawer.wallet.send.step1;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.R;
import com.google.zxing.Result;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.drawer.contact.ContactsFragment;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.send.step2.SendFragment2;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SendFragment1 extends Fragment implements ZXingScannerView.ResultHandler {

    //Wallet declaration
    private int index;

    //UI declarations
    private ZXingScannerView scanner;
    private Button contactsButton;


    public static SendFragment1 newInstance(int index) {
        SendFragment1 fragment = new SendFragment1();
        Bundle bundle = new Bundle();
        bundle.putInt("INDEX", index);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_send_1, container, false);
        getActivity().setTitle(R.string.send_title_1);


        //Un-bundle the index argument
        index = getArguments().getInt("INDEX");

        //UI definitions
        scanner = (ZXingScannerView) rootView.findViewById(R.id.scanner);
        contactsButton = (Button) rootView.findViewById(R.id.contacts_button);


        //Setup the QR code scanner
        scanner.setResultHandler(this);
        scanner.startCamera();


        //Setup the listener for the contacts button
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ContactsFragment fragment = ContactsFragment.newInstance(index, 2);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();
            }
        });


        return rootView;
    }



    @Override
    public void handleResult(Result result) {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        String resultStr = result.getText();

        if(CryptoMaxApi.getWallet(index).isValidAddress(resultStr)) {
            if(vibrator != null) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(StaticVariables.successLength, StaticVariables.successAmplitude));
                }

                else {
                    vibrator.vibrate(StaticVariables.successLength);
                }
            }

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SendFragment2 fragment = SendFragment2.newInstance(resultStr, 2);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.content, fragment);
            fragmentTransaction.commit();
        }

        else {
            if(vibrator != null) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(StaticVariables.errorLength, StaticVariables.errorAmplitude));
                }

                else {
                    vibrator.vibrate(StaticVariables.errorLength);
                }
            }

            String name = Exchange.translateToName(CryptoMaxApi.getWallet(index).exchangeSymbol);
            String errorMessage = getActivity().getResources().getString(R.string.bad_recipient_message, name);
            Toast newToast = Toast.makeText(getActivity(),
                    errorMessage,
                    Toast.LENGTH_LONG);
            newToast.show();
            scanner.resumeCameraPreview(this);
        }
    }
}
