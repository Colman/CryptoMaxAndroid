package com.maxtechnologies.cryptomax.ui.drawer.wallet.send.step2;

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

import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.ui.misc.LockableViewPager;
import com.maxtechnologies.cryptomax.R;

public class SendFragment2 extends Fragment {

    //Fragment declaration
    public int numToPop;

    //Transaction declarations
    public int fromIndex;
    public String toAddress;
    public float amount;
    public float fee;
    public String password;

    //Fragment declarations
    private AmountFragment amountFragment;
    private AuthenticateFragment authenticateFragment;
    private ConfirmFragment confirmFragment;

    //UI declarations
    private LockableViewPager pager;
    private CustomPagerAdapter adapter;
    private ImageView recipientImage;
    private TextView recipientTitle;
    private ImageView amountImage;
    private TextView amountTitle;
    private ProgressBar progressBar2;
    private ImageView authenticateImage;
    private TextView authenticateTitle;
    private ProgressBar progressBar3;
    private ImageView confirmImage;
    private TextView confirmTitle;


    public static SendFragment2 newInstance(String address, int numToPop) {
        SendFragment2 fragment = new SendFragment2();
        Bundle bundle = new Bundle();
        bundle.putString("ADDRESS", address);
        bundle.putInt("NUMTOPOP", numToPop);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_send_2, container, false);
        setTitle(2);

        //Get the arguments
        Bundle bundle = getArguments();

        //Transaction definition
        toAddress = bundle.getString("ADDRESS");

        //Fragment definition
        numToPop = bundle.getInt("NUMTOPOP");

        //Fragment definitions
        amountFragment = AmountFragment.newInstance();
        authenticateFragment = AuthenticateFragment.newInstance();
        confirmFragment = ConfirmFragment.newInstance();

        //UI definitions
        pager = (LockableViewPager) rootView.findViewById(R.id.pager);
        adapter = new CustomPagerAdapter(getChildFragmentManager());
        pager.setSwipeEnabled(false);
        pager.setAdapter(adapter);
        recipientImage = (ImageView) rootView.findViewById(R.id.recipient_image);
        recipientTitle = (TextView) rootView.findViewById(R.id.recipient_title);
        amountImage = (ImageView) rootView.findViewById(R.id.amount_image);
        amountTitle = (TextView) rootView.findViewById(R.id.amount_title);
        progressBar2 = (ProgressBar) rootView.findViewById(R.id.progress_2);
        authenticateImage = (ImageView) rootView.findViewById(R.id.authenticate_image);
        authenticateTitle = (TextView) rootView.findViewById(R.id.authenticate_title);
        progressBar3 = (ProgressBar) rootView.findViewById(R.id.progress_3);
        confirmImage = (ImageView) rootView.findViewById(R.id.confirm_image);
        confirmTitle = (TextView) rootView.findViewById(R.id.confirm_title);


        //Set the UI values
        if(Settings.theme == 0) {
            int color = getResources().getColor(R.color.colorAccent);
            recipientImage.setColorFilter(color);
            recipientTitle.setTextColor(color);
        }

        else {
            int color = getResources().getColor(R.color.colorDraculaAccent);
            recipientImage.setColorFilter(color);
            recipientTitle.setTextColor(color);
        }


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
                    return amountFragment;

                case 1:
                    return authenticateFragment;

                case 2:
                    return confirmFragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }



    public void nextStep() {
        int current = pager.getCurrentItem();
        if (current < adapter.getCount() - 1) {
            pager.setCurrentItem(current + 1, true);
            setTitle(current + 3);
        }

        else {
            FragmentManager fragmentManager = getFragmentManager();
            int count = fragmentManager.getBackStackEntryCount();
            fragmentManager.popBackStack(count - numToPop, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }



    public boolean lastStep() {
        int current = pager.getCurrentItem();
        if(current == 0) {
            return true;
        }

        else if(current == 1) {
            AuthenticateFragment fragment = (AuthenticateFragment) adapter.getItem(1);
            boolean first = fragment.lastStep();
            if(first) {
                pager.setCurrentItem(0, true);
                setTitle(2);
            }
        }

        else {
            pager.setCurrentItem(current - 1, true);
            setTitle(current + 1);
        }

        return false;
    }



    private void setTitle(int step) {
        if(step == 2) {
            getActivity().setTitle(R.string.send_title_2);
        }
        else if(step == 3) {
            getActivity().setTitle(R.string.send_title_3);
        }
        else if(step == 4) {
            getActivity().setTitle(R.string.send_title_4);
        }
    }



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
                amountImage.setImageDrawable(getResources().getDrawable(R.drawable.currency_circle));
                amountImage.setColorFilter(color);
                amountImage.setAlpha(1f);
                amountTitle.setTextColor(color);
                amountTitle.setAlpha(1f);
                progressBar2.setProgress(0);

                authenticateImage.setImageDrawable(getResources().getDrawable(R.drawable.password_circle));
                authenticateImage.setColorFilter(color);
                authenticateImage.setAlpha(alpha);
                authenticateTitle.setTextColor(color);
                authenticateTitle.setAlpha(alpha);
                progressBar3.setProgress(0);

                confirmImage.setImageDrawable(getResources().getDrawable(R.drawable.confirm_circle));
                confirmImage.setColorFilter(color);
                confirmImage.setAlpha(alpha);
                confirmTitle.setTextColor(color);
                confirmTitle.setAlpha(alpha);

                break;


            case 1:
                amountImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                amountImage.setColorFilter(checkedColor);
                amountImage.setAlpha(1f);
                amountTitle.setTextColor(checkedColor);
                amountTitle.setAlpha(1f);
                progressBar2.setProgress(100);

                authenticateImage.setImageDrawable(getResources().getDrawable(R.drawable.password_circle));
                authenticateImage.setColorFilter(color);
                authenticateImage.setAlpha(1f);
                authenticateTitle.setTextColor(color);
                authenticateTitle.setAlpha(1f);
                progressBar3.setProgress(0);

                confirmImage.setImageDrawable(getResources().getDrawable(R.drawable.confirm_circle));
                confirmImage.setColorFilter(color);
                confirmImage.setAlpha(alpha);
                confirmTitle.setTextColor(color);
                confirmTitle.setAlpha(alpha);

                break;


            default:
                amountImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                amountImage.setColorFilter(checkedColor);
                amountImage.setAlpha(1f);
                amountTitle.setTextColor(checkedColor);
                amountTitle.setAlpha(1f);
                progressBar2.setProgress(100);

                authenticateImage.setImageDrawable(getResources().getDrawable(R.drawable.check_circle));
                authenticateImage.setColorFilter(checkedColor);
                authenticateImage.setAlpha(1f);
                authenticateTitle.setTextColor(checkedColor);
                authenticateTitle.setAlpha(1f);
                progressBar3.setProgress(100);

                confirmImage.setImageDrawable(getResources().getDrawable(R.drawable.confirm_circle));
                confirmImage.setColorFilter(color);
                confirmImage.setAlpha(1f);
                confirmTitle.setTextColor(color);
                confirmTitle.setAlpha(1f);

                break;
        }
    }
}
