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
    public void likeComment(String commentId) {
        if (commentId == null) return;

        BulkOp existing = findAnyExistingReaction("comment_reaction", commentId);

        if (existing == null) {
            BulkOp op = BulkOp.commentReaction(commentId, "like");
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
            BulkOp newOp = BulkOp.commentReaction(commentId, "like");
            replaceOrAddOp(existing, newOp);
        }
    }

    public void dislikeComment(String commentId) {
        if (commentId == null) return;

        BulkOp existing = findAnyExistingReaction("comment_reaction", commentId);

        if (existing == null) {
            BulkOp op = BulkOp.commentReaction(commentId, "dislike");
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
            BulkOp newOp = BulkOp.commentReaction(commentId, "dislike");
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
                            if (!response.isSuccessful()) {
                                // on failure, requeue the supported ops at front so they are retried
                                buffer.addAll(0, supported);
                                String err = response.errorBody() != null ? response.errorBody().toString() : "no body";
                                Log.e("InteractionsBuffer", "Bulk failed (supported): HTTP " + response.code() + " body=" + err);
                            } else {
                                Log.i("InteractionsBuffer", "Bulk success (supported): HTTP " + response.code());
                                notifyBulkSuccess(supported);
                            }
                        } catch (Exception e) {
                            buffer.addAll(0, supported);
                            Log.e("InteractionsBuffer", "onResponse exception for supported bulk", e);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        buffer.addAll(0, supported);
                        Log.e("InteractionsBuffer", "Bulk failed (supported) network: " + t.getMessage(), t);
                    }
                });
            }

            // 2) create_comment individually (preserve order)
            if (!createComments.isEmpty()) {
                Log.d("InteractionsBuffer", "Processing create_comment ops: count=" + createComments.size());
                for (BulkOp create : createComments) {
                    AddCommentBody body = new AddCommentBody(create.content, create.parentComment);
                    SharedPreferences prefs = appCtx.getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String accessToken = prefs.getString("accessToken", null);
                    String authHeader = accessToken != null ? "Bearer " + accessToken : null;

                    api.addComment(authHeader, create.postId, body).enqueue(new Callback<GenericResp<CommentResp>>() {
                        @Override
                        public void onResponse(Call<GenericResp<CommentResp>> call, Response<GenericResp<CommentResp>> res) {
                            if (res.isSuccessful() && res.body() != null && res.body().success) {
                                CommentItem created = null;
                                try {
                                    created = res.body().data != null ? res.body().data.comment : null;
                                } catch (Exception ignore) {}

                                notifyCreateCommentSuccess(create.postId, create.clientId, created);
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
        for (BulkOp op : batch) {
            if (op.postId != null) set.add(op.postId);
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
    private void notifyCreateCommentSuccess(String postId, String clientId, CommentItem createdItem) {
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

}
