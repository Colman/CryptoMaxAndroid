package com.maxtechnologies.cryptomax.wallets.ethereum;

import org.ethereum.core.Transaction;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Date;

/**
 * Created by Colman on 11/07/2018.
 */

public class CompletedTransaction extends FullTransaction {
    private Date timeMined;
    private BigInteger confirmations;


    public CompletedTransaction(byte[] rawData, @NotNull String sendAddress, @NotNull Date timeMined, @NotNull BigInteger confirmations) {
        super(rawData, sendAddress);
        this.timeMined = timeMined;
        this.confirmations = confirmations;
    }


    public CompletedTransaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                       Integer chainId, @NotNull String sendAddress, @NotNull Date timeMined, @NotNull BigInteger confirmations) {
        super(nonce, gasPrice, gasLimit, receiveAddress, value, data, chainId, sendAddress);
        this.timeMined = timeMined;
        this.confirmations = confirmations;
    }


    public CompletedTransaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                                @NotNull String sendAddress, @NotNull Date timeMined, @NotNull BigInteger confirmations) {
        super(nonce, gasPrice, gasLimit, receiveAddress, value, data, sendAddress);
        this.timeMined = timeMined;
        this.confirmations = confirmations;
    }


    public CompletedTransaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                       byte[] r, byte[] s, byte v, Integer chainId, @NotNull String sendAddress, @NotNull Date timeMined, @NotNull BigInteger confirmations) {
        super(nonce, gasPrice, gasLimit, receiveAddress, value, data, r, s, v, chainId, sendAddress);
        this.timeMined = timeMined;
        this.confirmations = confirmations;
    }


    public CompletedTransaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                       byte[] r, byte[] s, byte v, @NotNull String sendAddress, @NotNull Date timeMined, @NotNull BigInteger confirmations) {
        super(nonce, gasPrice, gasLimit, receiveAddress, value, data, r, s, v, sendAddress);
        this.timeMined = timeMined;
        this.confirmations = confirmations;
    }



    @NotNull
    public Date getTimeMined() {
        return timeMined;
    }



    @NotNull
    public BigInteger getConfirmations() {
        return confirmations;
    }



    public void setConfirmations(@NotNull BigInteger confirmations) {
        this.confirmations = confirmations;
    }
}
