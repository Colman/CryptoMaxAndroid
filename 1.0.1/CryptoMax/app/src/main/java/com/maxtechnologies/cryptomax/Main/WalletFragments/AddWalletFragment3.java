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
import com.maxtechnologies.cryptomax.Main.MainActivity;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.CustomScannerView;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class AddWalletFragment3 extends Fragment implements ZXingScannerView.ResultHandler {

    //Wallet declaration
    private Wallet wallet;

    //UI declarations
    private CustomScannerView scanner;
    private Button skipButton;


    public static AddWalletFragment3 newInstance(Wallet wallet) {
        AddWalletFragment3 fragment = new AddWalletFragment3();
        Bundle bundle = new Bundle();
        bundle.putSerializable("WALLET", wallet);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_add_wallet_3, container, false);
        getActivity().setTitle(R.string.add_a_wallet);
        setHasOptionsMenu(true);


        //Un-bundle the wallet object
        Bundle bundle = getArguments();
        wallet = (Wallet) bundle.getSerializable("WALLET");

        //UI definitions
        scanner = (CustomScannerView) rootView.findViewById(R.id.scanner);
        skipButton = (Button) rootView.findViewById(R.id.skip_button);


        //Setup the listener for the create button
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CryptoMaxApi.addWallet(wallet, getContext());

                ((MainActivity) getActivity()).subWalletTickers();
                ((MainActivity) getActivity()).setAssets();

                FragmentManager fragmentManager = getFragmentManager();
                int count = fragmentManager.getBackStackEntryCount();
                fragmentManager.popBackStack(count - 3, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

        if(resultStr.matches("[a-zA-Z0-9]*")) {
            if(wallet.privateKeyToAddress(result.getText()).equals(wallet.address)) {
                if(vibrator != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(StaticVariables.successLength, StaticVariables.successAmplitude));
                    }

                    else {
                        vibrator.vibrate(StaticVariables.successLength);
                    }
                }

                wallet.encrypt(result.getText(), "TEMP", false, null);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                AddWalletFragment4 fragment = AddWalletFragment4.newInstance(wallet);
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

                Toast newToast = Toast.makeText(getActivity(),
                        R.string.private_key_mismatch_message,
                        Toast.LENGTH_LONG);
                newToast.show();
            }
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

            Toast newToast = Toast.makeText(getActivity(),
                    R.string.bad_private_key_message,
                    Toast.LENGTH_LONG);
            newToast.show();
        }

        scanner.resumeCameraPreview(this);
    }
}
