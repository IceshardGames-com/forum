// DeviceInfo.java
package com.iceshardgames.gamercommunity.Model;

public class DeviceInfo {
    private String deviceId;
    private String publicKey; // may be null now
    private String deviceName;
    private boolean isActive;

    public String getDeviceId() { return deviceId; }
    public String getPublicKey() { return publicKey; }
    public String getDeviceName() { return deviceName; }
    public boolean isActive() { return isActive; }
}
