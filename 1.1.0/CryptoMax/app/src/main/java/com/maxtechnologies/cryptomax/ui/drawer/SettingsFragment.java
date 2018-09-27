package com.maxtechnologies.cryptomax.ui.drawer;

import android.Manifest;
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.misc.Permissions;
import com.maxtechnologies.cryptomax.misc.Settings;
import com.maxtechnologies.cryptomax.ui.misc.CustomSpinnerAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsFragment extends Fragment {
    //Request codes for startingActivityForResult
    public static final int CHOOSE_IMAGE_REQUEST_CODE = 100;

    //UI declarations
    public ConstraintLayout overlayLayout;
    public ImageView profilePicture;
    private ImageView border;
    private Button changeButton;
    private EditText name;
    private Spinner currency;
    private ArrayList<String> currencies;
    private ArrayAdapter<String> cAdapter;
    private Spinner daily;
    private ArrayList<String> dailies;
    private ArrayAdapter<String> dAdapter;
    private Spinner theme;
    private ArrayList<String> themes;
    private ArrayAdapter<String> thAdapter;
    private ArrayList<String> timesList;
    private ArrayAdapter<String> tAdapter;
    private Spinner times;
    private CheckBox priceFlash;


    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
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



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_settings, container, false);
        getActivity().setTitle(R.string.settings);


        //UI definitions
        profilePicture = (ImageView) rootView.findViewById(R.id.profile_picture);
        overlayLayout = (ConstraintLayout) rootView.findViewById(R.id.overlay_layout);
        border = (ImageView) rootView.findViewById(R.id.border);
        changeButton = (Button) rootView.findViewById(R.id.change_button);
        name = (EditText) rootView.findViewById(R.id.name);
        currency = (Spinner) rootView.findViewById(R.id.currency);
        currencies = new ArrayList<>();
        for(int i = 0; i < Exchange.fiats.length; i++) {
            currencies.add(Exchange.fiats[i].name);
        }
        cAdapter = new CustomSpinnerAdapter(getActivity(), currencies);
        currency.setAdapter(cAdapter);
        dailies = new ArrayList<>();
        dailies.addAll(Arrays.asList("Both", "Percent Only"));
        daily = (Spinner) rootView.findViewById(R.id.daily);
        dAdapter = new CustomSpinnerAdapter(getActivity(), dailies);
        daily.setAdapter(dAdapter);
        themes = new ArrayList<>();
        themes.addAll(Arrays.asList("Default", "Dracula"));
        theme = (Spinner) rootView.findViewById(R.id.theme);
        thAdapter = new CustomSpinnerAdapter(getActivity(), themes);
        theme.setAdapter(thAdapter);
        timesList = new ArrayList<>();
        timesList.addAll(Arrays.asList("12 Hour", "24 Hour"));
        times = (Spinner) rootView.findViewById(R.id.times);
        tAdapter = new CustomSpinnerAdapter(getActivity(), timesList);
        times.setAdapter(tAdapter);
        priceFlash = (CheckBox) rootView.findViewById(R.id.flash_box);


        //Set the UI values
        if(CryptoMaxApi.getImage() == null) {
            Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                    "default_profile_picture", "drawable", getActivity().getPackageName()));
            profilePicture.setImageDrawable(drawable);
            overlayLayout.setVisibility(View.INVISIBLE);
        }
        else {
            profilePicture.setImageBitmap(CryptoMaxApi.getImage() );
            overlayLayout.setVisibility(View.VISIBLE);
        }

        Drawable drawable;
        try {
            if(Settings.theme == 0) {
                drawable = getResources().getDrawable(getResources().getIdentifier(
                        "image_border_black", "drawable", getActivity().getPackageName()));
            }

            else {
                drawable = getResources().getDrawable(getResources().getIdentifier(
                        "image_border_white", "drawable", getActivity().getPackageName()));
            }
            border.setImageDrawable(drawable);
        }
        catch (Exception e) {
            //Do nothing
        }

        name.setText(CryptoMaxApi.getName());
        currency.setSelection(Settings.currency);
        daily.setSelection(Settings.daily);
        theme.setSelection(Settings.theme);
        times.setSelection(Settings.times);
        priceFlash.setChecked(Settings.priceFlash);


        //Setup the listener for the profile picture
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CryptoMaxApi.getImage()  != null) {
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier(
                            "default_profile_picture", "drawable", getActivity().getPackageName()));
                    profilePicture.setImageDrawable(drawable);
                    ((DrawerActivity) getActivity()).profilePicture.setImageDrawable(drawable);
                    overlayLayout.setVisibility(View.INVISIBLE);

                    CryptoMaxApi.setImage(null, getContext());
                }
            }
        });


        //Setup the listener for the change button
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission = ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);

                if (permission == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    getActivity().startActivityForResult(intent, CHOOSE_IMAGE_REQUEST_CODE);
                }

                else {
                    Permissions.requestReadPermission(getActivity());
                }
            }
        });


        //Setup the focus listener for the name field
        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b) {
                    CryptoMaxApi.setName(name.getText().toString(), getContext());
                    ((DrawerActivity) getActivity()).name.setText(CryptoMaxApi.getName());
                }
            }
        });


        //Setup the listener for the currency spinner
        currency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Settings.currency = i;
                Settings.saveSettings(getActivity());

                ((DrawerActivity) getActivity()).tickersCallback();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the daily spinner
        daily.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Settings.daily = i;
                Settings.saveSettings(getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the theme spinner
        theme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(Settings.theme != i) {
                    Settings.theme = i;
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    for(int j = 0; j < manager.getBackStackEntryCount(); j++) {
                        manager.popBackStack();
                    }
                    getActivity().recreate();
                    Settings.saveSettings(getActivity());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the times spinner
        times.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Settings.times = i;
                Settings.saveSettings(getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Do nothing
            }
        });


        //Setup the listener for the flash box
        priceFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings.priceFlash = b;
                Settings.saveSettings(getActivity());
            }
        });


        return rootView;
    }



    public void permissionResult(boolean granted) {
        if(granted) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            getActivity().startActivityForResult(intent, CHOOSE_IMAGE_REQUEST_CODE);
        }
    }
}
