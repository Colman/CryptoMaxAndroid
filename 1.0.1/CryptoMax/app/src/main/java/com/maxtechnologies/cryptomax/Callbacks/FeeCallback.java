package com.maxtechnologies.cryptomax.Callbacks;

/**
 * Created by Colman on 02/06/2018.
 */

public interface FeeCallback {
    void onFailure(String reason);

    void onSuccess(float fee);
}
