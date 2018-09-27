package com.maxtechnologies.cryptomax.wallets.ethereum;

import org.ethereum.core.Transaction;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Colman on 12/07/2018.
 */

public class FullTransaction extends Transaction {
    private String sendAddress;


    public FullTransaction(byte[] rawData, @NotNull String sendAddress) {
        super(rawData);
        this.sendAddress = sendAddress;
    }


    public FullTransaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                                Integer chainId, @NotNull String sendAddress) {
        super(nonce, gasPrice, gasLimit, receiveAddress, value, data, chainId);
        this.sendAddress = sendAddress;
    }


    public FullTransaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                                @NotNull String sendAddress) {
        super(nonce, gasPrice, gasLimit, receiveAddress, value, data);
        this.sendAddress = sendAddress;
    }


    public FullTransaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                                byte[] r, byte[] s, byte v, Integer chainId, @NotNull String sendAddress) {
        super(nonce, gasPrice, gasLimit, receiveAddress, value, data, r, s, v, chainId);
        this.sendAddress = sendAddress;
    }


    public FullTransaction(byte[] nonce, byte[] gasPrice, byte[] gasLimit, byte[] receiveAddress, byte[] value, byte[] data,
                                byte[] r, byte[] s, byte v, @NotNull String sendAddress) {
        super(nonce, gasPrice, gasLimit, receiveAddress, value, data, r, s, v);
        this.sendAddress = sendAddress;
    }



    @NotNull
    public String getSendAddress() {
        return sendAddress;
    }
}
