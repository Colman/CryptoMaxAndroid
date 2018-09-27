package com.maxtechnologies.cryptomax.wallets;

import com.maxtechnologies.cryptomax.exchange.asset.Asset;
import com.maxtechnologies.cryptomax.exchange.asset.Coin;
import com.maxtechnologies.cryptomax.misc.BasicCallback;
import com.maxtechnologies.cryptomax.wallets.misc.FeeCallback;
import com.maxtechnologies.cryptomax.wallets.misc.WalletFailureListener;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Created by Colman on 04/01/2018.
 */

public abstract class Wallet implements Serializable {
    private String name;
    private String privateKey;
    private String address;
    private BigDecimal balance;
    private WalletFailureListener failureListener;


    public Wallet(String name, String privateKey, String address) {
        this.name = name;
        this.privateKey = privateKey;
        this.address = address;
    }



    @Nullable
    public String getName() {
        return name;
    }



    public void setName(String name) {
        this.name = name;
    }



    public String getPrivateKey() {
        return privateKey;
    }



    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }



    public String getAddress() {
        return address;
    }



    protected void setAddress(String address) {
        this.address = address;
    }



    @Nullable
    public BigDecimal getBalance() {
        return balance;
    }



    protected void setBalance(@Nullable BigDecimal balance) {
        this.balance = balance;
    }



    @Nullable
    protected WalletFailureListener getFailureListener() {
        return failureListener;
    }



    public void setFailureListener(@Nonnull WalletFailureListener listener) {
        this.failureListener = listener;
    }



    public void removeFailureListener() {
        failureListener = null;
    }



    public abstract void disconnect();

    public abstract void getFee(@Nonnull BigDecimal amount, @Nonnull FeeCallback callback);

    public abstract void send(@Nonnull String toAddress, @Nonnull BigDecimal amount, @Nonnull BigDecimal fee);
}
