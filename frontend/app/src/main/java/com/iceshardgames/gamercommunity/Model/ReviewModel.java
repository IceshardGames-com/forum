package com.iceshardgames.gamercommunity.Model;

public class ReviewModel {
    private String username;
    private String title;
    private String description;
    private String time;
    private int rating;

    public ReviewModel(String username, String title, String description, String time, int rating) {
        this.username = username;
        this.title = title;
        this.description = description;
        this.time = time;
        this.rating = rating;
    }

    public String getUsername() { return username; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTime() { return time; }
    public int getRating() { return rating; }
}
