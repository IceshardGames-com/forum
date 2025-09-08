package com.iceshardgames.gamercommunity;

public class AddCommentBody {
    public String content;
    public String parentCommentId; // nullable
    public AddCommentBody(String content) { this.content = content; }
    public AddCommentBody(String content, String parentId) { this.content = content; this.parentCommentId = parentId; }
}
