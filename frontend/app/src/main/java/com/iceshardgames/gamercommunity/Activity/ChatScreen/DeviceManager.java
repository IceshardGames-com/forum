// DeviceManager.java
package com.iceshardgames.gamercommunity.Activity.ChatScreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.UUID;

public class DeviceManager {
    private final SharedPreferences sp;
    private static final String KEY_ALIAS = "gc_msg_key"; // keystore alias

    public DeviceManager(Context c){ sp = c.getSharedPreferences("ChatDevice", Context.MODE_PRIVATE); }

    public String getOrCreateDeviceId(){
        String id = UUID.randomUUID().toString();
     /*   if(id == null){
            id = UUID.randomUUID().toString();
            sp.edit().putString("deviceId", id).apply();
        }*/
        return id;
    }

    public String getDeviceName(){
        return Build.MANUFACTURER + " " + Build.MODEL;
    }
    /** Create EC P-256 keypair in Android Keystore if missing */
    private void ensureKeypair() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            if (ks.containsAlias(KEY_ALIAS)) return;

            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");

            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_AGREE_KEY)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1")) // P-256
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setUserAuthenticationRequired(false)
                    .build();

            kpg.initialize(spec);
            kpg.generateKeyPair();
        } catch (Exception e) {
            android.util.Log.e("DeviceManager", "ensureKeypair", e);
        }
    }
    public String getPublicKey() {
        try {
            ensureKeypair();
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            PublicKey pub = ks.getCertificate(KEY_ALIAS).getPublicKey();
            byte[] x509 = pub.getEncoded(); // ASN.1 SubjectPublicKeyInfo
            return android.util.Base64.encodeToString(x509, android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            android.util.Log.e("DeviceManager", "getPublicKey", e);
            return ""; // if this is empty your server will likely 400
        }
    }
}
