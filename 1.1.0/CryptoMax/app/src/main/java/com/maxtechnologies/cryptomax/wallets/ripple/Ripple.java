package com.maxtechnologies.cryptomax.wallets.ripple;

import com.maxtechnologies.cryptomax.misc.BasicCallback;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.wallets.Wallet;
import com.maxtechnologies.cryptomax.wallets.misc.FeeCallback;
import com.maxtechnologies.cryptomax.wallets.misc.InvalidAddressException;
import com.maxtechnologies.cryptomax.wallets.misc.InvalidPrivateKeyException;
import com.maxtechnologies.cryptomax.wallets.misc.MessageException;
import com.maxtechnologies.cryptomax.wallets.misc.WalletFailureListener;
import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;
import com.ripple.core.types.known.tx.txns.Payment;
import com.ripple.crypto.ecdsa.K256;
import com.ripple.crypto.keys.IKeyPair;
import com.ripple.encodings.addresses.Addresses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by Colman on 08/01/2018.
 */

public class Ripple extends Wallet {
    private final static int normalClosureCode = 1000;
    private final static int invalidMessageClosureCode = 1008;
    private final static int internalErrorClosureCode = 10011;
    private volatile static boolean isOpen = false;
    private static WebSocket webSocket;
    private static ArrayList<Ripple> ripples;

    private final Object initLock = new Object();
    private int sequence;
    private ArrayList<Transaction> pending = new ArrayList<>();
    private TransactionListener listener;



    public Ripple(String name, String privateKey, @Nonnull String address) throws InvalidPrivateKeyException, InvalidAddressException {
        super(name, privateKey, address);

        if (!isValidAddress(address))
            throw new InvalidAddressException();
        if(!privateKeyMatchesAddress(privateKey, address))
            throw new InvalidPrivateKeyException();
    }



