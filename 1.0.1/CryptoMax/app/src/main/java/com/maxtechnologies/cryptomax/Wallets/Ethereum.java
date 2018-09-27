package com.maxtechnologies.cryptomax.Wallets;


import android.util.Log;

import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Callbacks.FeeCallback;
import com.maxtechnologies.cryptomax.Callbacks.TransactionCallback;
import com.maxtechnologies.cryptomax.Objects.PrivateKey;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Other.StaticVariables;

import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.cryptohash.Keccak256;
import org.ethereum.util.ByteUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Colman on 04/01/2018.
 */

public class Ethereum extends Wallet {


    public Ethereum(String exchangeSymbol, String name, PrivateKey privateKey, String address, float balance, ArrayList<Transaction> transactions) {
        super(exchangeSymbol, name, privateKey, address, balance, transactions);
    }



    public Ethereum(String exchangeSymbol, String name, String password) {
        super(exchangeSymbol, name, password);
    }



    @Override
    public void generateWallet(String password) {
        if(password == null || password.equals("")) {
            return;
        }
        ECKey key = new ECKey();
        byte[] address = key.getAddress();
        byte[] privateKey = key.getPrivKeyBytes();
        this.address = getCheckSummedAddress("0x" + byteArrayToHexString(address));
        String privateStr = byteArrayToHexString(privateKey).toLowerCase();

        encrypt(privateStr, password, false, null);
    }



    @Override
    public String privateKeyToAddress(String privateKey) {
        byte[] privateArr = hexStringToByteArray(privateKey.toLowerCase());
        byte[] address = ECKey.fromPrivate(privateArr).getAddress();
        String addressStr = "0x" + Wallet.byteArrayToHexString(address).toLowerCase();

        return getCheckSummedAddress(addressStr);
    }



    @Override
    public boolean isValidAddress(String address) {
        if(!address.substring(0, 2).equals("0x")) {
            return false;
        }
        if(address.length() != 42) {
            return false;
        }

        String hexLetters = "abcdefABCDEF";
        for(int i = 2; i < address.length(); i++) {
            if(Character.isDigit(address.charAt(i))) {
                continue;
            }
            boolean wasIn = false;
            for(int j = 0; j < hexLetters.length(); j++) {
                if(("" + hexLetters.charAt(j)).equals(address.charAt(i) + "")) {
                    wasIn = true;
                    break;
                }
            }
            if(!wasIn) {
                return false;
            }
        }


        return address.equals(getCheckSummedAddress(address));
    }



    @Override
    public String getCheckSummedAddress(String address) {
        address = address.substring(2).toLowerCase();
        Keccak256 keccak = new Keccak256();
        keccak.update(address.getBytes());
        String addressHash = Wallet.byteArrayToHexString(keccak.digest());

        StringBuilder newAddress = new StringBuilder();
        for(int i = 0; i < address.length(); i++) {
            char hexChar = address.charAt(i);
            if(Character.isLetter(hexChar)) {
                char hashChar = addressHash.charAt(i);
                if(hashChar == '8' || hashChar == '9' || Character.isLetter(hashChar)) {
                    newAddress.append(String.valueOf(hexChar).toUpperCase());
                    continue;
                }
            }
            newAddress.append(hexChar);
        }

        return "0x" + newAddress.toString();
    }



    @Override
    public void getBalance(final BasicCallback callback) {
        String url = "http://api.etherscan.io/api?module=account&action=balance&address=" + address;
        Request request = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    balance = Float.valueOf(jObj.getString("result")) * 0.000000000000000001f;
                }

                catch (Exception e) {
                    balance = -1;
                }

