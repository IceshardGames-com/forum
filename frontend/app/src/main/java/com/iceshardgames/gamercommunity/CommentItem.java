package com.iceshardgames.gamercommunity;

import com.google.gson.annotations.SerializedName;

public class CommentItem {
    @SerializedName("_id")
    public String id;
    public String post;
    public Author author;                      // <-- changed from String to Author
    @SerializedName("parentComment")
    public String parentCommentId; // can be null
    public String content;
    public Integer likes;
    public Integer dislikes;
    public String createdAt;

    // convenience getters
    public String getAuthorId() {
        if (author == null) return null;
        // author.id or author._id (both provided in your example)
        return author.id != null ? author.id : author._id;
    }
    public String getAuthorUsername() {
        return author != null ? (author.username != null ? author.username : author.displayName) : null;
    }
}
