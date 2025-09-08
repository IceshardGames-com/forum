package com.iceshardgames.gamercommunity.Utills;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iceshardgames.gamercommunity.BulkOp;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PendingStore {
    private static final String PREF = "InteractionsPending";
    private static final String KEY_OPS = "ops";
    private static PendingStore INSTANCE;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private PendingStore(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static synchronized PendingStore get(Context ctx) {
        if (INSTANCE == null) INSTANCE = new PendingStore(ctx);
        return INSTANCE;
    }

    public synchronized List<BulkOp> loadAll() {
        String json = prefs.getString(KEY_OPS, null);
        if (json == null) return new ArrayList<>();
        Type t = new TypeToken<List<BulkOp>>() {}.getType();
        try {
            return gson.fromJson(json, t);
        } catch (Exception e) {
            Log.e("PendingStore", "parse err", e);
            return new ArrayList<>();
        }
    }

    public synchronized void saveAll(List<BulkOp> list) {
        prefs.edit().putString(KEY_OPS, gson.toJson(list)).apply();
    }

    public synchronized void addOp(BulkOp op) {
        List<BulkOp> list = loadAll();
        for (int i = list.size() - 1; i >= 0; --i) {
            BulkOp p = list.get(i);
            if (sameTarget(p, op)) { list.remove(i); break; }
        }
        list.add(op);
        saveAll(list);
    }

    public synchronized void removeOps(List<BulkOp> toRemove) {
        List<BulkOp> list = loadAll();
        list.removeAll(toRemove);
        saveAll(list);
    }

    public synchronized void removeByClientId(String clientId) {
        if (clientId == null) return;
        List<BulkOp> list = loadAll();
        for (int i = list.size() - 1; i >= 0; --i) {
            BulkOp p = list.get(i);
            if (clientId.equals(p.clientId)) list.remove(i);
        }
        saveAll(list);
    }

    public synchronized void clear() {
        saveAll(new ArrayList<>());
    }

    private boolean sameTarget(BulkOp a, BulkOp b) {
        if (a == null || b == null) return false;
        if (a.op == null || b.op == null) return false;
        if (a.op.equals("post_reaction") && b.op.equals("post_reaction") && a.postId != null && a.postId.equals(b.postId)) return true;
        if (a.op.equals("comment_reaction") && b.op.equals("comment_reaction") && a.commentId != null && a.commentId.equals(b.commentId)) return true;
        if (a.op.equals("create_comment") && b.op.equals("create_comment") && a.clientId != null && a.clientId.equals(b.clientId)) return true;
        return false;
    }

    // confirmed-created helpers (persist server-confirmed created comments so list can reattach them)
    public synchronized void saveConfirmedCreatedComment(String postId, String clientId, String createdJson) {
        if (clientId == null) return;
        String key = "confirmed_" + postId;
        List<String> list = loadStringList(key);
        // dedupe by clientId (we assume stored JSON includes "clientId")
        boolean exists = false;
        for (String s : list) {
            if (s != null && s.contains("\"clientId\":\"" + clientId + "\"")) { exists = true; break; }
        }
        if (!exists) {
            list.add(createdJson);
            saveStringList(key, list);
        }
    }

    public synchronized List<String> loadConfirmedCreatedComments(String postId) {
        String key = "confirmed_" + postId;
        return loadStringList(key);
    }

    public synchronized void removeConfirmedCreatedByClientId(String postId, String clientId) {
        if (clientId == null) return;
        String key = "confirmed_" + postId;
        List<String> list = loadStringList(key);
        for (int i = list.size() - 1; i >= 0; --i) {
            String s = list.get(i);
            if (s != null && s.contains("\"clientId\":\"" + clientId + "\"")) {
                list.remove(i);
            }
        }
        saveStringList(key, list);
    }

    // helper to store/retrieve String list
    private List<String> loadStringList(String key) {
        String json = prefs.getString(key, null);
        if (json == null) return new ArrayList<>();
        try {
            Type t = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(json, t);
        } catch (Exception e) {
            Log.e("PendingStore", "parse err for " + key, e);
            return new ArrayList<>();
        }
    }
    private void saveStringList(String key, List<String> list) {
        prefs.edit().putString(key, gson.toJson(list)).apply();
    }
}
