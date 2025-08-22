package com.iceshardgames.gamercommunity.Activity.LoginScreen;

public class LoginResponse {
    private boolean success;
    private Data data;
    private String message;
    private String requestId;
    private String timestamp;

    public boolean isSuccess() { return success; }
    public Data getData() { return data; }
    public String getMessage() { return message; }

    public static class Data {
        private User user;
        private Tokens tokens;

        public User getUser() { return user; }
        public Tokens getTokens() { return tokens; }
    }

    public static class User {
        private String _id;
        private String username;
        private String email;
        private String role;
        private boolean isActive;
        private boolean isEmailVerified;
        private String lastLogin;
        private String createdAt;
        private String updatedAt;
        private String displayName;
        private boolean isPrivileged;

        public String getId() { return _id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }

    public static class Tokens {
        private String accessToken;
        private String refreshToken;

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}
