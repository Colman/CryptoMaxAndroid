package com.maxtechnologies.cryptomax.Callbacks;

/**
 * Created by Colman on 19/06/2018.
 */

public interface SequenceCallback {
    void onFailure(String reason);

    void onSuccess(int sequence);
}
