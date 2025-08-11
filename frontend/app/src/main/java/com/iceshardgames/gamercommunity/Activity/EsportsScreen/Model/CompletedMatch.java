package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model;

public class CompletedMatch {
    private String team1Name;
    private int team1LogoResId;
    private String team2Name;
    private int team2LogoResId;
    private String score;
    private String matchDate;
    private String date;

    public CompletedMatch(String team1Name, int team1LogoResId,
                          String team2Name, int team2LogoResId,
                          String score, String matchDate) {
        this.team1Name = team1Name;
        this.team1LogoResId = team1LogoResId;
        this.team2Name = team2Name;
        this.team2LogoResId = team2LogoResId;
        this.score = score;
        this.matchDate = matchDate;
    }

    public String getTeam1Name() { return team1Name; }
    public int getTeam1LogoResId() { return team1LogoResId; }
    public String getTeam2Name() { return team2Name; }
    public int getTeam2LogoResId() { return team2LogoResId; }
    public String getScore() { return score; }
    public String getMatchDate() { return matchDate; }
}

