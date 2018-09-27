package com.maxtechnologies.cryptomax.wallets.ethereum;

import com.maxtechnologies.cryptomax.exchange.asset.Coin;
import com.maxtechnologies.cryptomax.misc.BasicCallback;
import com.maxtechnologies.cryptomax.misc.MiscUtils;
import com.maxtechnologies.cryptomax.misc.StaticVariables;
import com.maxtechnologies.cryptomax.wallets.Wallet;
import com.maxtechnologies.cryptomax.wallets.misc.FeeCallback;
import com.maxtechnologies.cryptomax.wallets.misc.InvalidAddressException;
import com.maxtechnologies.cryptomax.wallets.misc.InvalidPrivateKeyException;
import com.maxtechnologies.cryptomax.wallets.misc.MessageException;
import com.maxtechnologies.cryptomax.wallets.misc.WalletFailureListener;

import org.ethereum.core.Transaction;
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
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Colman on 04/01/2018.
 */

public class Ethereum extends Wallet {
    private volatile BigInteger nonce = BigInteger.ZERO;
    private final ArrayList<Transaction> pending = new ArrayList<>();
    private volatile TransactionListener listener;
    private Thread updateThread;



    public Ethereum(String name, String privateKey, @Nonnull String address) throws InvalidPrivateKeyException, InvalidAddressException {
        super(name, privateKey, address);

        if (!isValidAddress(address))
            throw new InvalidAddressException();

        if (privateKey != null) {
            if (!privateKeyMatchesAddress(privateKey, address))
                throw new InvalidPrivateKeyException();
        }
    }



    public static Ethereum generate(String name) {
        ECKey key = new ECKey();
        byte[] addressBytes = key.getAddress();
        byte[] privateKeyBytes = key.getPrivKeyBytes();

        String privateKey = MiscUtils.byteArrayToHexString(privateKeyBytes).toLowerCase();
        String address = getCheckSummedAddress("0x" + MiscUtils.byteArrayToHexString(addressBytes));

        try {
            return new Ethereum(name, privateKey, address);
        } catch (InvalidPrivateKeyException | InvalidAddressException e) {
            return null;
        }
    }



    public void connect(final BasicCallback callback) {
        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nonce = fetchNonce();
                } catch (IOException | MessageException e) {
                    callback.onFailure("Failed to get nonce for reason: " + e.toString());
                    return;
                }

                try {
                    fetchBalance();
                } catch (IOException | MessageException e) {
                    callback.onFailure("Failed to get balance for reason: " + e.toString());
                    return;
                }

                callback.onSuccess();

                while (true) {
                    try {
                        getRecents();
                    } catch (IOException | MessageException e) {
                        WalletFailureListener listener = getFailureListener();
                        if (listener != null)
                            listener.onFailure("Unable to refresh wallet data for reason: " + e.toString());
                    }

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        updateThread.run();
    }



    public void reconnect(BasicCallback callback) {
        if (updateThread == null)
            connect(callback);
        else
            callback.onSuccess();
    }



    private BigInteger fetchNonce() throws IOException, MessageException {
        String url = "https://api.etherscan.io/api?module=proxy&action=eth_getTransactionCount&address=" + getAddress();
        Request request = new Request.Builder().url(url).build();
        Response response = StaticVariables.client.newCall(request).execute();

        String responseStr = response.body().string();
        try {
            JSONObject jObj = new JSONObject(responseStr);
            try {
                String result = jObj.getString("result");
                return new BigInteger(result, 16);
            } catch (JSONException e) {
                throw new MessageException("Etherscan error");
            }
        } catch (JSONException e) {
            throw new MessageException("Invalid JSON");
        }
    }



    private void fetchBalance() throws IOException, MessageException {
        String url = "http://api.etherscan.io/api?module=account&action=balance&address=" + getAddress();
        Request request = new Request.Builder().url(url).build();
        Response response = StaticVariables.client.newCall(request).execute();

        String responseStr = response.body().string();
        try {
            JSONObject jObj = new JSONObject(responseStr);
            if (jObj.getString("status").equals("1")
                    && jObj.getString("message").equals("OK")) {
                String balanceStr = jObj.getString("result");
                BigDecimal balance = new BigDecimal(balanceStr).multiply(new BigDecimal("0.000000000000000001"));
                setBalance(balance);
            } else {
                throw new MessageException("Etherscan error");
            }
        } catch (JSONException e) {
            throw new MessageException("Invalid JSON");
        }
    }



