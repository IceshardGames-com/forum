package com.iceshardgames.gamercommunity.Model.Response;


import java.util.List;

public class FriendListResponse {
    private boolean success;
    private Data data;
    private String message;
    private String requestId;
    private String timestamp;

    // ==== Getters & Setters ====
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // ==== Inner Data Class ====
    public static class Data {
        private List<Friend> friends;
        private int page;
        private int limit;

        public List<Friend> getFriends() {
            return friends;
        }

        public void setFriends(List<Friend> friends) {
            this.friends = friends;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }

    // ==== Inner Friend Class ====
    public static class Friend {
        private String _id;
        private String username;
        private String role;

        public String getId() {
            return _id;
        }

        public void setId(String _id) {
            this._id = _id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}

