package com.iceshardgames.gamercommunity;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Utills.PendingStore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InteractionsBuffer {
    private final PendingStore pendingStore;
    private static volatile InteractionsBuffer INSTANCE;
    private final ApiService api;
    private final Context appCtx;


    // thread-safe list for concurrent UI pushes
    private final CopyOnWriteArrayList<BulkOp> buffer = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // listener to notify app that bulk succeeded (main-thread)
    private volatile OnBulkSuccessListener onBulkSuccessListener;

    public interface OnBulkSuccessListener {
        void onBulkSuccess(List<String> affectedPostIds);

        void onCreateCommentConfirmed(String postId, String clientId, CommentItem created);
    }

    public void setOnBulkSuccessListener(OnBulkSuccessListener listener) {
        this.onBulkSuccessListener = listener;
    }

    public void clearOnBulkSuccessListener() {
        this.onBulkSuccessListener = null;
    }

    private InteractionsBuffer(Context ctx) {
        this.appCtx = ctx.getApplicationContext();
        api = ApiClient.getRetrofit().create(ApiService.class);
        // start periodic flush: first after 30s, then every 30s
        scheduler.scheduleAtFixedRate(this::flush, 30, 30, TimeUnit.SECONDS);
        this.pendingStore = PendingStore.get(this.appCtx);
    }

    public static InteractionsBuffer get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (InteractionsBuffer.class) {
                if (INSTANCE == null)
                    INSTANCE = new InteractionsBuffer(ctx.getApplicationContext());
            }
        }
        return INSTANCE;
    }

    // --- enqueue methods (with toggle/replace behavior) ---
    // ---------- POST reactions ----------
    public void likePost(String postId) {
        if (postId == null) return;

        // Find any existing reaction for this post (either in-memory or persisted)
        BulkOp existing = findAnyExistingReaction("post_reaction", postId);

        if (existing == null) {
            // No existing reaction -> enqueue like
            BulkOp op = BulkOp.postReaction(postId, "like");
            buffer.add(op);
            pendingStore.addOp(op);
            Log.d("InteractionsBuffer", "likePost enqueued for postId=" + postId + " (buffer size=" + buffer.size() + ")");
            return;
        }

        // Existing reaction found:
        if ("like".equals(existing.type)) {
            // same type -> user intends to toggle off -> remove existing
            Log.d("InteractionsBuffer", "Toggling off existing post like for " + postId);
            removeOpFromBufferAndPending(existing);
        } else {
            // different type -> replace (e.g. dislike -> like)
            Log.d("InteractionsBuffer", "Replacing existing post reaction (" + existing.type + ") with like for " + postId);
            BulkOp newOp = BulkOp.postReaction(postId, "like");
            replaceOrAddOp(existing, newOp);
        }
    }

    public void dislikePost(String postId) {
        if (postId == null) return;

        BulkOp existing = findAnyExistingReaction("post_reaction", postId);

        if (existing == null) {
            BulkOp op = BulkOp.postReaction(postId, "dislike");
            buffer.add(op);
            pendingStore.addOp(op);
            Log.d("InteractionsBuffer", "dislikePost enqueued for postId=" + postId + " (buffer size=" + buffer.size() + ")");
            return;
        }

        if ("dislike".equals(existing.type)) {
            Log.d("InteractionsBuffer", "Toggling off existing post dislike for " + postId);
            removeOpFromBufferAndPending(existing);
        } else {
            Log.d("InteractionsBuffer", "Replacing existing post reaction (" + existing.type + ") with dislike for " + postId);
            BulkOp newOp = BulkOp.postReaction(postId, "dislike");
            replaceOrAddOp(existing, newOp);
        }
    }

    // ---------- COMMENT reactions ----------
    public void likeComment(String commentId, String postId) {
        if (commentId == null) return;

        BulkOp existing = findAnyExistingReaction("comment_reaction", commentId);

        if (existing == null) {
            BulkOp op = BulkOp.commentReaction(postId,commentId, "like");
            buffer.add(op);
            pendingStore.addOp(op);
            Log.d("InteractionsBuffer", "likeComment enqueued for commentId=" + commentId + " (buffer size=" + buffer.size() + ")");
            return;
        }

        if ("like".equals(existing.type)) {
            Log.d("InteractionsBuffer", "Toggling off existing comment like for " + commentId);
            removeOpFromBufferAndPending(existing);
        } else {
            Log.d("InteractionsBuffer", "Replacing existing comment reaction (" + existing.type + ") with like for " + commentId);
            BulkOp newOp = BulkOp.commentReaction(postId,commentId, "like");
            replaceOrAddOp(existing, newOp);
        }
    }

    public void dislikeComment(String commentId, String postId) {
        if (commentId == null) return;

        BulkOp existing = findAnyExistingReaction("comment_reaction", commentId);

        if (existing == null) {
            BulkOp op = BulkOp.commentReaction(postId,commentId, "dislike");
            buffer.add(op);
            pendingStore.addOp(op);
            Log.d("InteractionsBuffer", "dislikeComment enqueued for commentId=" + commentId + " (buffer size=" + buffer.size() + ")");
            return;
        }

        if ("dislike".equals(existing.type)) {
            Log.d("InteractionsBuffer", "Toggling off existing comment dislike for " + commentId);
            removeOpFromBufferAndPending(existing);
        } else {
            Log.d("InteractionsBuffer", "Replacing existing comment reaction (" + existing.type + ") with dislike for " + commentId);
            BulkOp newOp = BulkOp.commentReaction(postId,commentId, "dislike");
            replaceOrAddOp(existing, newOp);
        }
    }

    public void enqueueCreateComment(String postId, String content, String parentCommentId, String clientId) {
        BulkOp op = BulkOp.createComment(postId, content, parentCommentId, clientId);
        buffer.add(op);
        pendingStore.addOp(op);
        Log.d("InteractionsBuffer", "Enqueued create_comment clientId=" + clientId + " post=" + postId + " (buffer size=" + buffer.size() + ")");
    }

    public void sharePost(String postId) {
        BulkOp op = BulkOp.postShare(postId);
        buffer.add(op);
        pendingStore.addOp(op);
        Log.d("InteractionsBuffer", "sharePost clientId=" + postId + " post=" + postId + " (buffer size=" + buffer.size() + ")");
    }

    //onetime
  /*  public void sharePost(String postId) {
        if (postId == null) return;

        // Avoid duplicate share entries (in-memory or persisted)
        if (postShareExists(postId)) {
            Log.d("InteractionsBuffer", "Ignoring duplicate sharePost for " + postId);
            return;
        }

        BulkOp op = BulkOp.postShare(postId);
        buffer.add(op);
        pendingStore.addOp(op);
        Log.d("InteractionsBuffer", "sharePost enqueued for postId=" + postId + " (buffer size=" + buffer.size() + ")");
    }*/


    // --- flush implementation (supported ops in bulk, create_comment one-by-one) ---
    // --- flush implementation (supported ops in bulk, create_comment one-by-one) ---
    public void flush() {
        try {
            if (buffer.isEmpty()) {
                Log.d("InteractionsBuffer", "flush(): buffer empty");
                return;
            }

            // snapshot and remove everything we took (we'll reconstruct a deduped supported list)
            List<BulkOp> batch = new ArrayList<>(buffer);
            buffer.removeAll(batch);

            // We'll dedupe supported ops (post_reaction, comment_reaction, post_share) by target key.
            // For create_comment we still process individually in FIFO order.
            List<BulkOp> createComments = new ArrayList<>();
            // Map targetKey -> last BulkOp seen for that target (last wins)
            java.util.LinkedHashMap<String, BulkOp> lastByTarget = new java.util.LinkedHashMap<>();

            for (BulkOp op : batch) {
                if (op == null || op.op == null) continue;
                if ("create_comment".equals(op.op)) {
                    createComments.add(op);
                    continue;
                }
                if ("post_reaction".equals(op.op) || "comment_reaction".equals(op.op) || "post_share".equals(op.op)) {
                    String key;
                    if ("post_reaction".equals(op.op)) key = "post:" + (op.postId != null ? op.postId : "");
                    else if ("comment_reaction".equals(op.op)) key = "comment:" + (op.commentId != null ? op.commentId : "");
                    else /* post_share */ key = "share:" + (op.postId != null ? op.postId : "");

                    // last one wins: put will replace earlier entry
                    lastByTarget.put(key, op);
                    continue;
                }

                // unknown op -> requeue (safe fallback)
                Log.w("InteractionsBuffer", "Unknown op in batch, requeueing: " + op.op);
                buffer.add(op);
            }

            // Build final supported list from deduped map (preserves insertion order of last occurrence)
            List<BulkOp> supported = new ArrayList<>(lastByTarget.values());

            // 1) supported via bulk (deduped)
            if (!supported.isEmpty()) {
                BulkBody body = new BulkBody(supported);
                SharedPreferences prefs = appCtx.getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String accessToken = prefs.getString("accessToken", null);
                String authHeader = accessToken != null ? "Bearer " + accessToken : null;
                api.bulk(authHeader, body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful()) {
                                Log.i("InteractionsBuffer", "Bulk success (supported): HTTP " + response.code());
                                notifyBulkSuccess(supported);
                                return;
                            }

                            // Not successful -> examine status
                            int code = response.code();
                            String errBody = null;
                            try { errBody = response.errorBody() != null ? response.errorBody().string() : null; } catch (Exception ignore) {}

                            Log.e("InteractionsBuffer", "Bulk failed (supported): HTTP " + code + " body=" + errBody);

                            if (code >= 500) {
                                // server error: transient -> requeue at front for quick retry
                                requeueWithRetryLimit(supported);
                                return;
                            }

                            if (code == 429) {
                                // too many requests: backing-off strategy
                                requeueWithBackoff(supported);
                                return;
                            }

                            if (code >= 400 && code < 500) {
                                // Client error: try to parse validation info.
                                // If we can identify invalid targets, drop them and keep valid ones.
                                List<BulkOp> toKeep = new ArrayList<>();
                                List<BulkOp> toDrop = new ArrayList<>();

                                // naive parsing: look for commentId/postId strings mentioned in errBody
                                // NOTE: make this more precise depending on server error schema
                                for (BulkOp op : supported) {
                                    boolean looksInvalid = false;
                                    if (errBody != null) {
                                        if (op.commentId != null && errBody.contains(op.commentId)) looksInvalid = true;
                                        if (op.postId != null && errBody.contains(op.postId)) looksInvalid = true;
                                        // also detect parent clientId validation (UUID showing up)
                                        if ("create_comment".equals(op.op) && op.parentComment != null && errBody.contains(op.parentComment)) looksInvalid = true;
                                    }
                                    if (looksInvalid) toDrop.add(op);
                                    else toKeep.add(op);
                                }

                                if (!toKeep.isEmpty()) {
                                    // requeue valid ones for retry
                                    requeueWithRetryLimit(toKeep);
                                }

                                // For dropped ops: either persist to dead-letter store or attempt fix if possible
                                for (BulkOp bad : toDrop) {
                                    Log.w("InteractionsBuffer", "Dropping invalid op after bulk 4xx: " + bad.op + " postId=" + bad.postId + " commentId=" + bad.commentId);
                                    // move to dead-letter to allow developer inspection
                                    try { pendingStore.saveDeadLetter(bad); } catch (Exception ignore) {}
                                }

                                // If nothing kept and nothing dropped we still requeue supported (conservative)
                                if (toKeep.isEmpty() && toDrop.isEmpty()) {
                                    requeueWithRetryLimit(supported);
                                }
                                return;
                            }

                            // default fallback: requeue everything
                            requeueWithRetryLimit(supported);
                        } catch (Exception e) {
                            buffer.addAll(0, supported);
                            Log.e("InteractionsBuffer", "onResponse exception for supported bulk", e);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        requeueWithRetryLimit(supported);
                        Log.e("InteractionsBuffer", "Bulk failed (supported) network: " + t.getMessage(), t);
                    }
                });
            }

            // 2) create_comment individually (preserve order)
            if (!createComments.isEmpty()) {

                Log.d("InteractionsBuffer", "Processing create_comment ops: count=" + createComments.size());
                // Build map of confirmed clientId->serverId for this post (from PendingStore confirmed-created records)
                java.util.Map<String, String> clientToServer = new java.util.HashMap<>();
                try {
                    // We may have confirmed created stubs across posts; parse confirmed list per post as needed.
                    for (BulkOp create : createComments) {
                        if (create == null || create.postId == null) continue;
                        List<String> confirmed = pendingStore.loadConfirmedCreatedComments(create.postId);
                        if (confirmed == null) continue;
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        for (String j : confirmed) {
                            if (j == null) continue;
                            try {
                                java.util.Map map = gson.fromJson(j, java.util.Map.class);
                                String clientId = map.get("clientId") == null ? null : String.valueOf(map.get("clientId"));
                                String serverId = map.get("id") == null ? null : String.valueOf(map.get("id"));
                                if (clientId != null && serverId != null && !clientToServer.containsKey(clientId)) {
                                    clientToServer.put(clientId, serverId);
                                }
                            } catch (Exception ignore) {}
                        }
                    }
                } catch (Exception e) {
                    Log.e("InteractionsBuffer", "error building client->server map", e);
                }
                for (BulkOp create : createComments) {
                    if (create == null) continue;

                    // Helper: check if a string is probably a server id (24 hex)
                    java.util.function.Predicate<String> looksLikeServerId = s -> {
                        if (s == null) return false;
                        if (s.length() != 24) return false;
                        return s.matches("^[0-9a-fA-F]{24}$");
                    };

                    // Decide parent to send
                    String parentToSend = create.parentComment;

                    if (parentToSend != null && !parentToSend.isEmpty() && !looksLikeServerId.test(parentToSend)) {
                        // parent looks like a clientId (uuid) — try mapping
                        String mapped = clientToServer.get(parentToSend);
                        if (mapped != null) {
                            parentToSend = mapped;
                        } else {
                            // Parent not confirmed yet -> requeue this create for later (do not call API now)
                            Log.d("InteractionsBuffer", "Deferring create_comment because parent not confirmed yet: clientParent=" + parentToSend + " clientId=" + create.clientId);
                            // requeue at end of buffer so it will be retried in next flush
                            buffer.add(create);
                            continue;
                        }
                    }

                    AddCommentBody body = new AddCommentBody(create.content, parentToSend);
                    SharedPreferences prefs = appCtx.getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String accessToken = prefs.getString("accessToken", null);
                    String authHeader = accessToken != null ? "Bearer " + accessToken : null;

                    String finalParentToSend = parentToSend;
                    api.addComment(authHeader, create.postId, body).enqueue(new Callback<GenericResp<CommentResp>>() {
                        @Override
                        public void onResponse(Call<GenericResp<CommentResp>> call, Response<GenericResp<CommentResp>> res) {
                            if (res.isSuccessful() && res.body() != null && res.body().success) {
                                CommentItem created = null;
                                try {
                                    created = res.body().data != null ? res.body().data.comment : null;
                                } catch (Exception ignore) {}

                                notifyCreateCommentSuccess(create.postId, create.clientId, finalParentToSend, created);
                                Log.i("InteractionsBuffer", "create_comment confirmed clientId=" + create.clientId);
                            } else {
                                // requeue this create op
                                buffer.add(create);
                                try {
                                    String err = res.errorBody() != null ? res.errorBody().string() : "no body";
                                    Log.e("InteractionsBuffer", "create_comment failed HTTP " + res.code() + " body=" + err);
                                } catch (Exception e) {
                                    Log.e("InteractionsBuffer", "error reading create_comment error body", e);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<GenericResp<CommentResp>> call, Throwable t) {
                            buffer.add(create);
                            Log.e("InteractionsBuffer", "create_comment network failed, requeueing: " + t.getMessage());
                        }
                    });
                }
            }

        } catch (Throwable t) {
            Log.e("InteractionsBuffer", "flush() threw exception", t);
        }
    }


    private List<String> computeAffectedPostIds(List<BulkOp> batch) {
        java.util.Set<String> set = new java.util.HashSet<>();
        if (batch == null) return new ArrayList<>(set);

        // Load persisted pending list once for possible lookups
        List<BulkOp> persisted = null;
        try { persisted = pendingStore.loadAll(); } catch (Exception ignore) {}

        for (BulkOp op : batch) {
            if (op == null) continue;

            if (op.postId != null && !op.postId.isEmpty()) {
                set.add(op.postId);
                continue;
            }

            // If op has commentId but no postId, try to find a persisted op that links commentId->postId
            if (op.commentId != null && persisted != null) {
                for (BulkOp p : persisted) {
                    if (p == null) continue;
                    if ("comment_reaction".equals(p.op) && op.commentId.equals(p.commentId) && p.postId != null) {
                        set.add(p.postId);
                        break;
                    }
                    // also check create_comment entries (in case you saved mapping there)
                    if ("create_comment".equals(p.op) && p.clientId != null && p.clientId.equals(op.commentId) && p.postId != null) {
                        set.add(p.postId);
                        break;
                    }
                }
            }
        }
        return new ArrayList<>(set);
    }

    public void shutdownNow() {
        scheduler.shutdownNow();
        try {
            flush();
        } catch (Exception ignored) {
        }
    }

    private void notifyBulkSuccess(List<BulkOp> batch) {
        // Remove persisted ops by matching target (so saved entries are cleared even if object instances differ)
        try {
            if (pendingStore != null && batch != null && !batch.isEmpty()) {
                List<BulkOp> persisted = pendingStore.loadAll();
                if (persisted == null) persisted = new ArrayList<>();
                for (BulkOp op : batch) {
                    if (op == null || op.op == null) continue;
                    for (int i = persisted.size() - 1; i >= 0; --i) {
                        BulkOp p = persisted.get(i);
                        if (p == null) continue;
                        if (sameTarget(p, op) && p.op != null && p.op.equals(op.op)) {
                            persisted.remove(i);
                            // Update local storage with confirmed server state
                            updateLocalStorageAfterBulkSuccess(op);
                        }
                    }
                }
                pendingStore.saveAll(persisted);
            }
        } catch (Exception e) {
            Log.e("InteractionsBuffer", "notifyBulkSuccess: error removing persisted ops by target", e);
        }

        // Also notify UI listener of affected post ids
        if (onBulkSuccessListener != null) {
            List<String> affected = computeAffectedPostIds(batch);
            android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
            main.post(() -> onBulkSuccessListener.onBulkSuccess(affected));
        }
    }

    /**
     * This is the key method: save a small "confirmed created" JSON and notify the UI listener
     * with the server-created CommentItem so the fragment can replace the optimistic entry immediately.
     */
    private void notifyCreateCommentSuccess(String postId, String clientId, String resolvedParent, CommentItem createdItem) {
        // save a confirmed stub (so loadComments can reattach it if server list hasn't returned it yet)
        if (pendingStore != null && createdItem != null && clientId != null) {
            com.google.gson.Gson g = new com.google.gson.Gson();
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("clientId", clientId);
            m.put("id", createdItem.id);
            m.put("content", createdItem.content);
            m.put("createdAt", createdItem.createdAt);
            m.put("likes", createdItem.likes);
            m.put("dislikes", createdItem.dislikes);
            // the server returned parent id for the created comment (may be null)
            m.put("parent", createdItem.parentCommentId != null ? createdItem.parentCommentId : null);
            m.put("resolvedParent", resolvedParent != null ? resolvedParent : null);

            String createdJson = g.toJson(m);
            pendingStore.saveConfirmedCreatedComment(postId, clientId, createdJson);
        }

        // remove pending create op (we confirmed it)
        if (pendingStore != null) pendingStore.removeByClientId(clientId);

        // Notify UI listener in main thread
        if (onBulkSuccessListener != null) {
            android.os.Handler main = new android.os.Handler(android.os.Looper.getMainLooper());
            main.post(() -> {
                // 1) first call onCreateCommentConfirmed so fragment can update optimistic UI immediately
                try {
                    onBulkSuccessListener.onCreateCommentConfirmed(postId, clientId, createdItem);
                } catch (Exception ignore) {
                }
                // 2) also notify onBulkSuccess (so components that refresh comments know a post changed)
                try {
                    onBulkSuccessListener.onBulkSuccess(java.util.Collections.singletonList(postId));
                } catch (Exception ignore) {
                }
            });
        }
    }

    // Force a flush immediately on current thread
    public void flushNow() {
        try {
            flush();
        } catch (Throwable t) {
            Log.e("InteractionsBuffer", "flushNow failed", t);
        }
    }

    // --- helper utilities ---

    /**
     * Find an existing reaction op in the in-memory buffer for the given target.
     * opKind: "post_reaction" or "comment_reaction"
     * id: postId or commentId
     */
    private BulkOp findExistingReactionOp(String opKind, String id) {
        if (id == null) return null;
        for (BulkOp b : buffer) {
            if (b == null || b.op == null) continue;
            if (!opKind.equals(b.op)) continue;
            if ("post_reaction".equals(opKind) && id.equals(b.postId)) return b;
            if ("comment_reaction".equals(opKind) && id.equals(b.commentId)) return b;
        }
        return null;
    }

    /** Simple field-based matching of two BulkOps (same target). */
    private boolean sameTarget(BulkOp a, BulkOp b) {
        if (a == null || b == null) return false;
        if (a.op == null || b.op == null) return false;
        if (a.op.equals("post_reaction") && b.op.equals("post_reaction")) {
            return a.postId != null && a.postId.equals(b.postId);
        }
        if (a.op.equals("comment_reaction") && b.op.equals("comment_reaction")) {
            return a.commentId != null && a.commentId.equals(b.commentId);
        }
        if (a.op.equals("create_comment") && b.op.equals("create_comment")) {
            return a.clientId != null && a.clientId.equals(b.clientId);
        }
        if (a.op.equals("post_share") && b.op.equals("post_share")) {
            return a.postId != null && a.postId.equals(b.postId);
        }
        return false;
    }

    /** Remove a specific BulkOp from both in-memory buffer and PendingStore. */
    private void removeOpFromBufferAndPending(BulkOp op) {
        if (op == null) return;
        try {
            // remove from in-memory buffer
            for (int i = buffer.size() - 1; i >= 0; --i) {
                BulkOp b = buffer.get(i);
                if (b != null && sameTarget(b, op)) {
                    buffer.remove(b);
                }
            }

            // remove matching targets from PendingStore
            List<BulkOp> list = pendingStore.loadAll();
            if (list != null) {
                for (int i = list.size() - 1; i >= 0; --i) {
                    BulkOp p = list.get(i);
                    if (p == null) continue;
                    if (sameTarget(p, op)) {
                        list.remove(i);
                    }
                }
                pendingStore.saveAll(list);
            }
        } catch (Exception e) {
            Log.e("InteractionsBuffer", "removeOpFromBufferAndPending error", e);
        }
    }

    /**
     * Replace an existing op (in-memory + pending store) with a new op.
     * If existing is null, this simply adds newOp.
     */
    private void replaceOrAddOp(BulkOp existing, BulkOp newOp) {
        if (newOp == null) return;
        try {
            // 1) remove ANY matching target from in-memory buffer (both like/dislike)
            for (int i = buffer.size() - 1; i >= 0; --i) {
                BulkOp b = buffer.get(i);
                if (b != null && sameTarget(b, newOp)) {
                    buffer.remove(b);
                }
            }
            // 2) add newOp to in-memory buffer
            buffer.add(newOp);

            // 3) update persisted store: remove any matching target ops, then append newOp
            List<BulkOp> list = pendingStore.loadAll();
            if (list == null) list = new ArrayList<>();
            for (int i = list.size() - 1; i >= 0; --i) {
                BulkOp p = list.get(i);
                if (p != null && sameTarget(p, newOp)) {
                    list.remove(i);
                }
            }
            list.add(newOp);
            pendingStore.saveAll(list);
        } catch (Exception e) {
            Log.e("InteractionsBuffer", "replaceOrAddOp error", e);
            // fallback: ensure at least we add newOp once
            try {
                buffer.add(newOp);
                pendingStore.addOp(newOp);
            } catch (Exception ignore) {}
        }
    }

    /**
     * Debug helper: log in-memory buffer and pending store contents.
     * Call from DEV button to inspect state.
     */
    public void debugDumpBuffer() {
//        Log.d("InteractionsBuffer", "==== DEBUG DUMP START ====");
        for (BulkOp op : buffer) {
//            Log.d("InteractionsBuffer", "In-memory: op=" + op.op + " type=" + op.type +
//                    " postId=" + op.postId + " commentId=" + op.commentId + " clientId=" + op.clientId);
        }
        try {
            List<BulkOp> persisted = pendingStore.loadAll();
            for (BulkOp op : persisted) {
//                Log.d("InteractionsBuffer", "PendingStore: op=" + op.op + " type=" + op.type +
//                        " postId=" + op.postId + " commentId=" + op.commentId + " clientId=" + op.clientId);
            }
        } catch (Exception e) {
//            Log.e("InteractionsBuffer", "Error dumping pendingStore", e);
        }
//        Log.d("InteractionsBuffer", "==== DEBUG DUMP END ====");
    }

    /**
     * Return true if a create_comment with this clientId is present in memory buffer
     * or persisted pending store.
     */
    private boolean createCommentExists(String clientId) {
        if (clientId == null) return false;
        // check in-memory
        for (BulkOp b : buffer) {
            if ("create_comment".equals(b.op) && clientId.equals(b.clientId)) return true;
        }
        // check persisted pending store
        try {
            List<BulkOp> persisted = pendingStore.loadAll();
            if (persisted != null) {
                for (BulkOp p : persisted) {
                    if (p != null && "create_comment".equals(p.op) && clientId.equals(p.clientId)) return true;
                }
            }
        } catch (Exception e) {
            Log.e("InteractionsBuffer", "createCommentExists check failed", e);
        }
        return false;
    }

    /**
     * Return true if a post_share for this postId exists in buffer or persisted store.
     */
    private boolean postShareExists(String postId) {
        if (postId == null) return false;
        // in-memory
        for (BulkOp b : buffer) {
            if ("post_share".equals(b.op) && postId.equals(b.postId)) return true;
        }
        // persisted
        try {
            List<BulkOp> persisted = pendingStore.loadAll();
            if (persisted != null) {
                for (BulkOp p : persisted) {
                    if (p != null && "post_share".equals(p.op) && postId.equals(p.postId)) return true;
                }
            }
        } catch (Exception e) {
            Log.e("InteractionsBuffer", "postShareExists check failed", e);
        }
        return false;
    }
    /**
     * Find an existing reaction op either in in-memory buffer OR in persisted PendingStore.
     * opKind: "post_reaction" or "comment_reaction"
     * id: postId or commentId
     *
     * Returns the first matching BulkOp found (preference: in-memory buffer then persisted store),
     * or null if none found.
     */
    private BulkOp findExistingReactionOpGlobal(String opKind, String id) {
        if (id == null) return null;
        // 1) check in-memory buffer
        for (BulkOp b : buffer) {
            if (b == null || b.op == null) continue;
            if (!opKind.equals(b.op)) continue;
            if ("post_reaction".equals(opKind) && id.equals(b.postId)) return b;
            if ("comment_reaction".equals(opKind) && id.equals(b.commentId)) return b;
        }
        // 2) check persisted pending store
        try {
            List<BulkOp> persisted = pendingStore.loadAll();
            if (persisted != null) {
                for (int i = persisted.size() - 1; i >= 0; --i) { // check latest-first
                    BulkOp p = persisted.get(i);
                    if (p == null || p.op == null) continue;
                    if (!opKind.equals(p.op)) continue;
                    if ("post_reaction".equals(opKind) && id.equals(p.postId)) return p;
                    if ("comment_reaction".equals(opKind) && id.equals(p.commentId)) return p;
                }
            }
        } catch (Exception e) {
            Log.e("InteractionsBuffer", "findExistingReactionOpGlobal: failed reading pending store", e);
        }
        return null;
    }
    // --- helper: exact-match check (in-memory or persisted) ---
    private boolean hasPendingReaction(String opKind, String id, String type) {
        if (id == null || type == null) return false;
        // in-memory
        for (BulkOp b : buffer) {
            if (b == null || b.op == null) continue;
            if (!opKind.equals(b.op)) continue;
            if ("post_reaction".equals(opKind) && id.equals(b.postId) && type.equals(b.type)) return true;
            if ("comment_reaction".equals(opKind) && id.equals(b.commentId) && type.equals(b.type)) return true;
        }
        // persisted
        try {
            List<BulkOp> persisted = pendingStore.loadAll();
            if (persisted != null) {
                for (int i = persisted.size() - 1; i >= 0; --i) {
                    BulkOp p = persisted.get(i);
                    if (p == null || p.op == null) continue;
                    if (!opKind.equals(p.op)) continue;
                    if ("post_reaction".equals(opKind) && id.equals(p.postId) && type.equals(p.type)) return true;
                    if ("comment_reaction".equals(opKind) && id.equals(p.commentId) && type.equals(p.type)) return true;
                }
            }
        } catch (Exception e) {
            Log.e("InteractionsBuffer", "hasPendingReaction: error reading pending store", e);
        }
        return false;
    }

    // --- helper: find any existing reaction op for given target (any type) ---
    private BulkOp findAnyExistingReaction(String opKind, String id) {
        if (id == null) return null;
        // prefer latest in-memory match
        for (BulkOp b : buffer) {
            if (b == null || b.op == null) continue;
            if (!opKind.equals(b.op)) continue;
            if ("post_reaction".equals(opKind) && id.equals(b.postId)) return b;
            if ("comment_reaction".equals(opKind) && id.equals(b.commentId)) return b;
        }
        // fallback to persisted (latest-first)
        try {
            List<BulkOp> persisted = pendingStore.loadAll();
            if (persisted != null) {
                for (int i = persisted.size() - 1; i >= 0; --i) {
                    BulkOp p = persisted.get(i);
                    if (p == null || p.op == null) continue;
                    if (!opKind.equals(p.op)) continue;
                    if ("post_reaction".equals(opKind) && id.equals(p.postId)) return p;
                    if ("comment_reaction".equals(opKind) && id.equals(p.commentId)) return p;
                }
            }
        } catch (Exception e) {
            Log.e("InteractionsBuffer", "findAnyExistingReaction: error reading pending store", e);
        }
        return null;
    }
    private void updateLocalStorageAfterBulkSuccess(BulkOp op) {
        SharedPreferences prefs = appCtx.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if ("post_reaction".equals(op.op)) {
            String key = "post_" + op.postId;
            if ("like".equals(op.type)) {
                editor.putBoolean(key + "_liked", true);
                editor.putBoolean(key + "_disliked", false);
            } else if ("dislike".equals(op.type)) {
                editor.putBoolean(key + "_liked", false);
                editor.putBoolean(key + "_disliked", true);
            }
        } else if ("comment_reaction".equals(op.op)) {
            String key = "comment_" + op.commentId;
            if ("like".equals(op.type)) {
                editor.putBoolean(key + "_liked", true);
                editor.putBoolean(key + "_disliked", false);
            } else if ("dislike".equals(op.type)) {
                editor.putBoolean(key + "_liked", false);
                editor.putBoolean(key + "_disliked", true);
            }
        }
        editor.apply();
    }
    // helper: requeue but enforce retry limit and dead-letter
    private void requeueWithRetryLimit(List<BulkOp> ops) {
        try {
            List<BulkOp> persisted = pendingStore.loadAll();
            if (persisted == null) persisted = new ArrayList<>();

            for (BulkOp op : ops) {
                if (op == null) continue;
                op.retryCount = op.retryCount + 1;
                if (op.retryCount > 5) {
                    // move to dead-letter
                    try { pendingStore.saveDeadLetter(op); } catch (Exception ignore) {}
                    Log.e("InteractionsBuffer", "Moving op to dead-letter after retries: " + op.op + " id:" + (op.postId!=null?op.postId:op.commentId));
                    // also remove matching persisted copies (if any)
                    for (int i = persisted.size()-1; i>=0; --i) {
                        if (sameTarget(persisted.get(i), op) && persisted.get(i).op.equals(op.op)) persisted.remove(i);
                    }
                } else {
                    // ensure persisted list has this op (replace existing)
                    for (int i = persisted.size()-1; i>=0; --i) {
                        BulkOp p = persisted.get(i);
                        if (sameTarget(p, op) && p.op.equals(op.op)) { persisted.remove(i); break; }
                    }
                    persisted.add(0, op); // add near front so it's retried early
                    // add to in-memory buffer front for immediate retry attempts
                    buffer.add(0, op);
                }
            }
            pendingStore.saveAll(persisted);
        } catch (Exception e) {
            // last resort: put ops back in-memory
            buffer.addAll(0, ops);
            Log.e("InteractionsBuffer", "requeueWithRetryLimit failed, re-added to buffer", e);
        }
    }

    private void requeueWithBackoff(List<BulkOp> ops) {
        // Simple approach: re-add to buffer and let scheduler handle next flush.
        // For more robust backoff you can track nextAttemptTs per op.
        buffer.addAll(ops);
    }
}
