package com.iceshardgames.gamercommunity.Model;

public class NewsModel {
    private String source;
    private String label;
    private String title;
    private String time;

    public NewsModel(String source, String label, String title, String time) {
        this.source = source;
        this.label = label;
        this.title = title;
        this.time = time;
    }

    public String getSource() { return source; }
    public String getLabel() { return label; }
    public String getTitle() { return title; }
    public String getTime() { return time; }
}

