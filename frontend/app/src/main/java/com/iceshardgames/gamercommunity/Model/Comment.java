package com.iceshardgames.gamercommunity.Model;


import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String user;
    private String text;
    private String time;
    private int likeCount;
    private int dislikeCount;
    private boolean liked;
    private boolean disliked;
    private List<Comment> replies;

    public Comment(String user, String text, String time) {
        this.user = user;
        this.text = text;
        this.time = time;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.liked = false;
        this.disliked = false;
        this.replies = new ArrayList<>();
    }

    public String getUser() { return user; }
    public String getText() { return text; }
    public String getTime() { return time; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }

    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }

    public boolean isDisliked() { return disliked; }
    public void setDisliked(boolean disliked) { this.disliked = disliked; }

    public List<Comment> getReplies() { return replies; }
    public void addReply(Comment reply) { this.replies.add(reply); }
}
