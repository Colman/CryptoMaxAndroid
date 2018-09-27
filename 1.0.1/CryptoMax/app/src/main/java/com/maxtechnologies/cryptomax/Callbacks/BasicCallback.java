package com.maxtechnologies.cryptomax.Callbacks;

import java.util.ArrayList;

/**
 * Created by Colman on 18/05/2018.
 */

public interface BasicCallback {
    void onFailure(String reason);

    void onSuccess();
}
