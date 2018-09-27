package com.maxtechnologies.cryptomax.Callbacks;

import android.graphics.Bitmap;

import com.maxtechnologies.cryptomax.Objects.Transaction;

import java.util.ArrayList;

/**
 * Created by Colman on 15/05/2018.
 */

public interface TransactionCallback {
    int INCORRECT_PASSWORD = 0;
    int INSUFFICIENT_FUNDS = 1;
    int INVALID_DEST_ADDRESS = 2;


    void onFailure(int code, String message);

    void onSuccess(String hash);
}
