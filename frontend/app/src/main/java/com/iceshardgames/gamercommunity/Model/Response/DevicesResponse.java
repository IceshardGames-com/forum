// DevicesResponse.java
package com.iceshardgames.gamercommunity.Model.Response;

import com.iceshardgames.gamercommunity.Model.DeviceInfo;

import java.util.List;

public class DevicesResponse {
    private boolean success;
    private String message;
    private Data data;
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Data getData() { return data; }
    public static class Data {
        private java.util.List<DeviceInfo> devices;
        public List<DeviceInfo> getDevices() { return devices; }
    }
}
