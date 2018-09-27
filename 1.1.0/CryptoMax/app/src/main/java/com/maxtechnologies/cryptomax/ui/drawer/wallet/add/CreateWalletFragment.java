package com.maxtechnologies.cryptomax.ui.drawer.wallet.add;

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

import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.add.security.EmailFragment;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.add.security.FingerprintFragment;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.add.security.NameFragment;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.add.security.PasswordFragment;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.add.security.SecurityFragment;

public class CreateWalletFragment extends SecurityFragment {

    //UI declarations
    private ImageView currencyImage;
    private TextView currencyTitle;
    private ProgressBar progressBar1;
    private ImageView passwordImage;
    private TextView passwordTitle;
    private ProgressBar progressBar2;
    private ImageView fingerprintImage;
    private TextView fingerprintTitle;
    private ProgressBar progressBar3;
    private ImageView emailImage;
    private TextView emailTitle;


    public static CreateWalletFragment newInstance() {
        CreateWalletFragment fragment = new CreateWalletFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = R.layout.fragment_create_wallet;
        pagerAdapter = new CreateWalletFragment.CustomPagerAdapter(getChildFragmentManager());
        fragmentManager = getFragmentManager();
        numToPop = 2;
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle(R.string.generate_a_wallet);


        //UI definitions
        currencyImage = (ImageView) rootView.findViewById(R.id.currency_image);
        currencyTitle = (TextView) rootView.findViewById(R.id.currency_title);
        progressBar1 = (ProgressBar) rootView.findViewById(R.id.progress_1);
        passwordImage = (ImageView) rootView.findViewById(R.id.password_image);
        passwordTitle = (TextView) rootView.findViewById(R.id.password_title);
        progressBar2 = (ProgressBar) rootView.findViewById(R.id.progress_2);
        fingerprintImage = (ImageView) rootView.findViewById(R.id.fingerprint_image);
        fingerprintTitle = (TextView) rootView.findViewById(R.id.fingerprint_title);
        progressBar3 = (ProgressBar) rootView.findViewById(R.id.progress_3);
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
                if(previousPage == 2 && position == 1) {
                    password = null;
                }
                if(previousPage == 3 && position == 2) {
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
                    return NameFragment.newInstance();

                case 1:
                    return PasswordFragment.newInstance();

                case 2:
                    return FingerprintFragment.newInstance();

                case 3:
                    return EmailFragment.newInstance();

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
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
                currencyImage.setImageDrawable(getResources().getDrawable(R.drawable.currency_circle));
                currencyImage.setColorFilter(color);
                currencyImage.setAlpha(1f);
                currencyTitle.setTextColor(color);
                currencyTitle.setAlpha(1f);
                progressBar1.setProgress(0);

                passwordImage.setImageDrawable(getResources().getDrawable(R.drawable.password_circle));
                passwordImage.setColorFilter(color);
                passwordImage.setAlpha(alpha);
                passwordTitle.setTextColor(color);
                passwordTitle.setAlpha(alpha);
                progressBar2.setProgress(0);

                fingerprintImage.setImageDrawable(getResources().getDrawable(R.drawable.fingerprint_circle));
                fingerprintImage.setColorFilter(color);
                fingerprintImage.setAlpha(alpha);
                fingerprintTitle.setTextColor(color);
                fingerprintTitle.setAlpha(alpha);
                progressBar3.setProgress(0);

                emailImage.setImageDrawable(getResources().getDrawable(R.drawable.email_circle));
                emailImage.setColorFilter(color);
                emailImage.setAlpha(alpha);
                emailTitle.setTextColor(color);
                emailTitle.setAlpha(alpha);

                break;


            case 1:
                currencyImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                currencyImage.setColorFilter(checkedColor);
                currencyImage.setAlpha(1f);
                currencyTitle.setTextColor(checkedColor);
                currencyTitle.setAlpha(1f);
                progressBar1.setProgress(100);

                passwordImage.setImageDrawable(getResources().getDrawable(R.drawable.password_circle));
                passwordImage.setColorFilter(color);
                passwordImage.setAlpha(1f);
                passwordTitle.setTextColor(color);
                passwordTitle.setAlpha(1f);
                progressBar2.setProgress(0);

                fingerprintImage.setImageDrawable(getResources().getDrawable(R.drawable.fingerprint_circle));
                fingerprintImage.setColorFilter(color);
                fingerprintImage.setAlpha(alpha);
                fingerprintTitle.setTextColor(color);
                fingerprintTitle.setAlpha(alpha);
                progressBar3.setProgress(0);

                emailImage.setImageDrawable(getResources().getDrawable(R.drawable.email_circle));
                emailImage.setColorFilter(color);
                emailImage.setAlpha(alpha);
                emailTitle.setTextColor(color);
                emailTitle.setAlpha(alpha);

                break;


            case 2:
                currencyImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                currencyImage.setColorFilter(checkedColor);
                currencyImage.setAlpha(1f);
                currencyTitle.setTextColor(checkedColor);
                currencyTitle.setAlpha(1f);
                progressBar1.setProgress(100);

                passwordImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                passwordImage.setColorFilter(checkedColor);
                passwordImage.setAlpha(1f);
                passwordTitle.setTextColor(checkedColor);
                passwordTitle.setAlpha(1f);
                progressBar2.setProgress(100);

                fingerprintImage.setImageDrawable(getResources().getDrawable(R.drawable.fingerprint_circle));
                fingerprintImage.setColorFilter(color);
                fingerprintImage.setAlpha(1f);
                fingerprintTitle.setTextColor(color);
                fingerprintTitle.setAlpha(1f);
                progressBar3.setProgress(0);

                emailImage.setImageDrawable(getResources().getDrawable(R.drawable.email_circle));
                emailImage.setColorFilter(color);
                emailImage.setAlpha(alpha);
                emailTitle.setTextColor(color);
                emailTitle.setAlpha(alpha);

                break;


            default:
                currencyImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                currencyImage.setColorFilter(checkedColor);
                currencyImage.setAlpha(1f);
                currencyTitle.setTextColor(checkedColor);
                currencyTitle.setAlpha(1f);
                progressBar1.setProgress(100);

                passwordImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                passwordImage.setColorFilter(checkedColor);
                passwordImage.setAlpha(1f);
                passwordTitle.setTextColor(checkedColor);
                passwordTitle.setAlpha(1f);
                progressBar2.setProgress(100);

                fingerprintImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                fingerprintImage.setColorFilter(checkedColor);
                fingerprintImage.setAlpha(1f);
                fingerprintTitle.setTextColor(checkedColor);
                fingerprintTitle.setAlpha(1f);
                progressBar3.setProgress(100);

                emailImage.setImageDrawable(getResources().getDrawable(R.drawable.email_circle));
                emailImage.setColorFilter(color);
                emailImage.setAlpha(1f);
                emailTitle.setTextColor(color);
                emailTitle.setAlpha(1f);

                break;
        }
    }
}
