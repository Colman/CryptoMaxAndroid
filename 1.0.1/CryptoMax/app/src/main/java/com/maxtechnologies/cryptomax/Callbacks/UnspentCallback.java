package com.maxtechnologies.cryptomax.Callbacks;

import com.maxtechnologies.cryptomax.Objects.UnspentOutput;

import java.util.ArrayList;

/**
 * Created by Colman on 18/06/2018.
 */

public interface UnspentCallback {
    void onFailure(String reason);

    void onSuccess(ArrayList<UnspentOutput> outputs);
}
