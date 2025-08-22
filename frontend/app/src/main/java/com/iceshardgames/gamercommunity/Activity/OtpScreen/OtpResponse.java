package com.iceshardgames.gamercommunity.Activity.OtpScreen;

import com.google.gson.annotations.SerializedName;

public class OtpResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("requestId")
    private String requestId;

    @SerializedName("timestamp")
    private String timestamp;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
