package com.maxtechnologies.cryptomax.Main.ContactFragments;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Callbacks.ProfilesCallback;
import com.maxtechnologies.cryptomax.Controllers.AlertController;
import com.maxtechnologies.cryptomax.Objects.Contact;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Colman on 01/05/2018.
 */

public class AddContactFragment2 extends Fragment {

    //UI declarations
    private ImageView profileImage;
    private ImageView imageBorder;
    private TextView name;
    private TextView address;
    private EditText memo;
    private Button finishButton;


    public static AddContactFragment2 newInstance(String symbol, String address) {
        AddContactFragment2 fragment = new AddContactFragment2();
        Bundle bundle = new Bundle();
        bundle.putString("SYMBOL", symbol);
        bundle.putString("ADDRESS", address);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_add_contact_2, container, false);
        getActivity().setTitle(R.string.add_a_contact);

        //Un-bundle the symbol and address
        Bundle bundle = getArguments();
        final String symbolStr = bundle.getString("SYMBOL");
        final String addressStr = bundle.getString("ADDRESS");

        //UI definitions
        profileImage = (ImageView) rootView.findViewById(R.id.profile_image);
        imageBorder = (ImageView) rootView.findViewById(R.id.image_border);
        name = (TextView) rootView.findViewById(R.id.name);
        address = (TextView) rootView.findViewById(R.id.address);
        memo = (EditText) rootView.findViewById(R.id.memo);
        finishButton = (Button) rootView.findViewById(R.id.finish_button);


        //Fetch the name and profile picture
        ArrayList<String> addressList = new ArrayList<>();
        addressList.add(addressStr);
        CryptoMaxApi.getProfiles(addressList, new ProfilesCallback() {
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
                                name.setText(names[0]);
                                name.setTypeface(null, Typeface.NORMAL);
                            }

                            if(images[0] != null) {
                                profileImage.setImageBitmap(images[0]);
                            }
                        }
                    });
                }
            }
        });


        //Set the UI values
        address.setText(addressStr);
        Drawable drawable;
        if(Settings.theme == 0) {
            drawable = getContext().getResources().getDrawable(getContext().getResources().getIdentifier(
                    "image_border_black", "drawable", getContext().getPackageName()));
        }

        else {
            drawable = getContext().getResources().getDrawable(getContext().getResources().getIdentifier(
                    "image_border_white", "drawable", getContext().getPackageName()));
        }
        imageBorder.setImageDrawable(drawable);


        //Setup the listener for the next button
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Contact contact = new Contact(symbolStr, addressStr, memo.getText().toString(), new Date());
                StaticVariables.addContact(contact, getContext());

                FragmentManager fragmentManager = getFragmentManager();
                int count = fragmentManager.getBackStackEntryCount();
                fragmentManager.popBackStack(count - 2, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });


        return rootView;
    }
}
