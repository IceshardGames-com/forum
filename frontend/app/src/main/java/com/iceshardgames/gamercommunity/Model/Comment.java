package com.iceshardgames.gamercommunity.Model;


import java.util.ArrayList;
import java.util.List;
import androidx.annotation.Nullable;

public class Comment {
    // visible fields
    private String user;       // display name shown on UI ("You", "ModMaster", etc.)
    private String text;
    private String time;       // human readable time (e.g. "2h ago")
    private int likeCount;
    private int dislikeCount;
    private boolean liked;
    private boolean disliked;
    private List<Comment> replies;
    private String parentServerId; // new: if this comment is a reply, parent comment server id
    private String authorId;


    // server/client ids
    @Nullable private String serverId; // real _id from server (24-hex)
    @Nullable private String clientId; // temporary id generated locally for optimistic creates

    public Comment(String user, String text, String time) {
        this.user = user;
        this.text = text;
        this.time = time;
        this.likeCount = 0;
        this.dislikeCount = 0;
        this.liked = false;
        this.disliked = false;
        this.replies = new ArrayList<>();
        this.serverId = null;
        this.clientId = null;
    }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getParentServerId() { return parentServerId; }
    public void setParentServerId(String parentServerId) { this.parentServerId = parentServerId; }
    // -- getters / setters --
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

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

    @Nullable
    public String getServerId() { return serverId; }
    public void setServerId(@Nullable String serverId) { this.serverId = serverId; }

    @Nullable
    public String getClientId() { return clientId; }
    public void setClientId(@Nullable String clientId) { this.clientId = clientId; }

    // convenience helpers
    public boolean isOptimistic() {
        return serverId == null && clientId != null;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "user='" + user + '\'' +
                ", text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", likeCount=" + likeCount +
                ", dislikeCount=" + dislikeCount +
                ", serverId='" + serverId + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
