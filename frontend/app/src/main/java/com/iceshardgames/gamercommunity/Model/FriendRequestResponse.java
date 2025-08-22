package com.iceshardgames.gamercommunity.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FriendRequestResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private Data data;

    @SerializedName("message")
    private String message;

    @SerializedName("requestId")
    private String requestId;

    @SerializedName("timestamp")
    private String timestamp;

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

    // ---------------- Inner Data Class ----------------
    public static class Data {
        @SerializedName("requests")
        private List<Request> requests;

        @SerializedName("type")
        private String type;

        @SerializedName("page")
        private int page;

        @SerializedName("limit")
        private int limit;

        public List<Request> getRequests() {
            return requests;
        }

        public String getType() {
            return type;
        }

        public int getPage() {
            return page;
        }

        public int getLimit() {
            return limit;
        }
    }

    // ---------------- Request Class ----------------
    public static class Request {
        @SerializedName("_id")
        private String id;

        @SerializedName("createdAt")
        private String createdAt;

        @SerializedName("user")
        private User user;

        public String getId() {
            return id;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public User getUser() {
            return user;
        }
    }

    // ---------------- User Class ----------------
    public static class User {
        @SerializedName("_id")
        private String id;

        @SerializedName("username")
        private String username;

        @SerializedName("email")
        private String email;

        public String getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }
    }
}