    public BigInteger getTransactionsSize() {
        synchronized (pending) {
            return nonce.add(BigInteger.valueOf(pending.size()));
        }
    }



    private void getRecents() throws IOException, MessageException {
        BigInteger newNonce = fetchNonce();
        if (newNonce.compareTo(nonce) == 0)
            return;

        String url = "http://api.etherscan.io/api?module=account&action=txlist&address=" + getAddress();
        url += "sort=desc&page=1&offset=" + newNonce.subtract(nonce).toString();
        Request request = new Request.Builder().url(url).build();
        Response response = StaticVariables.client.newCall(request).execute();

        String responseStr = response.body().string();
        CompletedTransaction[] transactions = parseTxs(responseStr);
        BigDecimal balance = getBalance();
        String address = getAddress();
        for (CompletedTransaction transaction : transactions) {
            BigInteger amountWei = ByteUtil.bytesToBigInteger(transaction.getValue());
            if (transaction.getSendAddress().equals(address)) {
                balance = balance.subtract(weiToEth(amountWei));
            } else {
                balance = balance.add(weiToEth(amountWei));
            }

            String hash = "0x" + MiscUtils.byteArrayToHexString(transaction.getHash());
            synchronized (pending) {
                for (int i = 0; i < pending.size(); i++) {
                    String hash2 = "0x" + MiscUtils.byteArrayToHexString(pending.get(i).getHash());
                    if (hash.equals(hash2)) {
                        pending.remove(i);
                        break;
                    }
                }
            }

            if (listener != null)
                listener.onConfirmed(transaction);
        }


        nonce = newNonce;
    }



