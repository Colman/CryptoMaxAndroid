package com.maxtechnologies.cryptomax.Main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Callbacks.TransactionCallback;
import com.maxtechnologies.cryptomax.Controllers.AlertController;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Main.ContactFragments.AddContactFragment1;
import com.maxtechnologies.cryptomax.Main.ContactFragments.ContactsFragment;
import com.maxtechnologies.cryptomax.Main.MainFragments.MarketSummaryFragment;
import com.maxtechnologies.cryptomax.Main.MainFragments.NewsFragment;
import com.maxtechnologies.cryptomax.Main.MainFragments.SettingsFragment;
import com.maxtechnologies.cryptomax.Main.MainFragments.WalletListFragment;
import com.maxtechnologies.cryptomax.Main.SendFragments.SendFragment2;
import com.maxtechnologies.cryptomax.Main.WalletFragments.AddWalletFragment1;
import com.maxtechnologies.cryptomax.Main.WalletFragments.AddWalletFragment3;
import com.maxtechnologies.cryptomax.Main.WalletFragments.SecurityFragment;
import com.maxtechnologies.cryptomax.Objects.Book;
import com.maxtechnologies.cryptomax.Objects.Candle;
import com.maxtechnologies.cryptomax.Objects.Coin;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Other.CryptoMaxApi;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.Callbacks.NetworkCallbacks;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.Wallets.Bitcoin;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Colman on 17/02/2018.
 */

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NetworkCallbacks,
                    ActivityCompat.OnRequestPermissionsResultCallback {


    //UI declaration
    private DrawerLayout drawer;
    public ActionBarDrawerToggle toggle;
    private NavigationView navigation;
    public ImageView profilePicture;
    public TextView name;
    private TextView assets;
    private ImageView arrow;
    private TextView change;
    private TextView absChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Settings.theme == 0) {
            setTheme(R.style.DefaultTheme);
        }
        else {
            setTheme(R.style.DraculaTheme);
        }
        setContentView(R.layout.activity_main);


        //Exit fullscreen
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);


        //UI definitions
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_closed);
        navigation = (NavigationView) findViewById(R.id.navigation);
        if(Settings.theme == 0) {
            navigation.inflateMenu(R.menu.menu_drawer);
        }

        else {
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
        if(CryptoMaxApi.getImage() == null) {
            Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                    "default_profile_picture", "drawable", getPackageName()));
            profilePicture.setImageDrawable(drawable);
        }
        else {
            profilePicture.setImageBitmap(CryptoMaxApi.getImage());
        }
        name.setText(CryptoMaxApi.getName());
        setAssets();


        //Setup the action bar
        drawer.addDrawerListener(toggle);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);
        navigation.setNavigationItemSelectedListener(this);


        //Set the initial fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MarketSummaryFragment fragment = MarketSummaryFragment.newInstance();
        fragmentTransaction.replace(R.id.content, fragment);
        fragmentTransaction.commit();


        //Get the balances for the wallets
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            CryptoMaxApi.getWallet(i).getBalance(new BasicCallback() {
                @Override
                public void onFailure(String reason) {
                    Log.e("Network", "Failed to get balance for reason: " + reason);
                }

                @Override
                public void onSuccess() {
                    //Do nothing
                }
            });
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        Exchange.activity = this;
        Exchange.callback = this;


        subWalletTickers();
    }



    @Override
    public void coinsCallback() {
        try {
            NetworkCallbacks fragment = (NetworkCallbacks) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.coinsCallback();
        }

        catch(java.lang.ClassCastException e) {
            //Do nothing
        }
    }



    @Override
    public void candlesCallback(ArrayList<Candle> entries, ArrayList<Float> volume) {
        try {
            NetworkCallbacks fragment = (NetworkCallbacks) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.candlesCallback(entries, volume);
        }

        catch(java.lang.ClassCastException e) {
            //Do nothing
        }
    }



    @Override
    public void tickersCallback() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setAssets();
            }
        });

        try {
            NetworkCallbacks fragment = (NetworkCallbacks) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.tickersCallback();
        }

        catch(java.lang.ClassCastException e) {
            //Do nothing
        }
    }



    @Override
    public void bookCallback(Book book) {
        try {
            NetworkCallbacks fragment = (NetworkCallbacks) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.bookCallback(book);
        }

        catch(java.lang.ClassCastException e) {
            //Do nothing
        }
    }



    @Override
    public void onPause() {
        super.onPause();

        StaticVariables.exchanges[Settings.exchangeIndex].close();
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.tutorial_button) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        Class fragmentClass;
        switch(id) {
            case R.id.markets_button:
                StaticVariables.articles = null;
                fragmentClass = MarketSummaryFragment.class;
                break;
            case R.id.wallets_button:
                StaticVariables.articles = null;
                fragmentClass = WalletListFragment.class;
                break;
            case R.id.news_button:
                fragmentClass = NewsFragment.class;
                break;
            case R.id.contacts_button:
                StaticVariables.articles = null;
                fragmentClass = ContactsFragment.class;
                break;
            default:
                StaticVariables.articles = null;
                fragmentClass = SettingsFragment.class;
        }

        String selectedFragment = fragmentManager.findFragmentById(R.id.content).getClass().toString();
        if(!fragmentClass.toString().equals(selectedFragment)) {
            Fragment fragment = null;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            }

            catch (Exception e) {
                e.printStackTrace();
            }

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.content, fragment, fragmentClass.toString());
            fragmentTransaction.commit();
        }

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            SettingsFragment fragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.permissionResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
            return;
        }
        catch(java.lang.ClassCastException e) {
            //Do nothing
        }

        try {
            AddWalletFragment1 fragment = (AddWalletFragment1) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.permissionResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
            return;
        }
        catch(java.lang.ClassCastException e) {
            //Do nothing
        }

        try {
            AddWalletFragment3 fragment = (AddWalletFragment3) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.permissionResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
            return;
        }
        catch(java.lang.ClassCastException e) {
            //Do nothing
        }

        try {
            AddContactFragment1 fragment = (AddContactFragment1) getSupportFragmentManager().findFragmentById(R.id.content);
            fragment.permissionResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
        catch(java.lang.ClassCastException e) {
            //Do nothing
        }
    }



    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int numFrag = fragmentManager.getBackStackEntryCount();

            if(numFrag > 0) {
                try {
                    NewsFragment fragment = (NewsFragment) getSupportFragmentManager().findFragmentById(R.id.content);
                    StaticVariables.articles = null;
                }

                catch(java.lang.ClassCastException e) {
                    try {
                        SecurityFragment fragment = (SecurityFragment) getSupportFragmentManager().findFragmentById(R.id.content);
                        boolean first = fragment.lastStep();
                        if(first) {
                            super.onBackPressed();
                        }
                    }

                    catch(java.lang.ClassCastException e2) {
                        try {
                            SendFragment2 fragment = (SendFragment2) getSupportFragmentManager().findFragmentById(R.id.content);
                            boolean first = fragment.lastStep();
                            if(first) {
                                super.onBackPressed();
                            }
                        }

                        catch(java.lang.ClassCastException e3) {
                            super.onBackPressed();
                        }
                    }
                }
            }

            else {
                this.moveTaskToBack(true);
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingsFragment.CHOOSE_IMAGE_REQUEST_CODE) {
            if(resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.ImageColumns.ORIENTATION
                };
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn,
                        null, null, null);
                cursor.moveToFirst();
                int dataIndex = cursor.getColumnIndex(filePathColumn[0]);
                int oriIndex = cursor.getColumnIndex(filePathColumn[1]);
                String selectedPath = cursor.getString(dataIndex);

                int orientation = 0;
                try {
                     orientation = cursor.getInt(oriIndex);
                }
                catch(Exception e) {
                    //Do nothing
                }
                cursor.close();

                Bitmap imageBmp = BitmapFactory.decodeFile(selectedPath);
                if(orientation != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    imageBmp = Bitmap.createBitmap(imageBmp , 0, 0, imageBmp.getWidth(), imageBmp.getHeight(), matrix, true);
                }

                imageBmp = cropImage(scaleImage(imageBmp, 300));
                CryptoMaxApi.setImage(imageBmp, this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
                        ((SettingsFragment) fragment).profilePicture.setImageBitmap(CryptoMaxApi.getImage());
                        ((SettingsFragment) fragment).overlayLayout.setVisibility(View.VISIBLE);
                        profilePicture.setImageBitmap(CryptoMaxApi.getImage());
                    }
                });
            }
        }
    }



    private Bitmap scaleImage(Bitmap bitmap, int length) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if(width <= height) {
            int newHeight = (int) (length * ((float) height / width));
            return Bitmap.createScaledBitmap(bitmap, length, newHeight, false);
        }

        else {
            int newWidth = (int) (length * ((float) width / height));
            return Bitmap.createScaledBitmap(bitmap, newWidth, length, false);
        }
    }



    private Bitmap cropImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if(width >= height) {
            int newX = (width - height) / 2;
            bitmap = Bitmap.createBitmap(bitmap, newX, 0, height, height);
        }

        else {
            int newY = (height - width) / 2;
            bitmap = Bitmap.createBitmap(bitmap, 0, newY, width, width);
        }


        //Crop circle
        int diameter = bitmap.getWidth();
        Bitmap output = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, diameter, diameter);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);
        canvas.drawCircle(diameter / 2, diameter / 2,
                diameter / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);


        return output;
    }



    public void subWalletTickers() {
        ArrayList<Integer> indexes = new ArrayList<>();
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            int index = Exchange.findIndex(CryptoMaxApi.getWallet(i).exchangeSymbol);
            if(!indexes.contains(index)) {
                indexes.add(index);
            }
        }
        StaticVariables.exchanges[Settings.exchangeIndex].subTickers(indexes);
    }



    public void setAssets() {
        float worth = 0;
        float before = 0;
        ArrayList<Coin> coins = StaticVariables.exchanges[Settings.exchangeIndex].coins;
        for(int i = 0; i < CryptoMaxApi.getWalletsSize(); i++) {
            Wallet wallet = CryptoMaxApi.getWallet(i);
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



    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
