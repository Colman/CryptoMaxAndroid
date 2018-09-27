package com.maxtechnologies.cryptomax.Main.SendFragments;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Main.WalletFragments.SecurityFragment;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;
import com.mattprecious.swirl.SwirlView;

import java.util.Random;

/**
 * Created by Colman on 02/06/2018.
 */

public class AuthenticateFragment extends Fragment {

    //Parent declaration
    private SendFragment2 parent;

    //Step declaration
    private int step;

    //Fingerprint declarations
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private CancellationSignal cancellationSignal;

    //Timer declaration
    private Handler handler;
    private Runnable runnable;

    //UI declarations
    private RelativeLayout cardFrame;
    private ConstraintLayout passwordLayout;
    private EditText password;
    private Button passwordButton;
    private ConstraintLayout fingerprintLayout;
    private SwirlView fingerprint;
    private SwirlView.State state;
    private ConstraintLayout emailLayout;
    private TextView email;
    private EditText code;
    private String codeStr;
    private Button emailButton;


    public static AuthenticateFragment newInstance() {
        AuthenticateFragment fragment = new AuthenticateFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_authenticate, container, false);

        //Parent definition
        parent = (SendFragment2) getParentFragment();

        //Step declaration
        step = 0;

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
        passwordLayout = (ConstraintLayout) rootView.findViewById(R.id.password_layout);
        password = (EditText) rootView.findViewById(R.id.password);
        passwordButton = (Button) rootView.findViewById(R.id.password_button);
        fingerprintLayout = (ConstraintLayout) rootView.findViewById(R.id.fingerprint_layout);
        fingerprint = (SwirlView) rootView.findViewById(R.id.fingerprint);
        emailLayout = (ConstraintLayout) rootView.findViewById(R.id.email_layout);
        email = (TextView) rootView.findViewById(R.id.email);
        code = (EditText) rootView.findViewById(R.id.code);
        emailButton = (Button) rootView.findViewById(R.id.email_button);


        //Setup the listener for the password button
        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passwordStr = password.getText().toString();
                Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
                if(wallet.decrypt(passwordStr) != null) {
                    parent.password = passwordStr;
                    nextStep();
                }

                else {
                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.incorrect_password,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }
        });


        //Setup the listener for the email button
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredCode = code.getText().toString().toUpperCase();

                if(enteredCode.equals(codeStr.toUpperCase())) {
                    parent.nextStep();
                }

                else {
                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.incorrect_code,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }
        });


        return rootView;
    }



    private void nextStep() {
        Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
        if(step == 0) {
            if(wallet.privateKey.fingerprint) {
                passwordLayout.setVisibility(View.INVISIBLE);
                fingerprintLayout.setVisibility(View.VISIBLE);
                step++;
                start();
                return;
            }

            else if(wallet.privateKey.email != null) {
                passwordLayout.setVisibility(View.INVISIBLE);
                emailLayout.setVisibility(View.VISIBLE);
                sendEmail();
                step += 2;
                return;
            }
        }

        else if(step == 1) {
            if(wallet.privateKey.email != null) {
                fingerprintLayout.setVisibility(View.INVISIBLE);
                emailLayout.setVisibility(View.VISIBLE);
                sendEmail();
                step++;
                cancel();
                return;
            }
        }

        parent.nextStep();
    }



    public boolean lastStep() {
        if(step == 1) {
            fingerprintLayout.setVisibility(View.INVISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            step = 0;
            cancel();
            return false;
        }

        else if(step == 2) {
            Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
            if(wallet.privateKey.fingerprint) {
                emailLayout.setVisibility(View.INVISIBLE);
                fingerprintLayout.setVisibility(View.VISIBLE);
                step--;
                start();
                return false;
            }

            emailLayout.setVisibility(View.INVISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            step = 0;
            return false;
        }

        return true;
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
                        Toast newToast = Toast.makeText(getActivity(),
                                errString,
                                Toast.LENGTH_LONG);
                        newToast.show();
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    setError();

                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.wrong_fingerprint,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                    setError();

                    Toast newToast = Toast.makeText(getActivity(),
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

                    nextStep();
                }
            }, null);
        }
    }



    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser){
            Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
            if(wallet.privateKey.email != null) {

                String emailStr = wallet.privateKey.email;
                String[] emailArr = wallet.privateKey.email.split("@");
                if(emailArr[0].length() > 3) {
                    emailStr = emailArr[0].substring(0, 3) + "***@" + emailArr[1];
                }
                email.setText(emailStr);
            }

            if(step == 1) {
                start();
            }
        }

        else {
            cancel();
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        if(step == 1) {
            start();
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        cancel();
    }



    private void start() {
        if(fingerprintEnabled()) {
            handler.removeCallbacksAndMessages(null);
            handler.post(runnable);
            listen();
        }

        else {
            nextStep();
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



    private boolean fingerprintEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            else if (!fingerprintManager.isHardwareDetected()) {
                return false;
            }
            else if (!fingerprintManager.hasEnrolledFingerprints()) {
                return false;
            }
            else if (!keyguardManager.isKeyguardSecure()) {
                return false;
            }
            else {
                return true;
            }
        }

        else {
            return false;
        }
    }



    private void sendEmail() {
        byte[] bytes = new byte[3];
        new Random().nextBytes(bytes);
        codeStr = Wallet.byteArrayToHexString(bytes);

        Wallet wallet = CryptoMaxApi.getWallet(parent.fromIndex);
        CryptoMaxApi.sendEmail(wallet.privateKey.email, codeStr);
        Toast newToast = Toast.makeText(getContext(), R.string.send_email_success,
                Toast.LENGTH_SHORT);
        newToast.show();
    }
}
