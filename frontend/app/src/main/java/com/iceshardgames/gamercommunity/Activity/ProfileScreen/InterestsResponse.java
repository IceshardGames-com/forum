package com.iceshardgames.gamercommunity.Activity.ProfileScreen;


import java.util.List;

public class InterestsResponse {
    private boolean success;
    private Data data;
    private String message;

    public boolean isSuccess() { return success; }
    public Data getData() { return data; }
    public String getMessage() { return message; }

    public static class Data {
        private List<Item> items;
        private int page;
        private int limit;
        private int total;

        public List<Item> getItems() { return items; }
    }

    public static class Item {
        private String _id;
        private String label;
        private String value;

        public String getId() { return _id; }
        public String getLabel() { return label; }
        public String getValue() { return value; }
    }
}
