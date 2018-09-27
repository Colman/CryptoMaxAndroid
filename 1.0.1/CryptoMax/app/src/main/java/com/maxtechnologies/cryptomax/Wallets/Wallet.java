package com.maxtechnologies.cryptomax.Wallets;

import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Callbacks.FeeCallback;
import com.maxtechnologies.cryptomax.Callbacks.TransactionCallback;
import com.maxtechnologies.cryptomax.Controllers.EncryptionController;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Objects.PrivateKey;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Other.OtherTools;

import org.ethereum.util.ByteUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;


/**
 * Created by Colman on 04/01/2018.
 */

public abstract class Wallet implements Serializable {
    public String exchangeSymbol;
    public String name;
    public PrivateKey privateKey;
    public String address;
    public float balance;
    public ArrayList<Transaction> transactions;


    public Wallet(String exchangeSymbol, String name, PrivateKey privateKey, String address, float balance, ArrayList<Transaction> transactions) {
        this.exchangeSymbol = exchangeSymbol;
        this.name = name;
        this.privateKey = privateKey;
        this.address = address;
        this.balance = balance;
        this.transactions = transactions;
    }


    public Wallet(String exchangeSymbol, String name, String password) {
        this(exchangeSymbol, name, null, null, 0, new ArrayList<Transaction>());
        generateWallet(password);
    }



    public boolean encrypt(String privateKey, String password, boolean fingerprint, String email) {
        if(privateKey == null || privateKey.equals("")) {
            return false;
        }
        if(password == null || password.equals("")) {
            return false;
        }

        String[] encrypted = EncryptionController.encrypt(privateKey, password);
        if(encrypted == null || encrypted.length != 3) {
            return false;
        }

        this.privateKey = new PrivateKey(encrypted[0], encrypted[1], encrypted[2], fingerprint, email);


        return true;
    }



    public String decrypt(String password) {
        if(privateKey == null) {
            return null;
        }

        String[] encryptedArr = new String[3];
        encryptedArr[0] = privateKey.encrypted;
        encryptedArr[1] = privateKey.passwordSalt;
        encryptedArr[2] = privateKey.passwordIv;
        return EncryptionController.decrypt(password, encryptedArr);
    }



    public static byte[] hexStringToByteArray(String str) {
        return ByteUtil.hexStringToBytes(str);
    }



    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for(int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }



    public Wallet clone() {
        return (Wallet) OtherTools.deepCopy(this);
    }



    public abstract void generateWallet(String password);

    public abstract String privateKeyToAddress(String privateKey);

    public abstract boolean isValidAddress(String address);

    public abstract String getCheckSummedAddress(String address);

    public abstract void getBalance(BasicCallback callback);

    public abstract void getTransactions(BasicCallback callback);

    public abstract void getFee(float amount, FeeCallback callback);

    public abstract void send(String toAddress, float amount, float fee, String password, TransactionCallback callback);
}
