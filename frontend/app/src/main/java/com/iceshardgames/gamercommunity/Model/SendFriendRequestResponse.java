package com.iceshardgames.gamercommunity.Model;

public class SendFriendRequestResponse {
    private boolean success;
    private Data data;
    private String message;
    private String requestId;
    private String timestamp;

    // --- Inner Data class ---
    public static class Data {
        private Request request;

        public Request getRequest() {
            return request;
        }
    }

    // --- Inner Request class ---
    public static class Request {
        private String requester;
        private String recipient;
        private String status;
        private String _id;
        private String createdAt;
        private String updatedAt;
        private int __v;

        public String getId() {
            return _id;
        }

        public String getRequester() {
            return requester;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getStatus() {
            return status;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public Data getData() {
        return data;
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
