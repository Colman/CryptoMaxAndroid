package com.maxtechnologies.cryptomax.ui.drawer.wallet.view;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.misc.BasicCallback;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.ui.misc.AlertController;
import com.maxtechnologies.cryptomax.exchange.book.Book;
import com.maxtechnologies.cryptomax.exchange.candle.Candle;
import com.maxtechnologies.cryptomax.R;
import com.google.zxing.WriterException;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.send.step1.SendFragment1;
import com.maxtechnologies.cryptomax.misc.MiscUtils;
import com.maxtechnologies.cryptomax.wallets.Wallet;

import java.util.ArrayList;

public class ViewWalletFragment extends Fragment implements NetworkCallbacks {

    //Wallet declaration
    public int walletIndex;

    //UI declarations
    private RelativeLayout walletHeader;
    private ImageView logo;
    private TextView balance;
    private TextView value;
    private TabLayout tabLayout;
    private ViewPager pager;
    private PagerAdapter pagerAdapter;
    private ConstraintLayout qrLayout;
    private ImageView qrCode;
    private TextView qrAddress;
    private Button copyButton;



    public static ViewWalletFragment newInstance(int index) {
        ViewWalletFragment fragment = new ViewWalletFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("INDEX", index);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_view_wallet, container, false);
        setHasOptionsMenu(true);


        //Un-bundle the wallets object and index
        Bundle bundle = getArguments();
        walletIndex = bundle.getInt("INDEX");

        //UI definitions
        walletHeader = (RelativeLayout) rootView.findViewById(R.id.wallet_header);
        logo = (ImageView) rootView.findViewById(R.id.logo);
        balance = (TextView) rootView.findViewById(R.id.balance);
        value = (TextView) rootView.findViewById(R.id.value);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        pagerAdapter = new CustomPagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(pager);
        qrLayout = (ConstraintLayout) rootView.findViewById(R.id.qr_layout);
        qrCode = (ImageView) rootView.findViewById(R.id.qr_code);
        qrAddress = (TextView) rootView.findViewById(R.id.qr_address);
        copyButton = (Button) rootView.findViewById(R.id.copy_button);


        //Wallet definition
        final Wallet wallet = CryptoMaxApi.getWallet(walletIndex);

        //Fill in the UI values
        getActivity().setTitle(wallet.name);
        if(Settings.theme == 0) {
            walletHeader.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        else {
            walletHeader.setBackgroundResource(R.color.colorPrimaryDark);
        }
        Drawable drawable;
        String symbol = Exchange.translateToSymbol(wallet.exchangeSymbol).toLowerCase();
        if(Settings.theme == 0) {
            try {
                drawable = getResources().getDrawable(getResources().getIdentifier(
                        symbol, "drawable", getActivity().getPackageName()));
            }

            catch (Exception e) {
                drawable = getResources().getDrawable(getResources().getIdentifier(
                        "logo", "drawable", getActivity().getPackageName()));
            }
        }

        else {
            try {
                drawable = getResources().getDrawable(getResources().getIdentifier(
                        symbol + "_d", "drawable", getActivity().getPackageName()));
            }

            catch (Exception e) {
                try {
                    drawable = getResources().getDrawable(getResources().getIdentifier(
                            symbol, "drawable", getActivity().getPackageName()));
                }

                catch (Exception e2) {
                    drawable = getResources().getDrawable(getResources().getIdentifier(
                            "logo_d", "drawable", getActivity().getPackageName()));
                }
            }
        }
        logo.setImageDrawable(drawable);
        setBalanceAndValue();
        qrAddress.setText(CryptoMaxApi.getWallet(walletIndex).address);
        Bitmap qrCodeImage;
        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            qrCodeImage = MiscUtils.encodeAsBitmap(CryptoMaxApi.getWallet(walletIndex).address, displayMetrics.widthPixels);
        }

        catch (WriterException e) {
            qrCodeImage = null;
        }
        qrCode.setImageBitmap(qrCodeImage);


        //Initiate network callback
        ArrayList<Integer> indexes = new ArrayList<>();
        indexes.add(Exchange.findIndex(wallet.exchangeSymbol));
        StaticVariables.exchanges[Settings.exchangeIndex].subTickers(indexes);


        //Setup the listener for the wallet layout
        qrLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrLayout.setVisibility(View.INVISIBLE);
            }
        });


        //Setup the listener for the pager tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //Do nothing
            }
        });


        //Setup the listener for the copy button
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ADDRESS", wallet.address);
                clipboard.setPrimaryClip(clip);
            }
        });


        return rootView;
    }



    @Override
    public void coinsCallback() {
        //Do nothing
    }



    @Override
    public void candlesCallback(ArrayList<Candle> entries, ArrayList<Float> volume) {
        //Do nothing
    }



    @Override
    public void tickersCallback() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBalanceAndValue();
            }
        });
    }



    @Override
    public void bookCallback(Book book) {
        //Do nothing
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_view_wallet, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.send_button:
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                SendFragment1 fragment = SendFragment1.newInstance(walletIndex);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content, fragment);
                fragmentTransaction.commit();
                return true;

            case R.id.qr_code_button:
                if(qrLayout.getVisibility() == View.INVISIBLE) {
                    qrLayout.setVisibility(View.VISIBLE);
                }
                else {
                    qrLayout.setVisibility(View.INVISIBLE);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public void updateBalance() {
        CryptoMaxApi.getWallet(walletIndex).getBalance(new BasicCallback() {
            @Override
            public void onFailure(String reason) {
                Log.e("Network", "Failed to get wallet balance for reason: " + reason);
                AlertController.networkError(getActivity(), true);
            }

            @Override
            public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBalanceAndValue();
                    }
                });
            }
        });
    }



    private void setBalanceAndValue() {
        Wallet wallet = CryptoMaxApi.getWallet(walletIndex);
        int index = Exchange.findIndex(wallet.exchangeSymbol);
        if(wallet.balance != -1) {
            balance.setText(Exchange.coinString(wallet.balance, index, true, true));
            float price = StaticVariables.exchanges[Settings.exchangeIndex].coins.get(index).price;
            value.setText(Exchange.fiatString(wallet.balance * price, true, true, true));
        }
        else {
            balance.setText(R.string.unknown_2);
            balance.setTypeface(null, Typeface.ITALIC);
            value.setText(R.string.unknown_2);
            value.setTypeface(null, Typeface.ITALIC);
        }
    }



    private class CustomPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<String> titleList;


        public CustomPagerAdapter(FragmentManager manager) {
            super(manager);
            titleList = new ArrayList<>();
            titleList.add("Transactions");
            titleList.add("Settings");
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return TransactionsFragment.newInstance(walletIndex);

                case 1:
                    return SettingsFragment.newInstance(walletIndex);

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return titleList.get(position);
        }
    }
}
