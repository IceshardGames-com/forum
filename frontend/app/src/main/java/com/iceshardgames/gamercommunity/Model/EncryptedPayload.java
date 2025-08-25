// EncryptedPayload.java (kept generic; ciphertext = plain text for now)
package com.iceshardgames.gamercommunity.Model;

public class EncryptedPayload {
    private String deviceId;
    private String ciphertext; // TEMP: plain text when E2E is off
    public EncryptedPayload(String deviceId, String ciphertext){
        this.deviceId = deviceId; this.ciphertext = ciphertext;
    }
    public String getDeviceId(){ return deviceId; }
    public String getCiphertext(){ return ciphertext; }
}
