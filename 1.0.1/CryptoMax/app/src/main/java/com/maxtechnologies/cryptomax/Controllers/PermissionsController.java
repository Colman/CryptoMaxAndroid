package com.maxtechnologies.cryptomax.Controllers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;

import com.maxtechnologies.cryptomax.R;

/**
 * Created by Colman on 25/01/2018.
 */

public class PermissionsController {


    public static void requestReadPermission(final Activity activity) {
        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(activity, perms, 1);
                }
            };

            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setMessage(R.string.app_name + " would like to access your images");
            dialog.setPositiveButton("Allow", listener);
            dialog.show();
        }

        else {
            String[] perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(activity, perms, 1);
        }
    }



    public static void cameraPermission(final Activity activity) {
        String perm = Manifest.permission.CAMERA;
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String[] perms = new String[]{Manifest.permission.CAMERA};
                    ActivityCompat.requestPermissions(activity, perms, 1);
                }
            };

            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setMessage(R.string.app_name + " would like to access your camera");
            dialog.setPositiveButton("Allow", listener);
            dialog.show();
        }

        else {
            String[] perms = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(activity, perms, 1);
        }
    }
}
