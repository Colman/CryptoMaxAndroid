package com.maxtechnologies.cryptomax.Wallets;

import android.util.Log;

import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Callbacks.FeeCallback;
import com.maxtechnologies.cryptomax.Callbacks.SequenceCallback;
import com.maxtechnologies.cryptomax.Callbacks.TransactionCallback;
import com.maxtechnologies.cryptomax.Objects.PrivateKey;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.crypto.ecdsa.K256;
import com.ripple.crypto.keys.IKeyPair;
import com.ripple.encodings.addresses.Addresses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Colman on 08/01/2018.
 */

public class Ripple extends Wallet {


    public Ripple(String exchangeSymbol, String name, PrivateKey privateKey, String address, float balance, ArrayList<Transaction> transactions) {
        super(exchangeSymbol, name, privateKey, address, balance, transactions);
    }



    @Override
    public void generateWallet(String password) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);

        String privateStr = Addresses.encode(bytes, Addresses.SEED_K256);
        IKeyPair keyPair = K256.createKeyPair(bytes, 0);
        address = Addresses.encodeAccountID(keyPair.id());
        encrypt(privateStr, password, false, null);
    }



    @Override
    public String privateKeyToAddress(String privateKey) {
        byte[] bytes = Addresses.decode(privateKey, Addresses.SEED_K256);
        IKeyPair keyPair = K256.createKeyPair(bytes, 0);
        return Addresses.encodeAccountID(keyPair.id());
    }



    @Override
    public boolean isValidAddress(String address) {
        return Addresses.isValidAccountID(address);
    }



    @Override
    public String getCheckSummedAddress(String address) {
        return address;
    }



    @Override
    public void getBalance(final BasicCallback callback) {
        String url = "http://data.ripple.com/v2/accounts/" + address + "/balances";
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
                    if(jObj.getString("result").equals("success")) {
                        JSONArray jArr = jObj.getJSONArray("balances");
                        float value = 0;
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONObject jObj2 = jArr.getJSONObject(i);
                            if(jObj2.getString("currency").equals("XRP")) {
                                value += Float.valueOf(jObj2.getString("value"));
                            }
                        }

                        balance = value;
                    }

                    else {
                        balance = 0;
                    }
                }

                catch(JSONException e) {
                    balance = -1;
                }

                callback.onSuccess();
            }
        });
    }



    @Override
    public void getTransactions(final BasicCallback callback) {
        String url = "http://data.ripple.com/v2/accounts/" + address + "/transactions";
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
                    JSONArray jArr = jObj.getJSONArray("transactions");
                    ArrayList<Transaction> transList = new ArrayList<>();
                    for(int i = 0; i < jArr.length(); i++) {
                        JSONObject tx = jArr.getJSONObject(i);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH);
                        Date date =  format.parse(tx.getString("date"));
                        JSONObject jObj2 = tx.getJSONObject("tx");
                        if(!jObj2.getString("TransactionType").equals("Payment")) {
                            continue;
                        }

                        float amount;
                        try {
                            amount = Float.valueOf(jObj2.getString("Amount")) / 1_000_000;
                        }
                        catch(Exception e2) {
                            continue;
                        }

                        float fee = Float.valueOf(jObj2.getString("Fee"));
                        transList.add(
                                new Transaction(
                                        tx.getString("hash"),
                                        jObj2.getString("Account"),
                                        jObj2.getString("Destination"),
                                        amount,
                                        fee,
                                        date
                                )
                        );
                    }

                    transactions = transList;
                    callback.onSuccess();
                }

                catch(JSONException | ParseException e) {
                    transactions = null;
                    callback.onFailure("Invalid transactions JSON");
                }
            }
        });
    }



    @Override
    public void getFee(float amount, final FeeCallback callback) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("method", "server_info");
            jObj.put("params", new JSONArray());
        }
        catch(JSONException e) {
            callback.onFailure("JSON error");
            return;
        }

        String url = "http://s1.ripple.com:51234";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
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
                    JSONObject jObj2 = jObj.getJSONObject("result").getJSONObject("info");
                    int factor = jObj2.getInt("load_factor");
                    double base = jObj2.getJSONObject("validated_ledger").getDouble("base_fee_xrp");
                    callback.onSuccess((float) (base * factor));
                }

                catch(JSONException e) {
                    callback.onFailure("Invalid response JSON");
                }
            }
        });
    }



    private void getSequence(final SequenceCallback callback) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("method", "account_info");
            JSONObject jObj2 = new JSONObject();
            jObj2.put("account", address);
            jObj2.put("strict", true);
            jObj2.put("ledger_index", "current");
            JSONArray jArr = new JSONArray();
            jArr.put(jObj2);
            jObj.put("params", jArr);
        }
        catch(JSONException e) {
            callback.onFailure("JSON error");
            return;
        }


        String url = "http://s1.ripple.com:51234";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
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
                    int sequence = jObj.getJSONObject("result").getJSONObject("account_data").getInt("Sequence");
                    callback.onSuccess(sequence);
                }

                catch(JSONException e) {
                    callback.onSuccess(-1);
                }
            }
        });
    }



    private void getLedgerSequence(final SequenceCallback callback) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("method", "server_info");
            jObj.put("params", new JSONArray());
        }
        catch(JSONException e) {
            callback.onFailure("JSON error");
            return;
        }

        String url = "http://s1.ripple.com:51234";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
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
                    JSONObject jObj2 = jObj.getJSONObject("result").getJSONObject("info");
                    int index = jObj2.getJSONObject("validated_ledger").getInt("seq");
                    callback.onSuccess(index);
                }

                catch(JSONException e) {
                    callback.onFailure("Invalid response JSON");
                }
            }
        });
    }



    @Override
    public void send(final String toAddress, final float amount, final float fee, final String password, final TransactionCallback callback) {
        final String privateStr = decrypt(password);
        if(privateStr == null) {
            callback.onFailure(TransactionCallback.INCORRECT_PASSWORD, "");
            return;
        }

        getSequence(new SequenceCallback() {
            @Override
            public void onFailure(String reason) {
                callback.onFailure(-1, "Failed to get sequence for reason: " + reason);
            }

            @Override
            public void onSuccess(final int sequence) {

                getLedgerSequence(new SequenceCallback() {
                    @Override
                    public void onFailure(String reason) {
                        callback.onFailure(-1, "Failed to get ledger sequence for reason: " + reason);
                    }

                    @Override
                    public void onSuccess(final int ledgerSequence) {
                        byte[] bytes = Addresses.decode(privateStr, Addresses.SEED_K256);
                        IKeyPair keyPair = K256.createKeyPair(bytes, 0);

                        Payment tx = new Payment();
                        tx.account(AccountID.fromAddress(address));
                        tx.fee(new Amount(fee));
                        tx.sequence(new UInt32(sequence));
                        tx.setCanonicalSignatureFlag();
                        tx.lastLedgerSequence(new UInt32(ledgerSequence + 4));
                        tx.signingPubKey(new Blob(keyPair.canonicalPubBytes()));
                        tx.amount(new Amount(amount));
                        tx.destination(AccountID.fromAddress(toAddress));
                        tx.sign(privateStr);

                        sendRawTx(tx, callback);
                    }
                });
            }
        });
    }



    private void sendRawTx(final Payment transaction, final TransactionCallback callback) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("method", "submit");
            JSONObject jObj2 = new JSONObject();
            jObj2.put("tx_blob", transaction.toHex());
            jObj.put("params", new JSONArray().put(jObj2));
        } catch(JSONException e) {
            //Do nothing
        }

        String url = "http://s1.ripple.com:51234";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(-1, "Failed to send tx for reason: " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onSuccess(transaction.hash().toHex());
            }
        });
    }
}
