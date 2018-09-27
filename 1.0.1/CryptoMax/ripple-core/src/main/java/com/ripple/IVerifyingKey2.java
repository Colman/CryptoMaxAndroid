package com.ripple;

import com.ripple.crypto.ecdsa.K256VerifyingKey;
import com.ripple.crypto.ed25519.EDVerifyingKey;
import com.ripple.crypto.keys.IVerifyingKey;

/**
 * Created by Colman on 22/06/2018.
 */

public class IVerifyingKey2 {
    public static IVerifyingKey from(byte[] bytes) {
        if (bytes[0] == (byte) 0xED) {
            return EDVerifyingKey.fromCanonicalPubBytes(bytes);
        } else {
            return K256VerifyingKey.fromCanonicalPubBytes(bytes);
        }
    }
}
