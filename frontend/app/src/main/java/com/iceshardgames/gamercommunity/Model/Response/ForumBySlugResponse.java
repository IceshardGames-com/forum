package com.iceshardgames.gamercommunity.Model.Response;

// inside ForumsFragmentBottom (near bottom of file) or in a separate file

public class ForumBySlugResponse {
    private boolean success;
    private Data data;
    private String message;
    // getters
    public boolean isSuccess() { return success; }
    public Data getData() { return data; }
    public String getMessage() { return message; }

    public static class Data {
        private Forum forum;
        public Forum getForum() { return forum; }

        public static class Forum {
            // fields based on sample response
            private String _id;
            private String uuid;
            private String name;
            private String slug;
            private String description;
            private String owner;
            private boolean verified;
            private String postPermission;
            private int followersCount;
            private int membersCount;
            private String createdAt;
            private String updatedAt;
            private String id; // also provided
            // getters
            public String get_id() { return _id; }
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
            public String getId() { return id != null ? id : _id; }
        }
    }
}

