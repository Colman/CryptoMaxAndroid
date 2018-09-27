package com.maxtechnologies.cryptomax.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.maxtechnologies.cryptomax.R;
import com.maxtechnologies.cryptomax.api.CryptoMaxApi;
import com.maxtechnologies.cryptomax.exchange.Bitfinex;
import com.maxtechnologies.cryptomax.exchange.Exchange;
import com.maxtechnologies.cryptomax.exchange.ExchangeCallback;
import com.maxtechnologies.cryptomax.exchange.asset.Coin;
import com.maxtechnologies.cryptomax.exchange.asset.FiatFiatPair;
import com.maxtechnologies.cryptomax.ui.misc.AlertController;
import com.maxtechnologies.cryptomax.wallets.Wallet;
import com.maxtechnologies.cryptomax.wallets.bitcoin.Bitcoin;
import com.maxtechnologies.cryptomax.wallets.bitcoin.TransactionListener;
import com.maxtechnologies.cryptomax.wallets.ethereum.CompletedTransaction;
import com.maxtechnologies.cryptomax.wallets.ethereum.Ethereum;
import com.maxtechnologies.cryptomax.wallets.ripple.Ripple;
import com.ripple.core.serialized.enums.TransactionType;

import org.bitcoinj.core.Transaction;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by Colman on 15/07/2018.
 */

public abstract class MainActivity extends AppCompatActivity {

    //Wallet filenames
    private final static String BITCOINSFILENAME = "bitcoins.sav";
    private final static String ETHEREUMSFILENAME = "ethereums.sav";
    private final static String RIPPLESFILENAME = "ripples.sav";

    private static Map<String, Coin> walletCoins;
    private static FiatFiatPair[] fiats;

