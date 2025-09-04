package com.iceshardgames.gamercommunity.Model.Response;

public class PostLikeResponse {
    private boolean success;
    private String message;
    private String requestId;
    private String timestamp;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getRequestId() { return requestId; }
    public String getTimestamp() { return timestamp; }
}
