package com.iceshardgames.gamercommunity.Model.Response;

public class FollowResponse {
    private boolean success;
    private Data data;
    private String message;

    public boolean isSuccess() { return success; }
    public Data getData() { return data; }
    public String getMessage() { return message; }

    public static class Data {
        private Member member;
        public Member getMember() { return member; }
    }

    public static class Member {
        private String _id;
        private String forum;
        private String user;
        private String role;
        private boolean isFollower;

        public boolean isFollower() { return isFollower; }
    }
}