                callback.onSuccess();
            }
        });
    }



    @Override
    public void getTransactions(final BasicCallback callback) {
        String url = "http://api.etherscan.io/api?module=account&action=txlist&address=" + address;
        Request request = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    JSONArray jArr = jObj.getJSONArray("result");
                    ArrayList<Transaction> txList = new ArrayList<>();
                    for(int i = 0; i < jArr.length(); i++) {
                        JSONObject temp = jArr.getJSONObject(i);
                        if(!temp.getString("txreceipt_status").equals("1"))
                            continue;
                        float amount = Float.valueOf(temp.getString("value")) * 0.000000000000000001f;
                        Date date = new Date(Long.valueOf(temp.getString("timeStamp")) * 1000);
                        String fromStr = getCheckSummedAddress(temp.getString("from"));
                        String toStr = getCheckSummedAddress(temp.getString("to"));
                        String hash = temp.getString("hash");
                        Transaction tx = new Transaction(hash, fromStr, toStr, amount, 0, date);
                        txList.add(tx);
                    }

                    Collections.reverse(txList);
                    transactions = txList;
                    callback.onSuccess();
                }

                catch(JSONException e) {
                    callback.onFailure("Invalid transactions JSON");
                }
            }
        });
    }



    @Override
    public void getFee(float amount, final FeeCallback callback) {
        getGasPrice(new GasPriceCallback() {
            @Override
            public void onFailure(String reason) {
                callback.onFailure("Failed to get gas price for reason: " + reason);
            }

            @Override
            public void onSuccess(byte[] gasPrice) {
                long price = ByteUtil.byteArrayToLong(gasPrice);
                float fee = ((float) price * 21 / 1000000000000000L);
                callback.onSuccess(fee);
            }
        });
    }



    protected interface GasPriceCallback {
        void onFailure(String reason);

        void onSuccess(byte[] gasPrice);
    }



    private void getGasPrice(final GasPriceCallback callback) {
        String url = "https://api.etherscan.io/api?module=proxy&action=eth_gasPrice";
        Request request = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    callback.onSuccess(hexStringToByteArray(jObj.getString("result")));
                }

                catch(JSONException e) {
                    callback.onFailure("Gas price JSON incorrectly formatted");
                }
            }
        });
    }



    @Override
    public void send(final String toAddress, final float amount, float fee, final String password, final TransactionCallback callback) {
        final String privateStr = decrypt(password);
        if(privateStr == null) {
            callback.onFailure(TransactionCallback.INCORRECT_PASSWORD, "");
            return;
        }
        if(amount + fee > balance) {
            callback.onFailure(TransactionCallback.INSUFFICIENT_FUNDS, "");
            return;
        }

        getTransactions(new BasicCallback() {
            @Override
            public void onFailure(String reason) {
                callback.onFailure(-1, reason);
            }

            @Override
            public void onSuccess() {
                getGasPrice(new GasPriceCallback() {
                    @Override
                    public void onFailure(String reason) {
                        callback.onFailure(-1, reason);
                    }

                    @Override
                    public void onSuccess(byte[] gasPrice) {
                        byte[] toAddressArr = Wallet.hexStringToByteArray(toAddress);
                        BigDecimal bigAmount = new BigDecimal(Float.toString(amount));
                        bigAmount = bigAmount.multiply(new BigDecimal("1000000000000000000"));
                        byte[] amountArr = ByteUtil.bigIntegerToBytes(bigAmount.toBigInteger());


                        int nonce = 0;
                        for(Transaction transaction : transactions) {
                            if(transaction.fromAddress.equals(address)) {
                                nonce++;
                            }
                        }

                        org.ethereum.core.Transaction tx = new org.ethereum.core.Transaction(
                                ByteUtil.intToBytesNoLeadZeroes(nonce),
                                gasPrice,
                                ByteUtil.longToBytesNoLeadZeroes(21000),
                                toAddressArr,
                                amountArr,
                                null,
                                1);

                        byte[] privateBytes = hexStringToByteArray(privateStr.toLowerCase());
                        tx.sign(ECKey.fromPrivate(privateBytes));

                        String hash = "0x" + byteArrayToHexString(tx.getHash()).toLowerCase();
                        sendRawTx(tx.getEncoded(), hash, new TransactionCallback() {
                            @Override
                            public void onFailure(int code, String message) {
                                callback.onFailure(code, message);
                            }

                            @Override
                            public void onSuccess(String hash) {
                                callback.onSuccess(hash);
                            }
                        });
                    }
                });
            }
        });
    }



    private void sendRawTx(byte[] rawTx, final String hash, final TransactionCallback callback) {
        String url = "https://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex=" +
                Wallet.byteArrayToHexString(rawTx);
        Request request = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(-1, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.d("test", responseStr);
                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    if(jObj.isNull("error")) {
                        callback.onSuccess(hash);
                    }
                    else {
                        JSONObject jObj2 = jObj.getJSONObject("error");
                        callback.onFailure(-1, jObj2.getString("message"));
                    }
                }
                catch(JSONException e) {
                    callback.onFailure(-1, "Invalid response JSON");
                }
            }
        });
    }
}
