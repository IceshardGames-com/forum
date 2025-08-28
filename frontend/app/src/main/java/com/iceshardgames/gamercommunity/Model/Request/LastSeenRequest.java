package com.iceshardgames.gamercommunity.Model.Request;

public class LastSeenRequest {
    private final String deviceId;
    public LastSeenRequest(String deviceId) { this.deviceId = deviceId; }
    public String getDeviceId() { return deviceId; }
}