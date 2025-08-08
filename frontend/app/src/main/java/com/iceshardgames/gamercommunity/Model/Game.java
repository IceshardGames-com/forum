package com.iceshardgames.gamercommunity.Model;

public class Game {
    private String title;
    private String genre;
    private int imageResId;
    private double rating;
    private int totalRatings;

    public Game(String title, String genre, int imageResId, double rating, int totalRatings) {
        this.title = title;
        this.genre = genre;
        this.imageResId = imageResId;
        this.rating = rating;
        this.totalRatings = totalRatings;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getImageResId() {
        return imageResId;
    }

    public double getRating() {
        return rating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }
}