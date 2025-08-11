package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model;

public class Team {
    private int rank;
    private String name;
    private int points;
    private int wins;
    private int losses;
    private int imageResId; // Resource ID for team logo

    public Team(int rank, String name, int points, int wins, int losses, int imageResId) {
        this.rank = rank;
        this.name = name;
        this.points = points;
        this.wins = wins;
        this.losses = losses;
        this.imageResId = imageResId;
    }

    public int getRank() { return rank; }
    public String getName() { return name; }
    public int getPoints() { return points; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getImageResId() { return imageResId; }
}

