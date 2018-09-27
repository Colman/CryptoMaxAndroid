package com.maxtechnologies.cryptomax.wallets.bitcoin;

import com.google.common.util.concurrent.ListenableFuture;
import com.maxtechnologies.cryptomax.exchange.asset.Asset;
import com.maxtechnologies.cryptomax.misc.BasicCallback;
import com.maxtechnologies.cryptomax.misc.MiscUtils;
import com.maxtechnologies.cryptomax.wallets.Wallet;
import com.maxtechnologies.cryptomax.wallets.misc.FeeCallback;
import com.maxtechnologies.cryptomax.wallets.misc.InvalidAddressException;
import com.maxtechnologies.cryptomax.wallets.misc.InvalidPrivateKeyException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.GetDataMessage;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.listeners.PeerDataEventListener;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.WalletTransaction;


import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Created by Colman on 04/01/2018.
 */

public class Bitcoin extends Wallet {
    private static PeerGroup peerGroup;
    private org.bitcoinj.wallet.Wallet wallet;
    private TransactionConfidenceEventListener confidenceListener;
    private TransactionListener transactionListener;


    public Bitcoin(String name, String privateKey, @Nonnull String address) throws InvalidPrivateKeyException, InvalidAddressException {
        super(name, privateKey, address);

        if (!isValidAddress(address))
            throw new InvalidAddressException();
        if(!privateKeyMatchesAddress(privateKey, address))
            throw new InvalidPrivateKeyException();
    }



    public static Bitcoin generate(String name) {
        ECKey key = new ECKey();
        MainNetParams params = MainNetParams.get();
        try {
            return new Bitcoin(name, key.getPrivateKeyAsWiF(params), key.toAddress(params).toBase58());
        } catch (InvalidPrivateKeyException | InvalidAddressException e) {
            return null;
        }
    }



