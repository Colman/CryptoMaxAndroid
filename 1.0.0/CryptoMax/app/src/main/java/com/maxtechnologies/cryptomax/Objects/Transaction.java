package com.maxtechnologies.cryptomax.Objects;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Colman on 04/01/2018.
 */

public class Transaction implements Serializable {
    public String hash;
    public String fromAddress;
    public String toAddress;
    public float amount;
    public float fee;
    public Date timeMined;


    public Transaction(String hash, String fromAddress, String toAddress, float amount, float fee, Date timeMined) {
        this.hash = hash;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.amount = amount;
        this.fee = fee;
        this.timeMined = timeMined;
    }
}
