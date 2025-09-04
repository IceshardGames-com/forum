package com.iceshardgames.gamercommunity.Model.Response;

import java.util.List;

public class GetPostsResponse {
    private boolean success;
    private Data data;
    private String message;
    private String requestId;
    private String timestamp;
    public boolean isSuccess() { return success; }
    public Data getData() { return data; }
    public String getMessage() {
        return message;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public static class Data {
        private List<CreatePostResponse.Post> posts;
        public List<CreatePostResponse.Post> getPosts() { return posts; }
    }
}
