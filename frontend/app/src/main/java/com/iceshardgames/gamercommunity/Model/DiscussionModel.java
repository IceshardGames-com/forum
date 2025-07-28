package com.iceshardgames.gamercommunity.Model;

public class DiscussionModel {
    private String tag, title, author, replies, timeAgo, tvLikes;

    public DiscussionModel(String tag, String title, String author, String replies, String timeAgo, String tvLikes) {
        this.tag = tag;
        this.title = title;
        this.author = author;
        this.replies = replies;
        this.timeAgo = timeAgo;
        this.tvLikes = tvLikes;
    }

    public String getTag() { return tag; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getReplies() { return replies; }
    public String getTimeAgo() { return timeAgo; }
    public String getTvLikes() { return tvLikes; }
}
