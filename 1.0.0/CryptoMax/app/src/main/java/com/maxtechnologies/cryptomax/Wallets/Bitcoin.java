package com.maxtechnologies.cryptomax.Wallets;

import android.util.Log;

import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Callbacks.FeeCallback;
import com.maxtechnologies.cryptomax.Callbacks.TransactionCallback;
import com.maxtechnologies.cryptomax.Callbacks.UnspentCallback;
import com.maxtechnologies.cryptomax.Objects.PrivateKey;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Objects.UnspentOutput;
import com.maxtechnologies.cryptomax.Other.StaticVariables;
import com.maxtechnologies.cryptomax.R;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.TransactionBroadcaster;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by Colman on 04/01/2018.
 */

public class Bitcoin extends Wallet {

    public Bitcoin(String exchangeSymbol, String name, PrivateKey privateKey, String address, float balance, ArrayList<Transaction> transactions) {
        super(exchangeSymbol, name, privateKey, address, balance, transactions);
    }



    @Override
    public void generateWallet(String password) {
        ECKey key = new ECKey();
        MainNetParams params = new MainNetParams();
        this.address = key.toAddress(params).toBase58();
        encrypt(key.getPrivateKeyAsWiF(params), password, false, null);
    }


    @Override
    public String privateKeyToAddress(String privateKey) {
        String bytes = Wallet.byteArrayToHexString(Base58.decodeChecked(privateKey));
        bytes = bytes.substring(2, bytes.length() - 2);
        MainNetParams params = new MainNetParams();
        return ECKey.fromPrivate(Wallet.hexStringToByteArray(bytes)).toAddress(params).toBase58();
    }



    @Override
    public boolean isValidAddress(String address) {
        if (address.length() < 26 || address.length() > 35) {
            return false;
        }

        byte[] decoded = Base58.decode(address);
        if (decoded == null) {
            return false;
        }

        byte[] hash = Sha256Hash.hashTwice(Arrays.copyOfRange(decoded, 0, 21));

        return Arrays.equals(Arrays.copyOfRange(hash, 0, 4), Arrays.copyOfRange(decoded, 21, 25));
    }



    @Override
    public String getCheckSummedAddress(String address) {
        return address;
    }



    @Override
    public void getBalance(final BasicCallback callback) {
        String url = "http://blockexplorer.com/api/addr/" + address;
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
                    balance = BigDecimal.valueOf(jObj.getDouble("balance")).floatValue();
                }

                catch(Exception e) {
                    balance = -1;
                }

