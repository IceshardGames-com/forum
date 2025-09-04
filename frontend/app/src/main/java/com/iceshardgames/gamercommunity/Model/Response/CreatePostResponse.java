package com.iceshardgames.gamercommunity.Model.Response;

public class CreatePostResponse {
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
        private Post post;
        public Post getPost() { return post; }
    }

    public static class Post {
        private String _id;
        private String forum;
        private String author;
        private String title;
        private String content;
        private int likes;
        private int dislikes;
        private int shares;
        private String createdAt;
        private String updatedAt;
        private int __v;

        public String getId() {
            return _id;
        }

        public String getForum() {
            return forum;
        }

        public String getAuthor() {
            return author;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public int getLikes() {
            return likes;
        }

        public int getDislikes() {
            return dislikes;
        }

        public int getShares() {
            return shares;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public int getV() {
            return __v;
        }
    }
}
