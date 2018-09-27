package com.maxtechnologies.cryptomax.ui.drawer.wallet.add.security;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.misc.Settings;

import java.util.ArrayList;

/**
 * Created by Colman on 04/05/2018.
 */

public class PasswordFragment extends Fragment {

    //Check mark declarations
    private Drawable x;
    private Drawable check;
    private int xColor;
    private int checkColor;

    //UI declarations
    private ConstraintLayout cardFrame;
    private ImageView length;
    private ImageView lower;
    private ImageView upper;
    private ImageView number;
    private EditText password;
    private EditText confirmPassword;
    private Button passwordButton;


    public static PasswordFragment newInstance() {
        PasswordFragment fragment = new PasswordFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_password, container, false);


        //Check mark definitions
        x = getResources().getDrawable(android.R.drawable.ic_delete);
        check = getResources().getDrawable(R.drawable.check_mark);
        if(Settings.theme == 0) {
            xColor = getResources().getColor(R.color.colorPrimary);
            checkColor = getResources().getColor(R.color.colorAccent);
        }
        else {
            xColor = Color.parseColor("#FFFFFF");
            checkColor = getResources().getColor(R.color.colorAccent);
        }



        //UI definitions
        cardFrame = (ConstraintLayout) rootView.findViewById(R.id.card_frame);
        if(Settings.theme == 0) {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border));
        }
        else {
            cardFrame.setBackgroundDrawable(getResources().getDrawable(R.drawable.card_border_d));
        }
        length = (ImageView) rootView.findViewById(R.id.length_icon);
        lower = (ImageView) rootView.findViewById(R.id.lower_icon);
        upper = (ImageView) rootView.findViewById(R.id.upper_icon);
        number = (ImageView) rootView.findViewById(R.id.number_icon);
        password = (EditText) rootView.findViewById(R.id.password);
        confirmPassword = (EditText) rootView.findViewById(R.id.confirm_password);
        passwordButton = (Button) rootView.findViewById(R.id.password_button);


        //Set the UI values
        length.setColorFilter(xColor);
        lower.setColorFilter(xColor);
        upper.setColorFilter(xColor);
        number.setColorFilter(xColor);


        //Setup the on edit listener for the password field
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ArrayList<Integer> requirements = checkPassword();
                if(requirements.contains(1)) {
                    length.setImageDrawable(x);
                    length.setColorFilter(xColor);
                }
                else {
                    length.setImageDrawable(check);
                    length.setColorFilter(checkColor);
                }

                if(requirements.contains(2)) {
                    lower.setImageDrawable(x);
                    lower.setColorFilter(xColor);
                }
                else {
                    lower.setImageDrawable(check);
                    lower.setColorFilter(checkColor);
                }

                if(requirements.contains(3)) {
                    upper.setImageDrawable(x);
                    upper.setColorFilter(xColor);
                }
                else {
                    upper.setImageDrawable(check);
                    upper.setColorFilter(checkColor);
                }

                if(requirements.contains(4)) {
                    number.setImageDrawable(x);
                    number.setColorFilter(xColor);
                }
                else {
                    number.setImageDrawable(check);
                    number.setColorFilter(checkColor);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Do nothing
            }
        });


        //Setup the on click listener for the password button
        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passwordStr = password.getText().toString();
                if(passwordStr.length() == 0) {
                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.password_must_set,
                            Toast.LENGTH_LONG);
                    newToast.show();
                    return;
                }

                if(checkPassword().size() == 0) {
                    SecurityFragment fragment = (SecurityFragment) getParentFragment();
                    fragment.password = passwordStr;
                    fragment.nextStep();
                }

                else {
                    Toast newToast = Toast.makeText(getActivity(),
                            R.string.password_mismatch,
                            Toast.LENGTH_SHORT);
                    newToast.show();
                }
            }
        });


        return rootView;
    }



    public ArrayList<Integer> checkPassword() {
        ArrayList<Integer> requirements = new ArrayList<>();
        String passwordStr = password.getText().toString();
        String confirmStr = confirmPassword.getText().toString();

        if(!passwordStr.equals(confirmStr)) {
            requirements.add(0);
        }

        if(passwordStr.length() < 8) {
            requirements.add(1);
        }

        boolean lowerCase = false;
        boolean upperCase = false;
        boolean number = false;
        for(int i = 0; i < passwordStr.length(); i++) {
            char c = passwordStr.charAt(i);

            if(Character.isLowerCase(c)) {
                lowerCase = true;
            }

            else if(Character.isUpperCase(c)) {
                upperCase = true;
            }

            else if(Character.isDigit(c)) {
                number = true;
            }
        }
        if(!lowerCase) {
            requirements.add(2);
        }
        if(!upperCase) {
            requirements.add(3);
        }
        if(!number) {
            requirements.add(4);
        }


        return requirements;
    }
}