    public void connect(final DownloadProgressListener listener) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    init();
                } catch (BlockStoreException e) {
                    listener.onFailure("Failed to create block store for reason: " + e.toString());
                    return;
                }


                peerGroup.addWallet(wallet);

                peerGroup.startBlockChainDownload(new PeerDataEventListener() {
                    @Override
                    public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                        if (blocksLeft == 0) {
                            BigDecimal balance = new BigDecimal(wallet.getBalance().getValue());
                            balance = balance.divide(new BigDecimal(100000000), BigDecimal.ROUND_DOWN);
                            setBalance(balance);
                        }

                        if (listener != null) {
                            listener.onProgress(blocksLeft);
                        }
                    }

                    @Override
                    public void onChainDownloadStarted(Peer peer, int blocksLeft) {
                        if (blocksLeft == 0) {
                            BigDecimal balance = new BigDecimal(wallet.getBalance().getValue());
                            balance = balance.divide(new BigDecimal(100000000), BigDecimal.ROUND_DOWN);
                            setBalance(balance);
                        }

                        if (listener != null) {
                            listener.onStart(blocksLeft);
                        }
                    }

                    @Nullable
                    @Override
                    public List<Message> getData(Peer peer, GetDataMessage m) {
                        return null;
                    }

                    @Override
                    public Message onPreMessageReceived(Peer peer, Message m) {
                        return m;
                    }
                });
            }
        });
        thread.run();
    }



    private static void init() throws BlockStoreException {
        if(peerGroup != null && peerGroup.numConnectedPeers() != 0)
            return;

        NetworkParameters params = MainNetParams.get();
        File file = new File("bitcoin.spvchain");
        BlockStore blockStore = new SPVBlockStore(params, file);
        BlockChain chain = new BlockChain(params, blockStore);
        peerGroup = new PeerGroup(params, chain);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));
        peerGroup.start();
    }



    @Override
    public void setPrivateKey(String privateKey) {
        super.setPrivateKey(privateKey);

        MainNetParams params = MainNetParams.get();
        if (privateKey != null) {
            byte[] seedBytes = MiscUtils.hexStringToByteArray(privateKey);
            DeterministicSeed seed = new DeterministicSeed(seedBytes, new ArrayList<String>(),
                    MnemonicCode.BIP39_STANDARDISATION_TIME_SECS);
            wallet = org.bitcoinj.wallet.Wallet.fromSeed(params, seed);
        } else {
            wallet = new org.bitcoinj.wallet.Wallet(params);
            Address address = Address.fromBase58(params, getAddress());
            wallet.addWatchedAddress(address);
        }
    }



    public int getTransactionsSize() {
        return wallet.getTransactionsByTime().size();
    }



    public Transaction[] getTransactions(int startIndex, int endIndex) throws IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex <= endIndex)
            throw new IllegalArgumentException("Invalid start or end indices");


        List<Transaction> txs = wallet.getTransactionsByTime();
        ArrayList<Transaction> result = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            try {
                result.add(txs.get(i));
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        return result.toArray(new Transaction[result.size()]);
    }



    public void addTransactionListener(@Nonnull TransactionListener listener) {
        transactionListener = listener;
        confidenceListener = new TransactionConfidenceEventListener() {
            @Override
            public void onTransactionConfidenceChanged(org.bitcoinj.wallet.Wallet wallet, Transaction tx) {
                if(tx.getConfidence().getDepthInBlocks() == 1) {
                    transactionListener.onConfirmed(tx);
                }
            }
        };

        wallet.addTransactionConfidenceEventListener(confidenceListener);
    }



    public void removeTransactionListener(@Nonnull TransactionListener listener) {
        if (transactionListener != null) {
            wallet.removeTransactionConfidenceEventListener(confidenceListener);
            confidenceListener = null;
            transactionListener = null;
        }
    }



    public static boolean privateKeyMatchesAddress(@Nonnull String privateKey, @Nonnull String address) {
        String bytes = MiscUtils.byteArrayToHexString(Base58.decodeChecked(privateKey));
        bytes = bytes.substring(2, bytes.length() - 2);
        MainNetParams params = MainNetParams.get();
        ECKey privateEC = ECKey.fromPrivate(MiscUtils.hexStringToByteArray(bytes));
        String address2 = privateEC.toAddress(params).toBase58();
        return address.equals(address2);
    }



    public static boolean isValidAddress(@Nonnull String address) {
        try {
            Address.fromBase58(Address.getParametersFromAddress(address), address);
            return true;
        } catch (AddressFormatException e) {
            return false;
        }
    }



    public BigDecimal getAmount(Transaction transaction) {
        return new BigDecimal(transaction.getValue(wallet).longValue()).multiply(new BigDecimal("0.00000001"));
    }



    @Override
    public void disconnect() {
        peerGroup.stop();
    }



    @Override
    public void getFee(@Nonnull BigDecimal amount, @Nonnull FeeCallback callback) {
        MainNetParams params = MainNetParams.get();
        amount = amount.multiply(new BigDecimal(100000000));
        Coin coin = Coin.valueOf(amount.longValue());
        Address address = Address.fromBase58(params, "1EsyLqNJ9PFTNiGBRkXVMXR9KJ4zPnhkAi");
        org.bitcoinj.core.Transaction tx;
        try {
            tx = wallet.createSend(address, coin);
        } catch (InsufficientMoneyException e) {
            callback.onFailure(FeeCallback.Code.INSUFFICIENT_FUNDS, "");
            return;
        }

        BigDecimal fee = new BigDecimal(tx.getFee().getValue());
        fee = fee.divide(new BigDecimal(100000000), BigDecimal.ROUND_UP);
        callback.onSuccess(fee);
    }



    @Override
    public void send(@Nonnull String toAddress, @Nonnull BigDecimal amount, @Nonnull BigDecimal fee) {
        MainNetParams params = MainNetParams.get();

        Address address;
        try {
            address = Address.fromBase58(params, toAddress);
        } catch (AddressFormatException e) {
            if (transactionListener != null)
                transactionListener.onFailed(TransactionListener.Error.INVALID_DEST, amount);
            return;
        }

        Coin coin = Coin.valueOf(amount.multiply(new BigDecimal("100000000")).longValue());

        org.bitcoinj.wallet.Wallet.SendResult result;
        try {
            result = wallet.sendCoins(peerGroup, address, coin);
        } catch(InsufficientMoneyException e) {
            if (transactionListener != null)
                transactionListener.onFailed(TransactionListener.Error.INSUFFICIENT_FUNDS, amount);
            return;
        }

        if (transactionListener != null)
            transactionListener.onPending(result.tx);
    }
}
