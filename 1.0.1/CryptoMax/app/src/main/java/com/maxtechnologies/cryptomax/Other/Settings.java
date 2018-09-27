package com.maxtechnologies.cryptomax.Other;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Colman on 14/01/2018.
 */

public class Settings {
    //File name declarations
    private final static String SETTINGSFILENAME = "settings.sav";

    //General declarations
    public static int exchangeIndex;
    public static int theme;
    public static int currency;
    public static int daily;
    public static int times;
    public static boolean priceFlash;
    public static int sortType;
    public static ArrayList<String> following;

    //Overlay declarations
    public static boolean smaChecked;
    public static boolean emaChecked;
    public static boolean bolChecked;
    public static boolean sarChecked;
    public static boolean rsiChecked;
    public static int[] sma;
    public static int[] ema;
    public static int[] bol;
    public static float[] sar;
    public static int[] rsi;
    public static boolean volumeChecked;


    public static void loadSettings(Context context) {
        try {
            FileInputStream stream = context.openFileInput(SETTINGSFILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            JSONObject jObj = new JSONObject(in.readLine());
            in.close();
            stream.close();
            Settings.exchangeIndex = jObj.getInt("EXCHANGEINDEX");
            Settings.theme = jObj.getInt("THEME");
            Settings.currency = jObj.getInt("CURRENCY");
            Settings.daily = jObj.getInt("DAILY");
            Settings.times = jObj.getInt("TIMES");
            Settings.priceFlash = jObj.getBoolean("PRICEFLASH");
            Settings.sortType = jObj.getInt("SORTTYPE");
            JSONArray jArr = jObj.getJSONArray("FOLLOWING");
            Settings.following = new ArrayList<>();
            for(int i = 0; i < jArr.length(); i++) {
                Settings.following.add(jArr.getString(i));
            }

            Settings.sma = new int[3];
            Settings.ema = new int[3];
            Settings.bol = new int[2];
            Settings.sar = new float[2];
            Settings.rsi = new int[2];

            Settings.smaChecked = jObj.getBoolean("SMACHECKED");
            jArr = jObj.getJSONArray("SMA");
            Settings.sma[0] = jArr.getInt(0);
            Settings.sma[1] = jArr.getInt(1);
            Settings.sma[2] = jArr.getInt(2);

            Settings.emaChecked = jObj.getBoolean("EMACHECKED");
            jArr = jObj.getJSONArray("EMA");
            Settings.ema[0] = jArr.getInt(0);
            Settings.ema[1] = jArr.getInt(1);
            Settings.ema[2] = jArr.getInt(2);

            Settings.bolChecked = jObj.getBoolean("BOLCHECKED");
            jArr = jObj.getJSONArray("BOL");
            Settings.bol[0] = jArr.getInt(0);
            Settings.bol[1] = jArr.getInt(1);

            Settings.sarChecked = jObj.getBoolean("SARCHECKED");
            jArr = jObj.getJSONArray("SAR");
            Settings.sar[0] = BigDecimal.valueOf(jArr.getDouble(0)).floatValue();
            Settings.sar[1] = BigDecimal.valueOf(jArr.getDouble(1)).floatValue();

            jArr = jObj.getJSONArray("RSI");
            Settings.rsi[0] = jArr.getInt(0);
            Settings.rsi[1] = jArr.getInt(1);

            Settings.volumeChecked = jObj.getBoolean("VOLUMECHECKED");
        }

        catch(java.io.IOException | JSONException e) {
            Settings.exchangeIndex = 0;
            Settings.theme = 1;
            Settings.currency = 30;
            Settings.daily = 0;
            Settings.times = 0;
            Settings.priceFlash = false;
            Settings.sortType = 0;
            Settings.following = new ArrayList<>();
            Settings.smaChecked = false;
            Settings.emaChecked = false;
            Settings.bolChecked = false;
            Settings.sarChecked = false;
            Settings.rsiChecked = false;
            Settings.sma = new int[] {15, 50, 0};
            Settings.ema = new int[] {10, 21, 100};
            Settings.bol = new int[] {20, 2};
            Settings.sar = new float[] {0.025f, 0.05f};
            Settings.rsi = new int[] {14, 35};
            Settings.volumeChecked = false;
        }
    }



    public static void saveSettings(Context context) {
        context.deleteFile(SETTINGSFILENAME);
        try {
            FileOutputStream stream = context.openFileOutput(SETTINGSFILENAME, 0);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            JSONObject jObj = new JSONObject();
            jObj.put("EXCHANGEINDEX", Settings.exchangeIndex);
            jObj.put("THEME", Settings.theme);
            jObj.put("CURRENCY", Settings.currency);
            jObj.put("DAILY", Settings.daily);
            jObj.put("TIMES", Settings.times);
            jObj.put("PRICEFLASH", Settings.priceFlash);
            jObj.put("SORTTYPE", Settings.sortType);
            jObj.put("FOLLOWING", new JSONArray(Settings.following));
            jObj.put("SMACHECKED", Settings.smaChecked);
            JSONArray jArr = new JSONArray();
            jArr.put(Settings.sma[0]);
            jArr.put(Settings.sma[1]);
            jArr.put(Settings.sma[2]);
            jObj.put("SMA", jArr);
            jObj.put("EMACHECKED", Settings.emaChecked);
            jArr = new JSONArray();
            jArr.put(Settings.ema[0]);
            jArr.put(Settings.ema[1]);
            jArr.put(Settings.ema[2]);
            jObj.put("EMA", jArr);
            jObj.put("BOLCHECKED", Settings.bolChecked);
            jArr = new JSONArray();
            jArr.put(Settings.bol[0]);
            jArr.put(Settings.bol[1]);
            jObj.put("BOL", jArr);
            jObj.put("SARCHECKED", Settings.sarChecked);
            jArr = new JSONArray();
            jArr.put(Settings.sar[0]);
            jArr.put(Settings.sar[1]);
            jObj.put("SAR", jArr);
            jObj.put("RSICHECKED", Settings.rsiChecked);
            jArr = new JSONArray();
            jArr.put(Settings.rsi[0]);
            jArr.put(Settings.rsi[1]);
            jObj.put("RSI", jArr);
            jObj.put("VOLUMECHECKED", Settings.volumeChecked);
            writer.write(jObj.toString());
            writer.close();
            stream.close();
        }

        catch (JSONException | java.io.IOException e) {
            context.deleteFile(SETTINGSFILENAME);
        }
    }
}
