package com.iceshardgames.gamercommunity;

import com.google.gson.annotations.SerializedName;

public class CommentItem {
    @SerializedName("_id") public String id;
    public String post;
    public String author;
    @SerializedName("parentComment") public String parentCommentId; // can be null
    public String content;
    public int likes;
    public int dislikes;
    public String createdAt;
}
