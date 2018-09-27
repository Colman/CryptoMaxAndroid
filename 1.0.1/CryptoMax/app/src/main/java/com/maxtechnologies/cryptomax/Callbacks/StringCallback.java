package com.maxtechnologies.cryptomax.Callbacks;

/**
 * Created by Colman on 21/05/2018.
 */

public interface StringCallback {
    void onFailure(String reason);

    void onSuccess(String result);
}
