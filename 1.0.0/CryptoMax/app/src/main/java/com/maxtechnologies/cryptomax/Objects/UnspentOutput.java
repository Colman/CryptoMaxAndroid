package com.maxtechnologies.cryptomax.Objects;

import com.maxtechnologies.cryptomax.Callbacks.UnspentCallback;

/**
 * Created by Colman on 18/06/2018.
 */

public class UnspentOutput {
    public String txHash;
    public int index;
    public String scriptPubKey;
    public double amount;

    public UnspentOutput(String txHash, int index, String scriptPubKey, double amount) {
        this.txHash = txHash;
        this.index = index;
        this.scriptPubKey = scriptPubKey;
        this.amount = amount;
    }
}
