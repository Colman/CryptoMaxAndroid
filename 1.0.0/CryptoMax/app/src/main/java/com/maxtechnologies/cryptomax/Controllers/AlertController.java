package com.maxtechnologies.cryptomax.Controllers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Other.Settings;
import com.maxtechnologies.cryptomax.R;

/**
 * Created by Colman on 08/01/2018.
 */

public class AlertController {

    public static void networkError(final Activity activity, final boolean closeApp) {
        int theme = AlertDialog.THEME_HOLO_LIGHT;
        if(Settings.theme == 1) {
            theme = AlertDialog.THEME_HOLO_DARK;
        }
        final int themeF = theme;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity, themeF);
                builder.setTitle(R.string.network_error)
                        .setMessage(R.string.network_message)
                        .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if(closeApp) {
                                    System.exit(0);
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }



    public static void tooManyRequestsError(final Activity activity) {
        int theme = AlertDialog.THEME_HOLO_LIGHT;
        if(Settings.theme == 1) {
            theme = AlertDialog.THEME_HOLO_DARK;
        }
        final int themeF = theme;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity, themeF);
                builder.setTitle(R.string.too_many_requests_error)
                        .setMessage(R.string.too_many_requests_message)
                        .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }
}
