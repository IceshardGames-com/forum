package com.iceshardgames.gamercommunity.Model;

public class PostModel {
    private String title, author;
    private int replies, likes;
    private boolean isRecent, isPinned;

    public PostModel(String title, String author, int replies, int likes, boolean isRecent, boolean isPinned) {
        this.title = title;
        this.author = author;
        this.replies = replies;
        this.likes = likes;
        this.isRecent = isRecent;
        this.isPinned = isPinned;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getReplies() { return replies; }
    public int getLikes() { return likes; }
    public boolean isRecent() { return isRecent; }
    public boolean isPinned() { return isPinned; }
}
