package com.maxtechnologies.cryptomax.Main.WalletFragments;


import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.Random;

/**
 * Created by Colman on 04/05/2018.
 */

public class EmailFragment extends Fragment {

    //Entry declarations
    private String emailStr;
    private String codeStr;

    //UI declarations
    private RelativeLayout cardFrame;
    private ConstraintLayout step1;
    private EditText email;
    private Button sendButton;
    private Button skipButton;
    private ConstraintLayout step2;
    private EditText code;
    private Button finishButton;
    private Button backButton;


    public static EmailFragment newInstance() {
        EmailFragment fragment = new EmailFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_email, container, false);


        //UI definitions
        cardFrame = (RelativeLayout) rootView.findViewById(R.id.card_frame);
        if(Settings.theme == 0) {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border));
        }
        else {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border_d));
        }
        step1 = (ConstraintLayout) rootView.findViewById(R.id.layout_1);
        email = (EditText) rootView.findViewById(R.id.email);
        sendButton = (Button) rootView.findViewById(R.id.send_button);
        skipButton = (Button) rootView.findViewById(R.id.skip_button);
        step2 = (ConstraintLayout) rootView.findViewById(R.id.layout_2);
        skipButton = (Button) rootView.findViewById(R.id.skip_button);
        code = (EditText) rootView.findViewById(R.id.code);
        finishButton = (Button) rootView.findViewById(R.id.finish_button);
        backButton = (Button) rootView.findViewById(R.id. back_button);


        //Setup the listener for the send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailStr = email.getText().toString();
                if(EmailValidator.getInstance().isValid(emailStr)) {
                    sendEmail();
                    step1.setVisibility(View.INVISIBLE);
                    step2.setVisibility(View.VISIBLE);
                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.email_sent,
                            Toast.LENGTH_SHORT);
                    newToast.show();
                }
                else {
                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.invalid_email,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }
        });


        //Setup the listener for the skip button
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecurityFragment fragment = (SecurityFragment) getParentFragment();
                fragment.wallet.privateKey.email = null;
                fragment.nextStep();
            }
        });


        //Setup the listener for the finish
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredCode = code.getText().toString().toUpperCase();

                if(enteredCode.equals(codeStr.toUpperCase())) {
                    SecurityFragment fragment = (SecurityFragment) getParentFragment();
                    fragment.email = emailStr;
                    fragment.nextStep();
                }

                else {
                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.incorrect_code,
                            Toast.LENGTH_LONG);
                    newToast.show();
                }
            }
        });


        //Setup the listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                step2.setVisibility(View.INVISIBLE);
                step1.setVisibility(View.VISIBLE);
            }
        });

        return rootView;
    }



    private void sendEmail() {
        byte[] bytes = new byte[3];
        new Random().nextBytes(bytes);
        codeStr = Wallet.byteArrayToHexString(bytes);

        CryptoMaxApi.sendEmail(emailStr, codeStr);
        Toast newToast = Toast.makeText(getContext(), R.string.send_email_success,
                Toast.LENGTH_SHORT);
        newToast.show();
    }
}
