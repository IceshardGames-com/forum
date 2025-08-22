package com.iceshardgames.gamercommunity.Activity.RegisterScreen;

public class RegisterResponse {
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
        private String username;
        private String email;
        private String role;
        private boolean isActive;
        private boolean isEmailVerified;
        private String id;
        private String displayName;

        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }

    public static class Tokens {
        private String accessToken;
        private String refreshToken;

        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}

