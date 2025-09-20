package com.iceshardgames.gamercommunity.Model;

public class PostModel {
    private String title, author,AuthorId,name;
    private String postId;   // <-- add

    private int replies, likes, dislikes;
    private boolean isRecent, isPinned;
    private long createdAt; // store in millis
    private boolean requiresFollow;
    private boolean requiresJoin;
    public PostModel(String postId,String title, String author, int replies, int likes, boolean isRecent, boolean isPinned, long createdAt, int dislikes,String AuthorId,String name) {
        this.postId = postId;        // <-- set
        this.title = title;
        this.author = author;
        this.replies = replies;
        this.likes = likes;
        this.isRecent = isRecent;
        this.isPinned = isPinned;
        this.createdAt = createdAt; // <-- do NOT set to System.currentTimeMillis() here
        this.dislikes = dislikes;
        this.AuthorId = AuthorId;
        this.name = name;

    }
    public String getPostId() { return postId; }   // <-- getter
    public String getName() { return name; }   // <-- getter

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getAuthorId() { return AuthorId; }
    public int getReplies() { return replies; }
    public int getLikes() { return likes; }
    public int getDisLikes() { return dislikes; }
    public boolean isRecent() { return isRecent; }
    public boolean isPinned() { return isPinned; }
    public boolean requiresFollow() { return requiresFollow; }
    public boolean requiresJoin() { return requiresJoin; }

    public void setRequiresFollow(boolean requiresFollow) {
        this.requiresFollow = requiresFollow;
    }
    public void setRequiresJoin(boolean requiresJoin) {
        this.requiresJoin = requiresJoin;
    }
}
