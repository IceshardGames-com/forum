package com.iceshardgames.gamercommunity.Model.Response;


public class CreateForumResponse {
    private boolean success;
    private Data data;
    private String message;
    private String requestId;
    private String timestamp;

    public boolean isSuccess() { return success; }
    public Data getData() { return data; }
    public String getMessage() { return message; }
    public String getRequestId() { return requestId; }
    public String getTimestamp() { return timestamp; }

    public static class Data {
        private Forum forum;
        public Forum getForum() { return forum; }
    }

    public static class Forum {
        private String uuid;
        private String name;
        private String slug;
        private String description;
        private String owner;
        private boolean verified;
        private String postPermission;
        private int followersCount;
        private int membersCount;
        private String _id;
        private String createdAt;
        private String updatedAt;
        private int __v;
        private String id;

        public String getUuid() { return uuid; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
        public String getDescription() { return description; }
        public String getOwner() { return owner; }
        public boolean isVerified() { return verified; }
        public String getPostPermission() { return postPermission; }
        public int getFollowersCount() { return followersCount; }
        public int getMembersCount() { return membersCount; }
        public String getCreatedAt() { return createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public String getId() { return id; }
    }
}