    public void getTransactions(@Nonnull final BigInteger startIndex, @Nonnull final BigInteger endIndex, @Nonnull final TransactionsCallback callback) throws IllegalArgumentException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (pending) {
                    if (endIndex.compareTo(startIndex) <= 0)
                        throw new IllegalArgumentException("End index must be greater than the start index");
                    if (endIndex.compareTo(getTransactionsSize()) >= 0)
                        throw new IllegalArgumentException("End index must not be greater than or equal to transactions size");

                    if (startIndex.compareTo(nonce) >= 0) {
                        List<Transaction> pendingList = pending.subList(startIndex.subtract(nonce).intValue(), pending.size());
                        Transaction[] pendingArr = pendingList.toArray(new Transaction[pendingList.size()]);
                        callback.onSuccess(new CompletedTransaction[0], pendingArr);
                        return;
                    }

                    final BigInteger diff;
                    if (endIndex.compareTo(nonce) >= 1) {
                        diff = nonce.subtract(startIndex);
                    } else {
                        diff = endIndex.subtract(startIndex);
                    }
                    BigInteger mod = diff;
                    BigInteger offset = BigInteger.ZERO;
                    for (BigInteger i = diff; i.compareTo(i.multiply(new BigInteger("2"))) < 0; i = i.add(BigInteger.ONE)) {
                        BigInteger mod2 = startIndex.mod(i);
                        if (mod2.compareTo(mod) < 0) {
                            mod = mod2;
                            offset = i;
                        }
                    }
                    BigInteger pageNum = startIndex.divide(offset).add(BigInteger.ONE);

                    String url = "http://api.etherscan.io/api?module=account&action=txlist&address=" + getAddress();
                    url += "offset=" + offset + "&page=" + pageNum;
                    Request request = new Request.Builder().url(url).build();

                    String responseStr;
                    try {
                        Response response = StaticVariables.client.newCall(request).execute();
                        responseStr = response.body().string();
                    } catch (IOException e) {
                        callback.onFailure(e.toString());
                        return;
                    }

                    CompletedTransaction[] completed;
                    try {
                        completed = parseTxs(responseStr);
                    } catch (MessageException e) {
                        callback.onFailure("Unable to parse txs for reason: " + e.toString());
                        return;
                    }

                    int pendingSize = endIndex.subtract(startIndex).intValue() - completed.length;
                    if (pendingSize > 0) {
                        Transaction[] pendingArr = new Transaction[pendingSize];
                        for (int i = 0; i < pendingSize; i++) {
                            pendingArr[i] = pending.get(i);
                        }
                        callback.onSuccess(completed, pendingArr);
                        return;
                    }

                    callback.onSuccess(completed, new Transaction[0]);
                }
            }
        });
        thread.run();
    }



    private CompletedTransaction[] parseTxs(String response) throws MessageException {
        JSONObject jObj;
        try {
            jObj = new JSONObject(response);
        } catch (JSONException e) {
            throw new MessageException("Response must be a JSONObject");
        }

        try {
            if (!jObj.getString("status").equals("1") || !jObj.getString("message").equals("OK")) {
                throw new MessageException("Etherscan error");
            }
        } catch (JSONException e) {
            throw new MessageException("Response must contain a status and message field");
        }

        JSONArray jArr;
        try {
            jArr = jObj.getJSONArray("result");
        } catch (JSONException e) {
            throw new MessageException("Response must contain a result field that is a JSONArray");
        }

        ArrayList<CompletedTransaction> transactions = new ArrayList<>();
        for (int i = 0; i < jArr.length(); i++) {
            CompletedTransaction transaction;
            try {
                transaction = parseTx(jArr.getJSONObject(i));
            } catch (JSONException e) {
                throw new MessageException("Each transaction must be a JSONObject");
            }
            if (transaction != null)
                transactions.add(transaction);
        }


        return transactions.toArray(new CompletedTransaction[transactions.size()]);
    }



    private CompletedTransaction parseTx(JSONObject transaction) throws MessageException {
        try {
            if(!transaction.getString("txreceipt_status").equals("1"))
                return null;
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a receipt status field");
        }

        byte[] nonce;
        try {
            String nonceStr = transaction.getString("nonce");
            nonce = new BigInteger(nonceStr).toByteArray();
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a nonce field");
        }

        byte[] gasPrice;
        try {
            String gasPriceStr = transaction.getString("gasPrice");
            gasPrice = new BigInteger(gasPriceStr).toByteArray();
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a gas price field");
        }

        byte[] gasLimit;
        try {
            String gasLimitStr = transaction.getString("gasLimit");
            gasLimit = new BigInteger(gasLimitStr).toByteArray();
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a gas limit field");
        }

        byte[] receive;
        try {
            String receiveStr = transaction.getString("to").substring(2);
            receive = MiscUtils.hexStringToByteArray(receiveStr);
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a receive field");
        }

        byte[] value;
        try {
            String valueStr = transaction.getString("value");
            value = new BigInteger(valueStr).toByteArray();
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a value field");
        }

        byte[] data;
        try {
            String dataStr = transaction.getString("data").substring(2);
            data = MiscUtils.hexStringToByteArray(dataStr);
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a data field");
        }

        String sendAddress;
        try {
            sendAddress = transaction.getString("from");
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a from field");
        }

        Date timeMined;
        try {
            BigInteger timeSec = new BigInteger(transaction.getString("timestamp"));
            timeMined = new Date(timeSec.longValue() * 1000);
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a time field");
        }

        BigInteger confirmations;
        try {
            String confirmStr = transaction.getString("confirmations");
            confirmations = new BigInteger(confirmStr);
        } catch (JSONException e) {
            throw new MessageException("Transaction must contain a confirmations field");
        }


        return new CompletedTransaction(
                nonce,
                gasPrice,
                gasLimit,
                receive,
                value, data,
                sendAddress,
                timeMined,
                confirmations
        );
    }



    private static String getCheckSummedAddress(@Nonnull String address) {
        address = address.substring(2).toLowerCase();
        Keccak256 keccak = new Keccak256();
        keccak.update(address.getBytes());
        String addressHash = MiscUtils.byteArrayToHexString(keccak.digest());

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



    public void addTransactionListener(@Nonnull TransactionListener listener) {
        this.listener = listener;
    }



    public void removeTransactionListener(@Nonnull TransactionListener listener) {
        listener = null;
    }



    private static BigDecimal weiToEth(BigInteger wei) {
        return new BigDecimal(wei).multiply(new BigDecimal("0.000000000000000001"));
    }



    private static BigInteger ethToWei(BigDecimal eth) {
        return eth.multiply(new BigDecimal("1000000000000000000")).toBigInteger();
    }



    public static boolean privateKeyMatchesAddress(@Nonnull String privateKey, @Nonnull String address) {
        byte[] privateArr = MiscUtils.hexStringToByteArray(privateKey.toLowerCase());
        byte[] addressArr = ECKey.fromPrivate(privateArr).getAddress();
        String addressStr = "0x" + MiscUtils.byteArrayToHexString(addressArr).toLowerCase();

        return address.equals(getCheckSummedAddress(addressStr));
    }



    public static boolean isValidAddress(@Nonnull String address) {
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
    public void disconnect() {
        if (updateThread != null)
            updateThread.interrupt();
    }



    @Override
    public void getFee(@Nonnull BigDecimal amount, @Nonnull final FeeCallback callback) {
        getGasPrice(new GasPriceCallback() {
            @Override
            public void onFailure(Code code, String reason) {
                FeeCallback.Code code2;
                if (code == Code.NETWORK_ERROR)
                    code2 = FeeCallback.Code.NETWORK_ERROR;
                else
                    code2 = FeeCallback.Code.INVALID_RESPONSE;
                callback.onFailure(code2, "Failed to get gas price for reason: " + reason);
            }

            @Override
            public void onSuccess(byte[] gasPrice) {
                long price = ByteUtil.byteArrayToLong(gasPrice);
                BigDecimal fee = new BigDecimal(price).multiply(new BigDecimal(21))
                        .multiply(new BigDecimal("0.000000000000001"));
                callback.onSuccess(fee);
            }
        });
    }



    protected interface GasPriceCallback {
        enum Code {
            NETWORK_ERROR,
            INVALID_RESPONSE
        }

        void onFailure(Code code, String reason);

        void onSuccess(byte[] gasPrice);
    }



    private void getGasPrice(final GasPriceCallback callback) {
        String url = "https://api.etherscan.io/api?module=proxy&action=eth_gasPrice";
        Request request = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(GasPriceCallback.Code.NETWORK_ERROR, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    callback.onSuccess(MiscUtils.hexStringToByteArray(jObj.getString("result")));
                } catch(JSONException e) {
                    callback.onFailure(GasPriceCallback.Code.INVALID_RESPONSE, "JSON incorrectly formatted");
                }
            }
        });
    }



    @Override
    public void send(@Nonnull final String toAddress, @Nonnull final BigDecimal amount, @Nonnull final BigDecimal fee) {
        if(amount.add(fee).compareTo(getBalance()) == 1) {
            if (listener != null)
                listener.onFailed(TransactionListener.Error.INSUFFICIENT_FUNDS, amount);
            return;
        }

        byte[] toAddressArr = MiscUtils.hexStringToByteArray(toAddress);
        byte[] amountArr = ByteUtil.bigIntegerToBytes(ethToWei(amount));
        BigInteger gasPrice = ethToWei(fee).divide(new BigInteger("21000"));

        final org.ethereum.core.Transaction tx = new org.ethereum.core.Transaction(
                ByteUtil.bigIntegerToBytes(nonce),
                ByteUtil.bigIntegerToBytes(gasPrice),
                ByteUtil.longToBytesNoLeadZeroes(21000),
                toAddressArr,
                amountArr,
                null,
                1);

        byte[] privateBytes = MiscUtils.hexStringToByteArray(getPrivateKey());
        tx.sign(ECKey.fromPrivate(privateBytes));


        String url = "https://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex=" +
                MiscUtils.byteArrayToHexString(tx.getEncoded());
        Request request = new Request.Builder().url(url).build();

        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null)
                    listener.onFailed(TransactionListener.Error.NETWORK_ERROR, amount);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                if (listener != null)
                    try {
                        JSONObject jObj = new JSONObject(responseStr);
                        if(jObj.isNull("error")) {
                            synchronized (pending) {
                                pending.add(tx);
                            }
                            listener.onPending(tx);
                        } else {
                            listener.onFailed(TransactionListener.Error.NETWORK_ERROR, amount);
                        }
                    } catch(JSONException e) {
                        listener.onFailed(TransactionListener.Error.NETWORK_ERROR, amount);
                    }
            }
        });
    }

    public static BigDecimal byteArrayToEth(byte[] bytes) {

    }
}
