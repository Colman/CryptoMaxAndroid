package com.maxtechnologies.cryptomax.Main.WalletFragments;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.mattprecious.swirl.SwirlView;


/**
 * Created by Colman on 04/05/2018.
 */

public class FingerprintFragment extends Fragment {

    //Fingerprint declarations
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private CancellationSignal cancellationSignal;

    //Timer declaration
    private Handler handler;
    private Runnable runnable;

    //UI declarations
    private RelativeLayout cardFrame;
    private ConstraintLayout step1;
    private TextView message1;
    private Button nextButton;
    private Button skipButton1;
    private ConstraintLayout step2;
    private SwirlView fingerprint;
    private SwirlView.State state;
    private Button skipButton2;


    public static FingerprintFragment newInstance() {
        FingerprintFragment fragment = new FingerprintFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_fingerprint, container, false);

        //Fingerprint definitions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getContext().getSystemService(Context.FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
        }

        //Timer definition
        handler = new Handler();
        runnable = new Runnable(){
            public void run(){
                if(state == SwirlView.State.OFF) {
                    fingerprint.setState(SwirlView.State.ON, true);
                    state = SwirlView.State.ON;
                }
                else {
                    fingerprint.setState(SwirlView.State.OFF, true);
                    state = SwirlView.State.OFF;
                }


                handler.postDelayed(this, 1000);
            }
        };

        //UI definitions
        cardFrame = (RelativeLayout) rootView.findViewById(R.id.card_frame);
        if(Settings.theme == 0) {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border));
        }
        else {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border_d));
        }
        step1 = (ConstraintLayout) rootView.findViewById(R.id.layout_1);
        message1 = (TextView) rootView.findViewById(R.id.message_1);
        nextButton = (Button) rootView.findViewById(R.id.next_button);
        skipButton1 = (Button) rootView.findViewById(R.id.skip_button_1);
        step2 = (ConstraintLayout) rootView.findViewById(R.id.layout_2);
        fingerprint = (SwirlView) rootView.findViewById(R.id.fingerprint);
        skipButton2 = (Button) rootView.findViewById(R.id.skip_button_2);


        //Setup the listener for the next button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                step1.setVisibility(View.INVISIBLE);
                step2.setVisibility(View.VISIBLE);

                handler.post(runnable);
                listen();
            }
        });


        //Setup the listeners for the skip buttons
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    AddWalletFragment4 fragment = (AddWalletFragment4) getParentFragment();
                    fragment.pager.setCurrentItem(2);
                }
                catch(ClassCastException e) {
                    CreateWalletFragment fragment = (CreateWalletFragment) getParentFragment();
                    fragment.pager.setCurrentItem(3);
                }
            }
        };
        skipButton1.setOnClickListener(listener);
        skipButton2.setOnClickListener(listener);


        //Check if the device supports fingerprint scanning
        setMessage();


        return rootView;
    }



    private void setMessage() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                message1.setText(R.string.fingerprint_device_not_supported);
                nextButton.setVisibility(View.GONE);
            }

            else if(!fingerprintManager.isHardwareDetected()) {
                message1.setText(R.string.fingerprint_device_not_supported);
                nextButton.setVisibility(View.GONE);
            }

            else if(!fingerprintManager.hasEnrolledFingerprints()) {
                message1.setText(R.string.no_fingerprints_added);
                nextButton.setVisibility(View.GONE);
            }

            else if(!keyguardManager.isKeyguardSecure()) {
                message1.setText(R.string.enable_lock_screen_security);
                nextButton.setVisibility(View.GONE);
            }

            else {
                message1.setText(R.string.fingerprint_supported);
                nextButton.setVisibility(View.VISIBLE);
            }
        }

        else {
            message1.setText(R.string.fingerprint_device_not_supported);
            nextButton.setVisibility(View.GONE);
        }
    }



    private void setError() {
        handler.removeCallbacksAndMessages(null);
        fingerprint.setState(SwirlView.State.ERROR, true);
        state = SwirlView.State.ERROR;
        handler.postDelayed(runnable, 1200);
    }



    private void listen() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cancellationSignal = new CancellationSignal();
            fingerprintManager.authenticate(null, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
                    setError();

                    if(errMsgId != 5) {
                        Toast newToast = Toast.makeText(getParentFragment().getActivity(),
                                errString,
                                Toast.LENGTH_LONG);
                        newToast.show();
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    setError();

                    Toast newToast = Toast.makeText(getParentFragment().getActivity(),
                            R.string.wrong_fingerprint,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    setError();

                    Toast newToast = Toast.makeText(getParentFragment().getActivity(),
                            helpString,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    if(vibrator != null) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            vibrator.vibrate(VibrationEffect.createOneShot(StaticVariables.successLength, StaticVariables.successAmplitude));
                        }

                        else {
                            vibrator.vibrate(StaticVariables.successLength);
                        }
                    }

                    SecurityFragment fragment = (SecurityFragment) getParentFragment();
                    fragment.fingerprint = true;
                    fragment.nextStep();
                }
            }, null);
        }
    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser){
            start();
        }

        else {
            cancel();
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        start();
    }



    @Override
    public void onPause() {
        super.onPause();
        cancel();
    }




    private void start() {
        if(step1.getVisibility() == View.VISIBLE) {
            setMessage();
        }

        else {
            handler.removeCallbacksAndMessages(null);
            handler.post(runnable);
            listen();
        }
    }



    private void cancel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(cancellationSignal != null) {
                cancellationSignal.cancel();
                cancellationSignal = null;
            }
        }

        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
