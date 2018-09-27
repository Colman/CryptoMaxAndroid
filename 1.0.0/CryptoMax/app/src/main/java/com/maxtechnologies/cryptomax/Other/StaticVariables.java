package com.maxtechnologies.cryptomax.Other;

import android.content.Context;

import com.maxtechnologies.cryptomax.Exchanges.Bitfinex;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Objects.Article;
import com.maxtechnologies.cryptomax.Objects.Contact;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Wallets.Bitcoin;
import com.maxtechnologies.cryptomax.Wallets.Ethereum;
import com.maxtechnologies.cryptomax.Wallets.Ripple;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import okhttp3.OkHttpClient;

/**
 * Created by Colman on 20/02/2018.
 */

public class StaticVariables {
    //File name declarations
    private final static String CONTACTSFILENAME = "contacts.sav";

    //Client declaration
    public static OkHttpClient client = new OkHttpClient();

    //Exchange definition
    public static Exchange[] exchanges = new Exchange[] {
            new Bitfinex()
    };

    //Articles declaration
    public static ArrayList<Article> articles;

    //Contacts declaration
    private static ArrayList<Contact> contacts = new ArrayList<>();

    //Supported wallets declaration
    private static Wallet[] supportedWallets = new Wallet[] {
            new Bitcoin("btc", "", null, null, 0, new ArrayList<Transaction>()),
            new Ethereum("eth", "", null, null, 0, new ArrayList<Transaction>()),
            new Ripple("xrp", "", null, null, 0, new ArrayList<Transaction>())
    };

    //Vibration declarations
    public static int errorLength = 250;
    public static int errorAmplitude = 10;
    public static int successLength = 25;
    public static int successAmplitude = 10;


    public static void init(Context context) {
        loadContacts(context);

    }



    private static void loadContacts(Context context) {
        try {
            FileInputStream fileStream = context.openFileInput(CONTACTSFILENAME);
            ObjectInputStream objStream = new ObjectInputStream(fileStream);
            StaticVariables.contacts = (ArrayList<Contact>) objStream.readObject();
            fileStream.close();
            objStream.close();
        }

        catch(java.io.IOException | java.lang.ClassNotFoundException e) {
            StaticVariables.contacts = new ArrayList<>();
        }
    }



    private static void saveContacts(Context context) {
        context.deleteFile(CONTACTSFILENAME);
        try {
            FileOutputStream fileStream = context.openFileOutput(CONTACTSFILENAME, 0);
            ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
            objStream.writeObject(StaticVariables.contacts);
            objStream.close();
            fileStream.close();
        }

        catch(java.io.IOException e) {
            context.deleteFile(CONTACTSFILENAME);
        }
    }



    public static Contact getContact(int index) {
        return contacts.get(index);
    }



    public static int getContactsSize() {
        return contacts.size();
    }



    public static void addContact(Contact contact, Context context) {
        contacts.add(contact);
        saveContacts(context);
    }



    public static void removeContacts(int[] indices, Context context) {
        ArrayList<Contact> newContacts = new ArrayList<>();
        for(int i = 0; i < contacts.size(); i++) {
            boolean in = false;
            for(int j : indices) {
                if(j == i) {
                    in = true;
                    break;
                }
            }
            if(!in) {
                newContacts.add(contacts.get(i));
            }
        }
        contacts = newContacts;

        saveContacts(context);
    }



    public static Wallet[] getSupportedWallets() {
        Wallet[] wallets = new Wallet[supportedWallets.length];
        for(int i = 0; i < supportedWallets.length; i++) {
            wallets[i] = supportedWallets[i].clone();
            wallets[i].exchangeSymbol = Exchange.translateToExchangeSymbol(wallets[i].exchangeSymbol);
        }

        return wallets;
    }
}
