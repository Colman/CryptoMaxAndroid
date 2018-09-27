package com.maxtechnologies.cryptomax.api;

import java.util.ArrayList;

/**
 * Created by Colman on 21/05/2018.
 */

public interface ArticlesCallback {
    void onFailure(String reason);

    void onSuccess(ArrayList<Article> articles);
}
