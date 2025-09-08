package com.iceshardgames.gamercommunity;

public class BulkOp {
    public String op; // "post_reaction" | "comment_reaction" | "post_share"
    public String postId; // for post_reaction or post_share
    public String commentId; // for comment_reaction
    public String type; // "like" | "dislike" (for *_reaction only)
    // for create comment
    public String clientId;      // temporary client id so we can reconcile
    public String content;       // comment text
    public String parentComment; // optional parentCommentId for replies
    // new - create comment
    public static BulkOp createComment(String postId, String content, String parentComment, String clientId) {
        BulkOp b = new BulkOp();
        b.op = "create_comment";
        b.postId = postId;
        b.content = content;
        b.parentComment = parentComment; // null for top-level
        b.clientId = clientId;
        return b;
    }
    public static BulkOp postReaction(String postId, String type) {
        BulkOp b = new BulkOp();
        b.op = "post_reaction"; b.postId = postId; b.type = type; return b;
    }
    public static BulkOp commentReaction(String commentId, String type) {
        BulkOp b = new BulkOp();
        b.op = "comment_reaction"; b.commentId = commentId; b.type = type; return b;
    }
    public static BulkOp postShare(String postId) {
        BulkOp b = new BulkOp();
        b.op = "post_share"; b.postId = postId; return b;
    }

}
