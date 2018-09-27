package com.maxtechnologies.cryptomax.wallets.bitcoin;

/**
 * Created by Colman on 30/06/2018.
 */

public interface DownloadProgressListener {
    void onFailure(String reason);

    void onStart(int numBlocks);

    void onProgress(int blocksLeft);
}
