package com.maxtechnologies.cryptomax.Main.WalletFragments;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Controllers.PermissionsController;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.CustomScannerView;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class AddWalletFragment1 extends Fragment implements ZXingScannerView.ResultHandler {

    //UI declarations
    private CustomScannerView scanner;
    private Button createButton;


    public static AddWalletFragment1 newInstance() {
        AddWalletFragment1 fragment = new AddWalletFragment1();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_add_wallet_1, container, false);
        getActivity().setTitle(R.string.add_a_wallet);
        setHasOptionsMenu(true);

        //UI definitions
        scanner = (CustomScannerView) rootView.findViewById(R.id.scanner);
        createButton = (Button) rootView.findViewById(R.id.create_button);


        //Setup the listener for the create button
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                CreateWalletFragment fragment = CreateWalletFragment.newInstance();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();
            }
        });


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



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
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

        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            if(resultStr.equals(CryptoMaxApi.getWallet(i).address)) {
                if(vibrator != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(StaticVariables.errorLength, StaticVariables.errorAmplitude));
                    }

                    else {
                        vibrator.vibrate(StaticVariables.errorLength);
                    }
                }

                Toast newToast = Toast.makeText(getActivity(),
                        R.string.same_address_message,
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

        wallets[index].address = resultStr;
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        AddWalletFragment2 fragment = AddWalletFragment2.newInstance(wallets[index]);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();
    }
}
