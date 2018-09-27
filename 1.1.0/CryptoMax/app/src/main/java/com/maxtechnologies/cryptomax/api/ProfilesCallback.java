package com.maxtechnologies.cryptomax.api;

import android.graphics.Bitmap;

/**
 * Created by Colman on 21/05/2018.
 */

public interface ProfilesCallback {
    void onFailure(String reason);

    void onSuccess(String[] names, Bitmap[] images);
}