    //Wallet declarations
    private static ArrayList<Bitcoin> bitcoins;
    private static ArrayList<Ethereum> ethereums;
    private static ArrayList<Ripple> ripples;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadWallets();
    }



    public void onStart() {
        super.onStart();

        for (int i = 0; i < bitcoins.size(); i++) {
            final Bitcoin bitcoin = getBitcoin(i);

            bitcoin.addTransactionListener(new TransactionListener() {
                @Override
                public void onFailed(Error error, BigDecimal amount) {
                    Coin coin = walletCoins.get("Bitcoin");
                    String amountStr = coin.assetString(amount, true, true);

                    String errorString;
                    if (error == Error.INSUFFICIENT_FUNDS) {
                        errorString = "the wallet has insufficient funds.";
                    } else if (error == Error.INVALID_DEST) {
                        errorString = "the destination is invalid.";
                    } else {
                        errorString = "of a network error.";
                    }

                    String toastStr = getResources().getString(R.string.transaction_failed, amountStr, errorString);
                    Toast toast = Toast.makeText(MainActivity.this,
                            toastStr,
                            Toast.LENGTH_LONG);
                    toast.show();
                }

                @Override
                public void onPending(Transaction transaction) {
                    Coin coin = walletCoins.get("Bitcoin");
                    BigDecimal amount = bitcoin.getAmount(transaction);
                    String amountStr = coin.assetString(amount, true, true);
                    Toast toast = Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.transaction_pending, amountStr),
                            Toast.LENGTH_LONG);
                    toast.show();
                }

                @Override
                public void onConfirmed(Transaction transaction) {
                    Coin coin = walletCoins.get("Bitcoin");
                    BigDecimal amount = bitcoin.getAmount(transaction);
                    String amountStr = coin.assetString(amount, true, true);
                    Toast toast = Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.transaction_complete, amountStr),
                            Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }


        for (int i = 0; i < ethereums.size(); i++) {
            Ethereum ethereum = getEthereum(i);

            ethereum.addTransactionListener(new com.maxtechnologies.cryptomax.wallets.ethereum.TransactionListener() {
                @Override
                public void onFailed(Error error, BigDecimal amount) {
                    Coin coin = walletCoins.get("Ethereum");
                    String amountStr = coin.assetString(amount, true, true);

                    String errorString;
                    if (error == com.maxtechnologies.cryptomax.wallets.ethereum.TransactionListener.Error.INSUFFICIENT_FUNDS) {
                        errorString = "the wallet has insufficient funds.";
                    } else {
                        errorString = "of a network error.";
                    }

                    String toastStr = getResources().getString(R.string.transaction_failed, amountStr, errorString);
                    Toast toast = Toast.makeText(MainActivity.this,
                            toastStr,
                            Toast.LENGTH_LONG);
                    toast.show();
                }

                @Override
                public void onPending(org.ethereum.core.Transaction transaction) {
                    Coin coin = walletCoins.get("Ethereum");
                    BigDecimal amount = Ethereum.byteArrayToEth(transaction.getValue());
                    String amountStr = coin.assetString(amount, true, true);

                    Toast toast = Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.transaction_pending, amountStr),
                            Toast.LENGTH_LONG);
                    toast.show();
                }

                @Override
                public void onConfirmed(CompletedTransaction transaction) {
                    Coin coin = walletCoins.get("Ethereum");
                    BigDecimal amount = Ethereum.byteArrayToEth(transaction.getValue());
                    String amountStr = coin.assetString(amount, true, true);

                    Toast toast = Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.transaction_complete, amountStr),
                            Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        for (int i = 0; i < ripples.size(); i++) {
            Ripple ripple = getRipple(i);

            ripple.addTransactionListener(new com.maxtechnologies.cryptomax.wallets.ripple.TransactionListener() {
                @Override
                public void onFailed(Error error, TransactionType type) {
                    String errorString;
                    if (error == com.maxtechnologies.cryptomax.wallets.ripple.TransactionListener.Error.INSUFFICIENT_FUNDS) {
                        errorString = "the account has insufficient funds.";
                    } else if (error == com.maxtechnologies.cryptomax.wallets.ripple.TransactionListener.Error.INVALID_DEST) {
                        errorString = "the destination is invalid.";
                    } else {
                        errorString = "of a network error.";
                    }

                    String toastStr = getResources().getString(R.string.ripple_failed, type.name().toLowerCase(), errorString);
                    Toast toast = Toast.makeText(MainActivity.this,
                            toastStr,
                            Toast.LENGTH_LONG);
                    toast.show();
                }

                @Override
                public void onPending(com.ripple.core.types.known.tx.Transaction transaction) {
                    String type = transaction.transactionType().name().toLowerCase();

                    Toast toast = Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.ripple_pending, type),
                            Toast.LENGTH_LONG);
                    toast.show();
                }

                @Override
                public void onValidated(com.ripple.core.types.known.tx.Transaction transaction) {
                    String type = transaction.transactionType().name().toLowerCase();

                    Toast toast = Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.ripple_complete, type),
                            Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }



    @Override
    public void onStop() {
        super.onStop();

        for (int i = 0; i < bitcoins.size(); i++) {
            bitcoins.get(i).disconnect();
        }

        for (int i = 0; i < ethereums.size(); i++) {
            ethereums.get(i).disconnect();
        }

        for (int i = 0; i < ripples.size(); i++) {
            ripples.get(i).disconnect();
        }
    }



    public static void setWalletCoins(@Nonnull  Map<String, Coin> walletCoins) {
        MainActivity.walletCoins = walletCoins;
    }



    public static Coin getWalletCoin(String name) {
        return walletCoins.get(name);
    }



    public static void setFiats(@Nonnull  FiatFiatPair[] fiats) {
        MainActivity.fiats = fiats;
    }



    private void loadWallets() {
        try {
            FileInputStream stream = openFileInput(BITCOINSFILENAME);
            ObjectInputStream objStream = new ObjectInputStream(stream);
            bitcoins = (ArrayList<Bitcoin>) objStream.readObject();
            stream.close();
            objStream.close();
        } catch(java.io.IOException | java.lang.ClassNotFoundException e) {
            bitcoins = new ArrayList<>();
        }

        try {
            FileInputStream stream = openFileInput(ETHEREUMSFILENAME);
            ObjectInputStream objStream = new ObjectInputStream(stream);
            ethereums = (ArrayList<Ethereum>) objStream.readObject();
            stream.close();
            objStream.close();
        } catch(java.io.IOException | java.lang.ClassNotFoundException e) {
            ethereums = new ArrayList<>();
        }

        try {
            FileInputStream stream = openFileInput(RIPPLESFILENAME);
            ObjectInputStream objStream = new ObjectInputStream(stream);
            ripples = (ArrayList<Ripple>) objStream.readObject();
            stream.close();
            objStream.close();
        } catch(java.io.IOException | java.lang.ClassNotFoundException e) {
            ripples = new ArrayList<>();
        }
    }



    public void saveWallets(boolean upload) {
        try {
            FileOutputStream fileStream = openFileOutput(BITCOINSFILENAME, 0);
            ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
            objStream.writeObject(bitcoins);
            objStream.close();
            fileStream.close();
        } catch(java.io.IOException e) {
            //Do nothing
        }

        try {
            FileOutputStream fileStream = openFileOutput(ETHEREUMSFILENAME, 0);
            ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
            objStream.writeObject(ethereums);
            objStream.close();
            fileStream.close();
        } catch(java.io.IOException e) {
            //Do nothing
        }

        try {
            FileOutputStream fileStream = openFileOutput(RIPPLESFILENAME, 0);
            ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
            objStream.writeObject(ripples);
            objStream.close();
            fileStream.close();
        } catch(java.io.IOException e) {
            //Do nothing
        }

        if (upload) {
            ArrayList<Wallet> wallets = new ArrayList<>();
            wallets.addAll(bitcoins);
            wallets.addAll(ethereums);
            wallets.addAll(ripples);

            Wallet[] walletsArr = wallets.toArray(new Wallet[bitcoins.size() + ethereums.size() + ripples.size()]);
            CryptoMaxApi.uploadWallets(walletsArr);
        }
    }



    public Bitcoin getBitcoin(int index) {
        return bitcoins.get(index);
    }



    public int getBitcoinsSize() {
        return bitcoins.size();
    }



    public void addBitcoin(Bitcoin bitcoin) {
        bitcoins.add(bitcoin);
        saveWallets(true);
    }



    public void removeBitcoins(int[] indices) {
        ArrayList<Bitcoin> newBitcoins = new ArrayList<>();
        for(int i = 0; i < bitcoins.size(); i++) {
            boolean in = false;
            for(int j : indices) {
                if(j == i) {
                    in = true;
                    break;
                }
            }
            if(!in)
                newBitcoins.add(bitcoins.get(i));
        }
        bitcoins = newBitcoins;

        saveWallets(true);
    }



    public Ethereum getEthereum(int index) {
        return ethereums.get(index);
    }



    public int getEthereumsSize() {
        return ethereums.size();
    }



    public void addEthereum(Ethereum ethereum) {
        ethereums.add(ethereum);
        saveWallets(true);
    }



    public void removeEthereums(int[] indices) {
        ArrayList<Ethereum> newEthereums = new ArrayList<>();
        for(int i = 0; i < ethereums.size(); i++) {
            boolean in = false;
            for(int j : indices) {
                if(j == i) {
                    in = true;
                    break;
                }
            }
            if(!in)
                newEthereums.add(ethereums.get(i));
        }
        ethereums = newEthereums;

        saveWallets(true);
    }



    public Ripple getRipple(int index) {
        return ripples.get(index);
    }



    public int getRipplesSize() {
        return ripples.size();
    }



    public void addRipple(Ripple ripple) {
        ripples.add(ripple);
        saveWallets(true);
    }



    public void removeRipples(int[] indices) {
        ArrayList<Ripple> newRipples = new ArrayList<>();
        for(int i = 0; i < ripples.size(); i++) {
            boolean in = false;
            for(int j : indices) {
                if(j == i) {
                    in = true;
                    break;
                }
            }
            if(!in)
                newRipples.add(ripples.get(i));
        }
        ripples = newRipples;

        saveWallets(true);
    }
}