    public static Ripple generate(String name) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);

        String privateStr = Addresses.encode(bytes, Addresses.SEED_K256);
        IKeyPair keyPair = K256.createKeyPair(bytes, 0);
        String address = Addresses.encodeAccountID(keyPair.id());
        try {
            return new Ripple(name, privateStr, address);
        } catch (InvalidPrivateKeyException |InvalidAddressException e) {
            return null;
        }
    }



    public void connect() {
        if (webSocket != null && !isOpen)
            init();

        boolean contains = false;
        int index = 0;
        for (int i = 0; i < ripples.size(); i++) {
            if (getAddress().equals(ripples.get(i).getAddress())) {
                contains = true;
                index = i;
                break;
            }
        }

        ripples.add(this);
        if (contains) {
            Ripple ripple = ripples.get(index);
            setBalance(ripple.getBalance());
            sequence = ripple.sequence;
            pending = ripple.pending;
        } else {
            try {
                String address = getAddress();
                JSONObject jObj = new JSONObject();
                jObj.put("id", address);
                jObj.put("command", "subscribe");
                JSONArray jArr = new JSONArray();
                jArr.put(address);
                jObj.put("accounts_proposed", jArr);

                webSocket.send(jObj.toString());
            } catch (JSONException e) {
                //Do nothing
            }

            synchronized (initLock) {
                try {
                    initLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            try {
                fetchInfo();
            } catch (MessageException | IOException e) {
                webSocket.close(internalErrorClosureCode, null);
            }
        }
    }



    public void reconnect() {

    }



    private static void init() {
        final Object lock = new Object();
        Request request = new Request.Builder().url("wss://s2.ripple.com:443").build();
        webSocket = StaticVariables.client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isOpen = true;
                ripples = new ArrayList<>();
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    handleMessage(text);
                } catch (MessageException e) {
                    String message = "Message error: " + e.getMessage();
                    webSocket.close(invalidMessageClosureCode, message);
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                isOpen = false;
                Ripple.webSocket = null;
                if (code != normalClosureCode)
                    for (Ripple wallet : ripples) {
                        WalletFailureListener listener = wallet.getFailureListener();
                        if (listener != null)
                            listener.onFailure(reason);
                    }
                ripples = null;
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                //Do nothing
            }
        });

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }



    private static void handleMessage(String message) throws MessageException {
        JSONObject jObj;
        try {
            jObj = new JSONObject(message);
        } catch (JSONException e) {
            throw new MessageException("Message is not a JSON object");
        }

        try {
            String id = jObj.getString("id");
            for (Ripple ripple : ripples) {
                if(ripple.getAddress().equals(id)) {
                    synchronized (ripple.initLock) {
                        ripple.initLock.notify();
                    }
                }
            }
        } catch (JSONException e) {
            handleUpdate(jObj);
        }
    }



    private static void handleUpdate(JSONObject update) throws MessageException {
        boolean validated = false;
        try {
            validated = update.getBoolean("validated");
        } catch (JSONException e) {
            //Do nothing
        }

        JSONArray affected;
        try {
            JSONObject meta = update.getJSONObject("meta");
            try {
                affected = meta.getJSONArray("AffectedNodes");
            } catch (JSONException e) {
                throw new MessageException("Each meta field must contain an affected nodes field");
            }
        } catch (JSONException e) {
            throw new MessageException("Each transaction must contain a meta field");
        }

        ArrayList<Integer> affectedIndices = new ArrayList<>();
        for (int i = 0; i < affected.length(); i++) {
            JSONObject fields;
            try {
                JSONObject node = affected.getJSONObject(i);
                try {
                    node = node.getJSONObject("CreatedNode");
                    try {
                        fields = node.getJSONObject("NewFields");
                    } catch (JSONException e) {
                        throw new MessageException("A created node must have a new fields field");
                    }
                } catch (JSONException e) {
                    try {
                        node = node.getJSONObject("ModifiedNode");
                        try {
                            fields = node.getJSONObject("FinalFields");
                        } catch (JSONException e2) {
                            throw new MessageException("A modified node must have a final fields field");
                        }
                    } catch (JSONException e2) {
                        continue;
                    }
                }
                try {
                    if(!node.getString("LedgerEntryType").equals("AccountRoot"))
                        continue;
                } catch (JSONException e) {
                    throw new MessageException("Every affected node must have a ledger entry type field");
                }
            } catch (JSONException e) {
                throw new MessageException("Each affected node must be a JSONObject");
            }


            try {
                String account = fields.getString("Account");

                int sequence = -1;
                try {
                    sequence = fields.getInt("Sequence");
                } catch (JSONException e) {
                    //Do nothing
                }

                BigDecimal balance = null;
                try {
                    String balanceStr = fields.getString("Balance");
                    balance = new BigDecimal(balanceStr).multiply(new BigDecimal("0.000001"));
                } catch (JSONException e) {
                    //Do nothing
                }

                for (int j = 0; j < ripples.size(); j++) {
                    Ripple ripple = ripples.get(j);
                    if (account.equals(ripple.getAddress())) {
                        if (!affectedIndices.contains(j))
                            affectedIndices.add(j);

                        if (validated) {
                            if (sequence != -1)
                                ripple.sequence = sequence;

                            if (balance != null)
                                ripple.setBalance(balance);
                        }
                    }
                }
            } catch (JSONException e) {
                throw new MessageException("The account field must be present in an account root change");
            }
        }


        JSONObject tx;
        try {
            tx = update.getJSONObject("transaction");
        } catch (JSONException e) {
            throw new MessageException("Each transaction must contain a transaction field");
        }

        Transaction transaction = (Transaction) Transaction.fromJSON(tx.toString());
        if (validated) {
            for (int i = 0; i < affectedIndices.size(); i++) {
                ripples.get(affectedIndices.get(i)).listener.onValidated(transaction);
            }
        } else {
            String hash = transaction.hash().toString();
            for (int index : affectedIndices) {
                Ripple ripple = ripples.get(index);
                boolean contains = false;
                for (int i = 0; i < ripple.pending.size(); i++) {
                    if (ripple.pending.get(i).hash().toString().equals(hash)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    ripple.pending.add(transaction);
                    ripple.listener.onPending(transaction);
                }
            }
        }
    }



    private void fetchInfo() throws IOException, MessageException {
        String url = "http://data.ripple.com/v2/accounts/" + getAddress() + "/balances";
        Request request = new Request.Builder().url(url).build();
        Response response = StaticVariables.client.newCall(request).execute();

        String responseStr = response.body().string();

        try {
            JSONObject jObj = new JSONObject(responseStr);
            if(jObj.getString("result").equals("success")) {
                JSONArray jArr = jObj.getJSONArray("balances");
                BigDecimal value = BigDecimal.ZERO;
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jObj2 = jArr.getJSONObject(i);
                    if(jObj2.getString("currency").equals("XRP")) {
                        value = value.add(new BigDecimal(jObj2.getString("value")));
                    }
                }
                setBalance(value);
            } else if(jObj.getString("message").equals("account not found")) {
                setBalance(BigDecimal.ZERO);
            } else {
                throw new MessageException("Unknown message");
            }
        } catch(JSONException e) {
            throw new MessageException("Message must be a JSON Object");
        }
    }



    public int getNumTransactions() {
        return pending.size() + sequence - 1;
    }



    public void getTransactions(final int startIndex, final int endIndex, final TransactionsCallback callback) {
        if (startIndex < 0 || endIndex < 0 || startIndex <= endIndex)
            callback.onFailure("Invalid start or end indices");

        String url = "http://data.ripple.com/v2/accounts/" + getAddress() + "/transactions?min_sequence=";
        url += (startIndex + 1) + "&max_sequence=" + (endIndex + 1);
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
                    if (jObj.getString("result").equals("success")) {
                        JSONArray jArr = jObj.getJSONArray("transactions");
                        ArrayList<Transaction> txList = new ArrayList<>();
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONObject tx;
                            try {
                                tx = jArr.getJSONObject(i);
                            } catch (JSONException e) {
                                callback.onFailure("Each transaction must be a JSONObject");
                                return;
                            }

                            try {
                                tx = tx.getJSONObject("tx");
                            } catch (JSONException e) {
                                callback.onFailure("Each transaction must contain a tx field");
                                return;
                            }

                            txList.add((Transaction) Transaction.fromJSON(tx.toString()));
                        }

                        ArrayList<Integer> pendingIndices = new ArrayList<>();
                        int pendingEnd = endIndex - sequence;
                        if (pendingEnd > 0) {
                            int pendingStart = startIndex - sequence;
                            if (pendingStart < 0)
                                pendingStart = 0;
                            for (int i = pendingStart; i < pendingEnd; i++) {
                                pendingIndices.add(txList.size());
                                txList.add(pending.get(i));
                            }
                        }

                        Transaction[] transactions = txList.toArray(new Transaction[txList.size()]);
                        int[] pendingArr = new int[pendingIndices.size()];
                        for (int i = 0; i < pendingIndices.size(); i++)
                            pendingArr[i] = pendingIndices.get(i);
                        callback.onSuccess(transactions, pendingArr);
                    } else {
                        callback.onFailure("Request failed for reason: " + jObj.getString("message"));
                    }
                } catch(JSONException e) {
                    callback.onFailure("Invalid transactions JSON");
                }
            }
        });
    }



    public void addTransactionListener(@Nonnull TransactionListener listener) {
        this.listener = listener;
    }



    public void removeTransactionListener(@Nonnull TransactionListener listener) {
        listener = null;
    }



    public static boolean privateKeyMatchesAddress(@Nonnull String privateKey, @Nonnull String address) {
        byte[] bytes = Addresses.decode(privateKey, Addresses.SEED_K256);
        IKeyPair keyPair = K256.createKeyPair(bytes, 0);
        return Addresses.encodeAccountID(keyPair.id()).equals(address);
    }



    public static boolean isValidAddress(@Nonnull String address) {
        return Addresses.isValidAccountID(address);
    }



    @Override
    public void disconnect() {
        if (webSocket != null)
            webSocket.close(normalClosureCode, null);
    }



    @Override
    public void getFee(@Nonnull BigDecimal amount, @Nonnull final FeeCallback callback) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("method", "server_info");
            jObj.put("params", new JSONArray());
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
                callback.onFailure(FeeCallback.Code.NETWORK_ERROR, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    JSONObject jObj2 = jObj.getJSONObject("result").getJSONObject("info");
                    int factor = jObj2.getInt("load_factor");
                    String baseStr = jObj2.getJSONObject("validated_ledger").getString("base_fee_xrp");
                    BigDecimal base = new BigDecimal(baseStr);
                    callback.onSuccess(base.multiply(new BigDecimal(factor)));
                } catch(JSONException e) {
                    callback.onFailure(FeeCallback.Code.INVALID_RESPONSE, "Invalid response JSON");
                }
            }
        });
    }



    private interface LedgerSequenceCallback {
        void onFailure(String reason);

        void onSuccess(long ledgerSequence);
    }



    private void getLedgerSequence(final LedgerSequenceCallback callback) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("method", "server_info");
            jObj.put("params", new JSONArray());
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
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    JSONObject jObj2 = jObj.getJSONObject("result").getJSONObject("info");
                    long index = jObj2.getJSONObject("validated_ledger").getLong("seq");
                    callback.onSuccess(index);
                } catch(JSONException e) {
                    callback.onFailure("Invalid response JSON");
                }
            }
        });
    }



    @Override
    public void send(@Nonnull final String toAddress, @Nonnull final BigDecimal amount, @Nonnull final BigDecimal fee) {
        getLedgerSequence(new LedgerSequenceCallback() {
            @Override
            public void onFailure(String reason) {
                listener.onFailed(TransactionListener.Error.NETWORK_ERROR, amount);
            }

            @Override
            public void onSuccess(long ledgerSequence) {
                String privateKey = getPrivateKey();
                byte[] bytes = Addresses.decode(privateKey, Addresses.SEED_K256);
                IKeyPair keyPair = K256.createKeyPair(bytes, 0);

                Payment tx = new Payment();
                tx.account(AccountID.fromAddress(getAddress()));
                tx.fee(new Amount(fee));
                tx.sequence(new UInt32(sequence));
                tx.setCanonicalSignatureFlag();
                tx.lastLedgerSequence(new UInt32(ledgerSequence + 4));
                tx.signingPubKey(new Blob(keyPair.canonicalPubBytes()));
                tx.amount(new Amount(amount));
                tx.destination(AccountID.fromAddress(toAddress));
                tx.sign(privateKey);

                sendRawTx(tx, amount);
            }
        });
    }



    private void sendRawTx(final Transaction transaction, final BigDecimal amount) {
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
                listener.onFailed(TransactionListener.Error.NETWORK_ERROR, amount);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                pending.add(transaction);
                listener.onPending(transaction);
            }
        });
    }
}