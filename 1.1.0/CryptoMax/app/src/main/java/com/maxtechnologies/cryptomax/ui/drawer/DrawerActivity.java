package com.maxtechnologies.cryptomax.ui.drawer;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.maxtechnologies.cryptomax.exchange.PriceListener;
import com.maxtechnologies.cryptomax.exchange.asset.AssetPair;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.exchange.asset.Asset;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.exchange.asset.Coin;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.ui.ExchangeListenerActivity;
import com.maxtechnologies.cryptomax.ui.MainActivity;
import com.maxtechnologies.cryptomax.ui.drawer.contact.ContactsFragment;
import com.maxtechnologies.cryptomax.ui.drawer.market.MarketsFragment;
import com.maxtechnologies.cryptomax.ui.drawer.news.NewsFragment;
import com.maxtechnologies.cryptomax.ui.drawer.wallet.misc.WalletsFragment;
import com.maxtechnologies.cryptomax.wallets.Wallet;
import com.maxtechnologies.cryptomax.wallets.bitcoin.Bitcoin;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

/**
 * Created by Colman on 17/02/2018.
 */

public class DrawerActivity extends ExchangeListenerActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PriceListener priceListener;

    //UI declaration
    private DrawerLayout drawer;
    public ActionBarDrawerToggle toggle;
    public ImageView profilePicture;
    public TextView name;
    private TextView assets;
    private ImageView arrow;
    private TextView change;
    private TextView absChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        int theme = preferences.getInt("theme", 0);
        if(theme == 0) {
            setTheme(R.style.DefaultTheme);
        } else {
            setTheme(R.style.DraculaTheme);
        }
        setContentView(R.layout.activity_main);


        //UI definitions
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_closed);
        NavigationView navigation = (NavigationView) findViewById(R.id.navigation);
        if(Settings.theme == 0) {
            navigation.inflateMenu(R.menu.menu_drawer);
        } else {
            navigation.inflateMenu(R.menu.menu_drawer_d);
        }
        navigation.setItemIconTintList(null);
        profilePicture = (ImageView) navigation.getHeaderView(0).findViewById(R.id.profile_picture);
        name = (TextView) navigation.getHeaderView(0).findViewById(R.id.name);
        assets = (TextView) navigation.getHeaderView(0).findViewById(R.id.assets);
        arrow = (ImageView) navigation.getHeaderView(0).findViewById(R.id.arrow);
        change = (TextView) navigation.getHeaderView(0).findViewById(R.id.change);
        absChange = (TextView) navigation.getHeaderView(0).findViewById(R.id.abs_change);


        //Fill in the UI values
        String uri = preferences.getString("profile_image", null);
        profilePicture.setImageURI(Uri.parse(uri));
        name.setText(preferences.getString("name", null));


        //Setup the action bar
        drawer.addDrawerListener(toggle);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);
        navigation.setNavigationItemSelectedListener(this);


        //Set the initial fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MarketsFragment fragment = MarketsFragment.newInstance();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();
    }



    @Override
    public void onStart() {
        super.onStart();

        ArrayList<Coin> coins = new ArrayList<>();
        if (getBitcoinsSize() > 0) {
           coins.add(getWalletCoin("Bitcoin"));
        }
        if (getEthereumsSize() > 0) {
            coins.add(getWalletCoin("Ethereum"));
        }
        if (getRipplesSize() > 0) {
            coins.add(getWalletCoin("Ripple"));
        }

        int assetPairSize = getE
        for (int i = 0; i < )
        int[] indicesArr = ArrayUtils.toPrimitive((Integer[]) indices.toArray());
        priceListener = new PriceListener() {
            @Override
            public void onFailure(String reason) {

            }

            @Override
            public void onPriceChanged(int index) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAssets();
                    }
                });
            }
        };

        exchange.subTickers(indicesArr, priceListener);
    }



    @Override
    public void onStop() {
        super.onStop();

        Exchange exchange = Exchange.getInstance();
        exchange.close();
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();
        Class selectedFragment = fragmentManager.findFragmentById(R.id.content).getClass();
        Fragment fragment;
        if (id == R.id.markets_button && !selectedFragment.equals(MarketsFragment.class)) {
            fragment = MarketsFragment.newInstance();
        } else if (id == R.id.wallets_button && !selectedFragment.equals(WalletsFragment.class)) {
            fragment = WalletsFragment.newInstance();
        } else if (id == R.id.news_button && !selectedFragment.equals(NewsFragment.class)) {
            fragment = NewsFragment.newInstance();
        } else if (id == R.id.contacts_button && !selectedFragment.equals(ContactsFragment.class)) {
            fragment = ContactsFragment.newInstance();
        } else if (id == R.id.tutorial_button && !selectedFragment.equals(ContactsFragment.class)) {
            return false;
        } else if (id == R.id.settings_button && !selectedFragment.equals(SettingsFragment.class)) {
            fragment = SettingsFragment.newInstance();
        } else {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }



    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int numFrag = fragmentManager.getBackStackEntryCount();

            if (numFrag == 0) {
                super.onBackPressed();
            } else {
                fragmentManager.popBackStack();
            }
        }
    }



    public void setAssets() {
        float worth = 0;
        float before = 0;
        ArrayList<Asset> assets = new ArrayList<>();
        CryptoMaxApi cryptoMaxApi = CryptoMaxApi.getInstance(this);
        for(int i = 0; i < cryptoMaxApi.getWalletsSize(); i++) {
            Wallet wallet = cryptoMaxApi.getWallet(i);
            int index = Exchange.findIndex(CryptoMaxApi.getWallet(i).exchangeSymbol);
            if(wallet.balance < 0) {
                continue;
            }
            float value = wallet.balance * coins.get(index).price;
            worth += value;
            before += value / ((coins.get(index).change1d / 100) + 1);
        }

        assets.setText(Exchange.fiatString(worth, true, true, true));


        String arrowPath = "green_arrow";
        if(before != 0) {
            float changeVal = ((worth / before) - 1) * 100;
            String changeStr = String.format("%.2f", changeVal) + "%";
            if(changeVal < 0) {
                changeStr = changeStr.substring(1, changeStr.length());
                arrowPath = "red_arrow";
            }
            change.setText(changeStr);
        }

        else {
            change.setText("0.00%");
        }

        Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                arrowPath, "drawable", getPackageName()));
        arrow.setImageDrawable(drawable);


        if(Settings.daily == 0) {
            String absString = Exchange.changeString(0, worth - before);
            if(worth - before >= 0) {
                absChange.setText("+ " + absString);
            }

            else {
                absChange.setText("- " + absString.substring(1, absString.length()));
            }
        }

        else {
            absChange.setText("");
        }
    }
}
