package com.iceshardgames.gamercommunity.Model;

import java.util.List;

public class UserSearchResponse {
    private boolean success;
    private Data data;
    private String message;

    public boolean isSuccess() { return success; }
    public Data getData() { return data; }
    public String getMessage() { return message; }

    public static class Data {
        private List<User> users;
        private Pagination pagination;

        public List<User> getUsers() { return users; }
        public Pagination getPagination() { return pagination; }
    }

    public static class User {
        private String _id;
        private String username;
        private String email;
        private String role;
        private boolean isActive;

        public String getId() { return _id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public boolean isActive() { return isActive; }
    }

    public static class Pagination {
        private int page;
        private int limit;
        private int total;

        public int getPage() { return page; }
        public int getLimit() { return limit; }
        public int getTotal() { return total; }
    }
}

