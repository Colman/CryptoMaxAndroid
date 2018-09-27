package com.maxtechnologies.cryptomax.Callbacks;

/**
 * Created by Colman on 19/05/2018.
 */

public interface IdCallback {
    void onFailure(String reason);

    void onSuccess(String id);
}