                callback.onSuccess();
            }
        });
    }



    @Override
    public void getTransactions(final BasicCallback callback) {
        String url = "http://blockexplorer.com/api/txs/?address=" + address;
        Request request = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                transactions = parseTransactions(responseStr);
                if(transactions == null) {
                    callback.onFailure("Invalid transactions JSON");
                    return;
                }

                callback.onSuccess();
            }
        });
    }



    private ArrayList<Transaction> parseTransactions(String jsonString) {
        try {
            JSONObject jObj = new JSONObject(jsonString);
            JSONArray txs = jObj.getJSONArray("txs");
            ArrayList<Transaction> txList = new ArrayList<>();
            for(int i = 0; i < txs.length(); i++) {
                JSONObject tx = txs.getJSONObject(i);
                boolean isInput = false;
                float amount = 0;
                JSONArray inputs = tx.getJSONArray("vin");
                for(int j = 0; j < inputs.length(); j++) {
                    JSONObject input = inputs.getJSONObject(j);
                    if(input.getString("addr").equals(address)) {
                        amount = BigDecimal.valueOf(input.getDouble("value")).floatValue();
                        isInput = true;
                        break;
                    }
                }
                JSONArray outputs = tx.getJSONArray("vout");
                if(!isInput) {
                    for(int j = 0; j < outputs.length(); j++) {
                        JSONObject output = outputs.getJSONObject(j);
                        JSONObject scriptPub = output.getJSONObject("scriptPubKey");
                        String addr = scriptPub.getJSONArray("addresses").getString(0);
                        if(addr.equals(address)) {
                            amount = BigDecimal.valueOf(output.getDouble("value")).floatValue();
                            break;
                        }
                    }
                }

                String from;
                String to;
                if(isInput) {
                    if(outputs.length() == 1) {
                        JSONObject output = outputs.getJSONObject(0);
                        JSONObject scriptPub = output.getJSONObject("scriptPubKey");
                        JSONArray jArr = scriptPub.getJSONArray("addresses");
                        if(jArr.isNull(0)) {
                            to = "unknown";
                        }
                        else {
                            to = jArr.getString(0);
                        }
                    }
                    else {
                        to = "various";
                    }
                    from = address;
                }
                else {
                    if(inputs.length() == 1) {
                        JSONObject jObj2 = inputs.getJSONObject(0);
                        if(jObj2.isNull("addr")) {
                            from = "unknown";
                        }
                        else {
                            from = jObj2.getString("addr");
                        }
                    }
                    else {
                        from = "various";
                    }
                    to = address;
                }
                Date date = new Date(Long.valueOf(tx.getString("time")) * 1000);
                String hash = tx.getString("txid");
                Transaction trans = new Transaction(hash, from, to, amount, 0, date);
                txList.add(trans);
            }

            return txList;
        }

        catch(JSONException e) {
            return null;
        }
    }



    @Override
    public void getFee(final float amount, final FeeCallback callback) {
        String url = "http://bitcoinfees.21.co/api/v1/fees/recommended";
        Log.d("test", "yay");
        Request request = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                final int sat;
                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    sat = jObj.getInt("halfHourFee");
                }

                catch(JSONException e) {
                    callback.onFailure("Invalid transactions JSON");
                    return;
                }

                getUnspents(new UnspentCallback() {
                    @Override
                    public void onFailure(String reason) {
                        callback.onFailure("Failed to get UTXOs for reason: " + reason);
                    }

                    @Override
                    public void onSuccess(ArrayList<UnspentOutput> outputs) {
                        double total = 0;
                        int numTransactions = 0;
                        for(UnspentOutput output : outputs) {
                            total += output.amount;
                            numTransactions++;
                            if(total > amount)
                                break;
                        }
                        callback.onSuccess(((float) 147 * sat * numTransactions) / 100000000);
                    }
                });
            }
        });
    }



    private void getUnspents(final UnspentCallback callback) {
        String url = "http://blockexplorer.com/api/addr/" + address + "/utxo";
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
                    JSONArray jArr = new JSONArray(responseStr);
                    ArrayList<UnspentOutput> outputs = new ArrayList<>();
                    for(int i = 0; i < jArr.length(); i++) {
                        JSONObject jObj = jArr.getJSONObject(i);
                        UnspentOutput output = new UnspentOutput(jObj.getString("txid"),
                                jObj.getInt("vout"), jObj.getString("scriptPubKey"),
                                jObj.getDouble("amount"));
                        outputs.add(output);
                    }

                    Collections.sort(outputs, new Comparator<UnspentOutput>() {
                        @Override
                        public int compare(UnspentOutput output1, UnspentOutput output2) {
                            if(output1.amount < output2.amount) {
                                return -1;
                            }
                            else if(output1.amount == output2.amount) {
                                return 0;
                            }
                            return 1;
                        }
                    });
                    callback.onSuccess(outputs);
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
        if(amount + fee > balance) {
            callback.onFailure(TransactionCallback.INSUFFICIENT_FUNDS, "");
            return;
        }

        getUnspents(new UnspentCallback() {
            @Override
            public void onFailure(String reason) {
                callback.onFailure(-1, "Failed to get unspent transactions for reason: " + reason);
            }

            @Override
            public void onSuccess(ArrayList<UnspentOutput> outputs) {
                MainNetParams params = new MainNetParams();
                String bytes = Wallet.byteArrayToHexString(Base58.decodeChecked(privateStr));
                bytes = bytes.substring(2, bytes.length() - 2);
                ArrayList<ECKey> keys = new ArrayList<>();
                keys.add(ECKey.fromPrivate(Wallet.hexStringToByteArray(bytes)));

                org.bitcoinj.core.Transaction tx = new org.bitcoinj.core.Transaction(params);
                long amountSat = (long) (amount * 100000000);
                tx.addOutput(Coin.valueOf(amountSat), Address.fromBase58(params, toAddress));

                double total = 0;
                for(UnspentOutput output : outputs) {
                    Sha256Hash hash = new Sha256Hash(output.txHash);
                    TransactionOutPoint out = new TransactionOutPoint(params, output.index, hash);
                    Script script = new Script(hexStringToByteArray(output.scriptPubKey));
                    tx.addSignedInput(out, script, keys.get(0));
                    total += output.amount;
                    if(total > amount + fee) {
                        break;
                    }
                }
                tx.addOutput(Coin.valueOf((long) ((total - amount - fee) * 100000000)), Address.fromBase58(params, address));

                sendRawTx(tx.bitcoinSerialize(), tx.getHashAsString(), callback);
            }
        });
    }



    private void sendRawTx(byte[] rawTx, final String hash, final TransactionCallback callback) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("rawtx", byteArrayToHexString(rawTx));
        }
        catch(JSONException e) {
            callback.onFailure(-1, "JSON error");
            return;
        }

        String url = "https://blockexplorer.com/api/tx/send";
        MediaType type = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("rawtx", byteArrayToHexString(rawTx))
                .build();
        Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(-1, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                Log.d("test", responseStr);
                callback.onSuccess(hash);
            }
        });
    }
}
