package com.maxtechnologies.cryptomax.wallets.misc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import android.util.Base64;


import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by Colman on 03/05/2018.
 */

public class EncryptionUtils {


    public static String[] encrypt(String input, String password) {
        //Generate salt and init vector
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = secureRandom.generateSeed(16);
        byte[] initBytes = secureRandom.generateSeed(16);
        IvParameterSpec initVector = new IvParameterSpec(initBytes);

        byte[] encrypted;
        try {
            //Generate secret key from password
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] secret = factory.generateSecret(spec).getEncoded();

            //Encrypt input using secret key
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "AES"), initVector);
            encrypted = cipher.doFinal(input.getBytes());
        }
        catch(NoSuchAlgorithmException e) {
            return null;
        }
        catch(InvalidKeySpecException e) {
            return null;
        }
        catch(NoSuchPaddingException e) {
            return null;
        }
        catch(InvalidAlgorithmParameterException e) {
            return null;
        }
        catch(InvalidKeyException e) {
            return null;
        }
        catch(IllegalBlockSizeException e) {
            return null;
        }
        catch(BadPaddingException e) {
            return null;
        }


        return new String[] {
                Base64.encodeToString(encrypted, Base64.DEFAULT),
                Base64.encodeToString(salt, Base64.DEFAULT),
                Base64.encodeToString(initBytes, Base64.DEFAULT)
        };
    }



    public static String decrypt(String password, String[] encrypted) {
        if(encrypted == null || encrypted.length != 3) {
            return null;
        }

        //Convert salt and init vector
        byte[] saltBytes = Base64.decode(encrypted[1], Base64.DEFAULT);
        byte[] initBytes = Base64.decode(encrypted[2], Base64.DEFAULT);
        IvParameterSpec initVec = new IvParameterSpec(initBytes);

        byte[] decrypted;
        try {
            //Generate secret key from password
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 128);
            byte[] secret = factory.generateSecret(spec).getEncoded();

            //Decrypt input using secret key
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secret, "AES"), initVec);
            decrypted = cipher.doFinal(Base64.decode(encrypted[0], Base64.DEFAULT));
        }
        catch(NoSuchAlgorithmException e) {
            return null;
        }
        catch(InvalidKeySpecException e) {
            return null;
        }
        catch(NoSuchPaddingException e) {
            return null;
        }
        catch(InvalidAlgorithmParameterException e) {
            return null;
        }
        catch(InvalidKeyException e) {
            return null;
        }
        catch(IllegalBlockSizeException e) {
            return null;
        }
        catch(BadPaddingException e) {
            return null;
        }

        return new String(decrypted).trim();
    }
}
