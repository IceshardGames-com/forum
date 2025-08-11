package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model;

public class EsportsMatch {
    private String title;
    private String teamA;
    private String teamB;
    private String status;
    private String score;
    private String prize;
    private String liveNow;
    private int viewers;

    public EsportsMatch(String title, String teamA, String teamB, String status, String score, String prize, String liveNow, int viewers) {
        this.title = title;
        this.teamA = teamA;
        this.teamB = teamB;
        this.status = status;
        this.score = score;
        this.prize = prize;
        this.liveNow = liveNow;
        this.viewers = viewers;
    }

    // Getters
    public String getTitle() { return title; }
    public String getTeamA() { return teamA; }
    public String getTeamB() { return teamB; }
    public String getStatus() { return status; }
    public String getScore() { return score; }
    public String getPrize() { return prize; }
    public String getLiveNow() { return liveNow; }
    public int getViewers() { return viewers; }
}


