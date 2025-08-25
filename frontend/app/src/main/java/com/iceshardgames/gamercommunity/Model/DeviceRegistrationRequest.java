// DeviceRegistrationRequest.java
package com.iceshardgames.gamercommunity.Model;

public class DeviceRegistrationRequest {
    private String deviceId;
    private String publicKey;   // keep "" for now (E2E later)
    private String deviceName;  // e.g. "Pixel 7"
    private String deviceType;  // "android"

    public DeviceRegistrationRequest(String deviceId, String publicKey, String deviceName, String deviceType) {
        this.deviceId = deviceId;
        this.publicKey = publicKey;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
    }
}
