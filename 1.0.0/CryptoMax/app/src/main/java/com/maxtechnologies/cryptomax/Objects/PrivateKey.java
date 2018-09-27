package com.maxtechnologies.cryptomax.Objects;

import java.io.Serializable;

/**
 * Created by Colman on 16/05/2018.
 */

public class PrivateKey implements Serializable {
    public String encrypted;
    public String passwordSalt;
    public String passwordIv;
    public boolean fingerprint;
    public String email;


    public PrivateKey(String encrypted, String passwordSalt, String passwordIv, boolean fingerprint, String email) {
        this.encrypted = encrypted;
        this.passwordSalt = passwordSalt;
        this.passwordIv = passwordIv;
        this.fingerprint = fingerprint;
        this.email = email;
    }
}
