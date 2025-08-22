package com.iceshardgames.gamercommunity.Activity.OtpScreen;

public class ChangePasswordResponse {
    private boolean success;
    private String message;
    private String requestId;
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
