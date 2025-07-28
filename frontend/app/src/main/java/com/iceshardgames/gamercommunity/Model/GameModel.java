package com.iceshardgames.gamercommunity.Model;

public class GameModel {
    private String title;
    private String genre;
    private int imageResId;
    private int gamePercetage;
    private double rating;

    public GameModel(String title, String genre, int imageResId, double rating, int gamePercetage) {
        this.title = title;
        this.genre = genre;
        this.imageResId = imageResId;
        this.rating = rating;
        this.gamePercetage = gamePercetage;
    }

    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getImageResId() { return imageResId; }
    public double getRating() { return rating; }
    public int getGamePercetage() { return gamePercetage; }
    public void setGamePercetage(int gamePercetage) { this.gamePercetage = gamePercetage; }
}

