package com.maxtechnologies.cryptomax.Main.WalletFragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;


/**
 * Created by Colman on 03/05/2018.
 */

public class AddWalletFragment4 extends SecurityFragment {

    //UI declarations
    private ImageView passwordImage;
    private TextView passwordTitle;
    private ProgressBar progressBar1;
    private ImageView fingerprintImage;
    private TextView fingerprintTitle;
    private ProgressBar progressBar2;
    private ImageView emailImage;
    private TextView emailTitle;


    public static AddWalletFragment4 newInstance(Wallet wallet) {
        AddWalletFragment4 fragment = new AddWalletFragment4();
        Bundle bundle = new Bundle();
        bundle.putSerializable("WALLET", wallet);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = R.layout.fragment_add_wallet_4;
        pagerAdapter = new AddWalletFragment4.CustomPagerAdapter(getChildFragmentManager());
        fragmentManager = getFragmentManager();
        numToPop = 4;
        wallet = (Wallet) getArguments().getSerializable("WALLET");
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle(R.string.add_a_wallet);


        //UI definitions
        passwordImage = (ImageView) rootView.findViewById(R.id.password_image);
        passwordTitle = (TextView) rootView.findViewById(R.id.password_title);
        progressBar1 = (ProgressBar) rootView.findViewById(R.id.progress_1);
        fingerprintImage = (ImageView) rootView.findViewById(R.id.fingerprint_image);
        fingerprintTitle = (TextView) rootView.findViewById(R.id.fingerprint_title);
        progressBar2 = (ProgressBar) rootView.findViewById(R.id.progress_2);
        emailImage = (ImageView) rootView.findViewById(R.id.email_image);
        emailTitle = (TextView) rootView.findViewById(R.id.email_title);


        //Set the initial progress
        setProgress(0);


        //Setup the listener for the pager
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                if(previousPage == 1 && position == 0) {
                    password = null;
                }
                if(previousPage == 2 && position == 1) {
                    fingerprint = false;
                }

                previousPage = position;
                setProgress(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Do nothing
            }
        });


        return rootView;
    }



    private class CustomPagerAdapter extends FragmentPagerAdapter {

        public CustomPagerAdapter(FragmentManager manager) {
            super(manager);
        }


        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return PasswordFragment.newInstance();

                case 1:
                    return FingerprintFragment.newInstance();

                case 2:
                    return EmailFragment.newInstance();

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }



    @Override
    protected void setProgress(int position) {
        int color = Color.BLACK;
        int checkedColor = getResources().getColor(R.color.colorAccent);
        float alpha = 0.4f;
        if(Settings.theme != 0) {
            color = Color.WHITE;
            checkedColor = getResources().getColor(R.color.colorDraculaAccent);
            alpha = 0.6f;
        }

        switch(position) {
            case 0:
                passwordImage.setImageDrawable(getResources().getDrawable(R.drawable.password_circle));
                passwordImage.setColorFilter(color);
                passwordImage.setAlpha(1f);
                passwordTitle.setTextColor(color);
                passwordTitle.setAlpha(1f);
                progressBar1.setProgress(0);

                fingerprintImage.setImageDrawable(getResources().getDrawable(R.drawable.fingerprint_circle));
                fingerprintImage.setColorFilter(color);
                fingerprintImage.setAlpha(alpha);
                fingerprintTitle.setTextColor(color);
                fingerprintTitle.setAlpha(alpha);
                progressBar2.setProgress(0);

                emailImage.setImageDrawable(getResources().getDrawable(R.drawable.email_circle));
                emailImage.setColorFilter(color);
                emailImage.setAlpha(alpha);
                emailTitle.setTextColor(color);
                emailTitle.setAlpha(alpha);

                break;


            case 1:
                passwordImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                passwordImage.setColorFilter(checkedColor);
                passwordImage.setAlpha(1f);
                passwordTitle.setTextColor(checkedColor);
                passwordTitle.setAlpha(1f);
                progressBar1.setProgress(100);

                fingerprintImage.setImageDrawable(getResources().getDrawable(R.drawable.fingerprint_circle));
                fingerprintImage.setColorFilter(color);
                fingerprintImage.setAlpha(1f);
                fingerprintTitle.setTextColor(color);
                fingerprintTitle.setAlpha(1f);
                progressBar2.setProgress(0);

                emailImage.setImageDrawable(getResources().getDrawable(R.drawable.email_circle));
                emailImage.setColorFilter(color);
                emailImage.setAlpha(alpha);
                emailTitle.setTextColor(color);
                emailTitle.setAlpha(alpha);

                break;


            default:
                passwordImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                passwordImage.setColorFilter(checkedColor);
                passwordImage.setAlpha(1f);
                passwordTitle.setTextColor(checkedColor);
                passwordTitle.setAlpha(1f);
                progressBar1.setProgress(100);

                fingerprintImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                fingerprintImage.setColorFilter(checkedColor);
                fingerprintImage.setAlpha(1f);
                fingerprintTitle.setTextColor(checkedColor);
                fingerprintTitle.setAlpha(1f);
                progressBar2.setProgress(100);

                emailImage.setImageDrawable(getResources().getDrawable(R.drawable.email_circle));
                emailImage.setColorFilter(color);
                emailImage.setAlpha(1f);
                emailTitle.setTextColor(color);
                emailTitle.setAlpha(1f);

                break;
        }
    }
}
