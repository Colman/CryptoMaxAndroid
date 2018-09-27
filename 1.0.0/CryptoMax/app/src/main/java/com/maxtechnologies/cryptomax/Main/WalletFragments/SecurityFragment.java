package com.maxtechnologies.cryptomax.Main.WalletFragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Main.MainActivity;
import com.maxtechnologies.cryptomax.Objects.LockableViewPager;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.OtherTools;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Wallet;
import com.google.zxing.WriterException;

/**
 * Created by Colman on 25/05/2018.
 */

public abstract class SecurityFragment extends Fragment {

    //Layout declaration
    protected int layout;
    protected FragmentManager fragmentManager;
    protected int numToPop;

    //Wallet declarations
    public Wallet wallet;
    private String privateKey;
    protected int previousPage;
    public String password;
    public boolean fingerprint;
    public String email;

    //UI declarations
    private ConstraintLayout mainLayout;
    protected LockableViewPager pager;
    protected PagerAdapter pagerAdapter;
    private ScrollView agreementLayout;
    private Button disagreeButton;
    private Button agreeButton;
    private ConstraintLayout qrLayout;
    private ImageView qrCode;
    private Button copyButton;
    private Button finishButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_security, container, false);
        ViewGroup childView = (ViewGroup) inflater.inflate(
                layout, container, false);
        rootView.addView(childView, 0);
        setHasOptionsMenu(true);


        //Wallet definition
        previousPage = -1;

        //UI definitions
        mainLayout = (ConstraintLayout) childView;
        pager = (LockableViewPager) rootView.findViewById(R.id.pager);
        pager.setSwipeEnabled(false);
        pager.setAdapter(pagerAdapter);
        agreementLayout = (ScrollView) rootView.findViewById(R.id.agreement_layout);
        disagreeButton = (Button) rootView.findViewById(R.id.disagree_button);
        agreeButton = (Button) rootView.findViewById(R.id.agree_button);
        qrLayout = (ConstraintLayout) rootView.findViewById(R.id.qr_layout);
        qrCode = (ImageView) rootView.findViewById(R.id.qr_code);
        copyButton = (Button) rootView.findViewById(R.id.copy_button);
        finishButton = (Button) rootView.findViewById(R.id.finish_button);


        //Setup the listener for the disagree button
        disagreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agreementLayout.setVisibility(View.INVISIBLE);
                mainLayout.setVisibility(View.VISIBLE);
            }
        });


        //Setup the listener for the agree button
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                agreementLayout.setVisibility(View.INVISIBLE);

                privateKey = wallet.decrypt("TEMP");

                Bitmap qrCodeImage;
                try {
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    qrCodeImage = OtherTools.encodeAsBitmap(privateKey, displayMetrics.widthPixels);
                }

                catch (WriterException e) {
                    qrCodeImage = null;
                }
                qrCode.setImageBitmap(qrCodeImage);

                qrLayout.setVisibility(View.VISIBLE);
            }
        });


        //Setup the listener for the copy button
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("PRIVATE", privateKey);
                clipboard.setPrimaryClip(clip);

                Toast newToast = Toast.makeText(getActivity(),
                        R.string.copied_to_clipboard,
                        Toast.LENGTH_LONG);
                newToast.show();
            }
        });


        //Setup the listener for the finish button
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wallet.encrypt(privateKey, password, fingerprint, email);
                CryptoMaxApi.addWallet(wallet, getContext());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) getActivity()).subWalletTickers();
                        ((MainActivity) getActivity()).setAssets();

                        int count = fragmentManager.getBackStackEntryCount();
                        fragmentManager.popBackStack(count - numToPop, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                });
            }
        });


        return rootView;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }



    public void nextStep() {
        int current = pager.getCurrentItem();
        if (current < pagerAdapter.getCount() - 1) {
            pager.setCurrentItem(current + 1, true);
        }

        else {
            mainLayout.setVisibility(View.INVISIBLE);
            agreementLayout.setVisibility(View.VISIBLE);
        }
    }



    public boolean lastStep() {
        int current = pager.getCurrentItem();
        if(current != 0) {
            if(agreementLayout.getVisibility() == View.VISIBLE) {
                agreementLayout.setVisibility(View.INVISIBLE);
                mainLayout.setVisibility(View.VISIBLE);
            }

            else if(qrLayout.getVisibility() == View.VISIBLE) {
                qrLayout.setVisibility(View.INVISIBLE);
                agreementLayout.setVisibility(View.VISIBLE);
            }

            else {
                pager.setCurrentItem(current - 1, true);
            }

            return false;
        }

        else {
            return true;
        }
    }



    protected abstract void setProgress(int progress);
}
