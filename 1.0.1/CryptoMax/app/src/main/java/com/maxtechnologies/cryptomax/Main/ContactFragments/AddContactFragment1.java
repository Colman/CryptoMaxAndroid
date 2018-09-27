package com.maxtechnologies.cryptomax.Main.ContactFragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Controllers.PermissionsController;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Other.CustomScannerView;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class AddContactFragment1 extends Fragment implements ZXingScannerView.ResultHandler {

    //UI declaration
    private CustomScannerView scanner;


    public static AddContactFragment1 newInstance() {
        AddContactFragment1 fragment = new AddContactFragment1();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_add_contact_1, container, false);
        getActivity().setTitle(R.string.add_a_contact);

        //UI definitions
        scanner = (CustomScannerView) rootView.findViewById(R.id.scanner);


        //Request camera permission
        int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            scanner.setResultHandler(this);
            scanner.startCamera();
        }

        else {
            PermissionsController.cameraPermission(getActivity());
        }

        return rootView;
    }



    public void permissionResult(boolean granted) {
        if(granted) {
            scanner.setResultHandler(this);
            scanner.startCamera();
        }
        else {
            getActivity().onBackPressed();
        }
    }



    @Override
    public void handleResult(Result result) {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        String resultStr = result.getText();

        if(resultStr.matches("[a-zA-Z0-9]*")) {
            Wallet[] wallets = StaticVariables.getSupportedWallets();

            int index = -1;
            for(int i = 0; i < wallets.length; i++) {
                if(wallets[i].isValidAddress(resultStr)) {
                    index = i;
                    break;
                }
            }

            if(index == -1) {
                if(vibrator != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(StaticVariables.errorLength, StaticVariables.errorAmplitude));
                    }

                    else {
                        vibrator.vibrate(StaticVariables.errorLength);
                    }
                }

                Toast newToast = Toast.makeText(getActivity(),
                        R.string.bad_address_message,
                        Toast.LENGTH_LONG);
                newToast.show();
                scanner.resumeCameraPreview(this);
                return;
            }

            for(int i = 0; i < StaticVariables.getContactsSize(); i++) {
                if(resultStr.equals(StaticVariables.getContact(i).address)) {
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
                    scanner.resumeCameraPreview(this);
                    return;
                }
            }

            if(vibrator != null) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(StaticVariables.successLength, StaticVariables.successAmplitude));
                }

                else {
                    vibrator.vibrate(StaticVariables.successLength);
                }
            }

            String symbol = Exchange.translateToSymbol(wallets[index].exchangeSymbol);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            AddContactFragment2 fragment = AddContactFragment2.newInstance(symbol, resultStr);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.content, fragment);
            fragmentTransaction.commit();
        }

        else {
            Toast newToast = Toast.makeText(getActivity(),
                    R.string.bad_address_message,
                    Toast.LENGTH_LONG);
            newToast.show();
            scanner.resumeCameraPreview(this);
        }
    }
}
