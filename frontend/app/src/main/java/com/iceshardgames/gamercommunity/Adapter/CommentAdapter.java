package com.iceshardgames.gamercommunity.Adapter;

import static android.content.Context.MODE_PRIVATE;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.BulkOp;
import com.iceshardgames.gamercommunity.CommentItem;
import com.iceshardgames.gamercommunity.CommentsPage;
import com.iceshardgames.gamercommunity.GenericResp;
import com.iceshardgames.gamercommunity.InteractionsBuffer;
import com.iceshardgames.gamercommunity.Model.Comment;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.PendingStore;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CommentAdapter that:
 *  - keeps replies hidden by default
 *  - shows replies only when user toggles them (or when we add a reply)
 *  - checks PendingStore off the UI thread for persisted optimistic replies
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList;
    private final Context context;
    private final InteractionsBuffer buffer;
    private final ApiService api;
    private final String postId;
    private long mLastClickTime = 0;

    // Executor reused for PendingStore checks
    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // set of expanded keys (serverId or clientId or fallback position string)
    // set of expanded keys (serverId or clientId or fallback position string)
    private final Set<String> expandedKeys = new HashSet<>();

    // track replies that are currently being loaded (by serverId)
    private final Set<String> repliesLoading = Collections.synchronizedSet(new HashSet<>());

    // small placeholder text while loading (optional)
    private static final String REPLIES_LOADING_PLACEHOLDER = "Loading replies…";


    public CommentAdapter(List<Comment> commentList, Context context, InteractionsBuffer buffer, ApiService api, String postId) {
        this.commentList = commentList;
        this.context = context;
        this.buffer = buffer;
        this.api = api;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
// Load saved reaction state
        // stable key for prefs: prefer serverId then clientId then position fallback
        final String key = comment.getServerId() != null ? comment.getServerId()
                : (comment.getClientId() != null ? comment.getClientId() : "pos:" + position);

        // Basic bind using model (server counts are preferred)
        holder.tvUser.setText(comment.getUser());
        holder.tvTime.setText("· " + comment.getTime());
        holder.tvText.setText(comment.getText());

        // We'll compute displayed counts from server-model unless prefs explicitly contain counts
        int displayLikes = comment.getLikeCount();
        int displayDislikes = comment.getDislikeCount();

        // Read persisted comment reaction (safely) into local vars (do not overwrite comment model)
        boolean savedLiked = false;
        boolean savedDisliked = false;
        boolean hasSavedKeys = false;
        try {
            SharedPreferences prefs = context.getSharedPreferences("CommentReactions", MODE_PRIVATE);
            String prefKey = "comment_" + key;
            hasSavedKeys = prefs.contains(prefKey + "_liked") || prefs.contains(prefKey + "_disliked");
            savedLiked = prefs.getBoolean(prefKey + "_liked", false);
            savedDisliked = prefs.getBoolean(prefKey + "_disliked", false);

            // only override displayed counts if prefs explicitly saved counts
            if (prefs.contains(prefKey + "_likes")) {
                displayLikes = prefs.getInt(prefKey + "_likes", displayLikes);
            }
            if (prefs.contains(prefKey + "_dislikes")) {
                displayDislikes = prefs.getInt(prefKey + "_dislikes", displayDislikes);
            }
        } catch (Exception e) {
            Log.w("CommentAdapter", "error reading saved comment reaction", e);
        }

        // Set counts on UI (use display values)
        holder.tvLikeCount.setText(String.valueOf(Math.max(0, displayLikes)));
        holder.tvDislikeCount.setText(String.valueOf(Math.max(0, displayDislikes)));

        // Decide user-level flags (priority: in-memory optimistic flags -> persisted booleans)
        boolean userLiked = comment.isLiked();
        boolean userDisliked = comment.isDisliked();
        if (!userLiked && !userDisliked && hasSavedKeys) {
            userLiked = savedLiked;
            userDisliked = savedDisliked;
        }

        // HYBRID rule (independent):
        // show like filled if userLiked OR (server/display likes > 0)
        boolean likeFill = userLiked || displayLikes > 0;
        boolean dislikeFill = userDisliked || displayDislikes > 0;

      /*  // If both would be filled because counts>0 but user reacted one-way, prefer user reaction
        if (userLiked && !userDisliked) dislikeFill = false;
        if (userDisliked && !userLiked) likeFill = false;*/

        // Apply drawables robustly
        try {
            int likeRes = likeFill ? R.drawable.ic_like_filled : R.drawable.ic_like_outline;
            int dislikeRes = dislikeFill ? R.drawable.ic_dislike_filled : R.drawable.ic_dislike_outline;
            holder.imgLike.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(context, likeRes));
            holder.imgLike.invalidate();
            holder.imgLike.post(() -> holder.imgLike.refreshDrawableState());

            holder.imgDislike.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(context, dislikeRes));
            holder.imgDislike.invalidate();
            holder.imgDislike.post(() -> holder.imgDislike.refreshDrawableState());
        } catch (Exception e) {
            Log.w("CommentAdapter", "failed to set comment icons", e);
        }

        // --- click handlers: update model optimistically, persist, enqueue, then rebind via notifyItemChanged ---

        holder.layoutLike.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            // Optimistic model update (mutate model so future logic sees correct in-memory flags)
            if (comment.isLiked()) {
                comment.setLiked(false);
                comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            } else {
                comment.setLiked(true);
                comment.setLikeCount(comment.getLikeCount() + 1);
                if (comment.isDisliked()) {
                    comment.setDisliked(false);
                    comment.setDislikeCount(Math.max(0, comment.getDislikeCount() - 1));
                }
            }

            // Persist & enqueue
            String target = comment.getServerId() != null ? comment.getServerId() : comment.getClientId();
            if (target != null) {
                saveCommentReactionState(target, comment.isLiked(), comment.isDisliked(),
                        comment.getLikeCount(), comment.getDislikeCount());
                buffer.likeComment(target, postId);
            } else {
                Log.d("InteractionsBuffer", "likeComment: no target id for optimistic comment");
            }

            // Let the authoritative onBind recompute icon/counter
            notifyItemChanged(pos);
        });

        holder.layoutDislike.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            if (comment.isDisliked()) {
                comment.setDisliked(false);
                comment.setDislikeCount(Math.max(0, comment.getDislikeCount() - 1));
            } else {
                comment.setDisliked(true);
                comment.setDislikeCount(comment.getDislikeCount() + 1);
                if (comment.isLiked()) {
                    comment.setLiked(false);
                    comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                }
            }

            String target = comment.getServerId() != null ? comment.getServerId() : comment.getClientId();
            if (target != null) {
                saveCommentReactionState(target, comment.isLiked(), comment.isDisliked(),
                        comment.getLikeCount(), comment.getDislikeCount());
                buffer.dislikeComment(target, postId);
            } else {
                Log.d("InteractionsBuffer", "dislikeComment: no target id for optimistic comment");
            }

            notifyItemChanged(pos);
        });

        // ===== replies container handling =====
        // Clear and populate repliesContainer but keep it hidden unless expandedKeys contains this key
        holder.repliesContainer.removeAllViews();

        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            // create views for replies but DO NOT show them automatically
            for (Comment reply : comment.getReplies()) {
                View replyView = LayoutInflater.from(context).inflate(R.layout.reply_item, holder.repliesContainer, false);
                TextView tvReplyUser = replyView.findViewById(R.id.tvReplyUser);
                TextView tvReplyTime = replyView.findViewById(R.id.tvReplyTime);
                TextView tvReplyText = replyView.findViewById(R.id.tvReplyText);

                tvReplyUser.setText("@" + reply.getUser());
                tvReplyTime.setText("· " + reply.getTime());
                tvReplyText.setText(reply.getText());

                holder.repliesContainer.addView(replyView);
            }
        }

        // Determine visibility based on expansion state
        if (expandedKeys.contains(key)) {
            holder.repliesContainer.setVisibility(View.VISIBLE);
            holder.layoutReplyBox.setVisibility(View.VISIBLE);
        } else {
            holder.repliesContainer.setVisibility(View.GONE);
        }

        // reply input always hidden initially
//        holder.layoutReplyBox.setVisibility(View.GONE);

        // ===== btnReply click behavior =====
        // ===== btnReply click behavior =====
        holder.btnReply.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            // a stable key for this comment row (prefer serverId, then clientId, else position)
            final String keyLocal = key;

            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                if (expandedKeys.contains(keyLocal)) {
                    expandedKeys.remove(keyLocal);
                } else {
                    expandedKeys.add(keyLocal);
                }
                notifyItemChanged(pos);
                holder.layoutReplyBox.setVisibility(View.VISIBLE);
                return;
            }

            // No in-memory replies -> check PendingStore (off UI thread) and maybe fetch server replies
            backgroundExecutor.execute(() -> {
                boolean pendingRepliesExist = false;
                try {
                    List<BulkOp> pending = PendingStore.get(context).loadAll();
                    if (pending != null) {
                        for (BulkOp op : pending) {
                            if (op == null) continue;
                            if (!"create_comment".equals(op.op)) continue;
                            if (op.postId == null || !op.postId.equals(postId)) continue;
                            String parentId = op.parentComment;
                            if (parentId == null) continue;
                            // match parent by serverId OR clientId
                            if ((comment.getServerId() != null && comment.getServerId().equals(parentId)) ||
                                    (comment.getClientId() != null && comment.getClientId().equals(parentId))) {
                                pendingRepliesExist = true;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("CommentAdapter", "Error checking pending replies", e);
                }

                final boolean hasPending = pendingRepliesExist;
                mainHandler.post(() -> {
                    if (hasPending) {
                        // expand and show placeholder (actual optimistic replies will be merged by loadComments)
                        expandedKeys.add(keyLocal);
                        notifyItemChanged(pos);
                        holder.layoutReplyBox.setVisibility(View.VISIBLE);
                        return;
                    }

                    // No local pending replies -- attempt to lazy-load server replies if we have serverId
                    if (comment.getServerId() != null && !repliesLoading.contains(comment.getServerId())) {
                        // mark loading and show immediate placeholder
                        repliesLoading.add(comment.getServerId());
                        holder.repliesContainer.removeAllViews();
                        TextView loading = new TextView(context);
                        loading.setText(REPLIES_LOADING_PLACEHOLDER);
                        holder.repliesContainer.addView(loading);
                        holder.repliesContainer.setVisibility(View.VISIBLE);
                        holder.layoutReplyBox.setVisibility(View.VISIBLE);

                        // fire network request to fetch replies
                        fetchRepliesForComment(comment, keyLocal, pos, holder);
                    } else {
                        // fallback: just show reply input box (no server replies available)
                        if (holder.layoutReplyBox.getVisibility() == View.VISIBLE) {
                            holder.layoutReplyBox.setVisibility(View.GONE);
                        } else {
                            holder.layoutReplyBox.setVisibility(View.VISIBLE);
                            holder.repliesContainer.setVisibility(View.GONE);
                            holder.etReply.requestFocus();
                        }
                    }
                });
            });
        });

        // Send reply (optimistic + enqueue)
        holder.btnSendReply.setOnClickListener(v -> {
            String replyText = holder.etReply.getText().toString().trim();
            if (replyText.isEmpty()) return;
            holder.etReply.setText("");
            holder.layoutReplyBox.setVisibility(View.GONE);

            // create client id for optimistic reply
            String clientId = java.util.UUID.randomUUID().toString();

            // decide parent id: prefer serverId, fallback to client's own clientId (if parent itself is optimistic)
            String parentIdForOp = comment.getServerId() != null ? comment.getServerId() : comment.getClientId();

            // optimistic reply shown immediately, mark with clientId and parentId (could be serverId or clientId)
            Comment reply = new Comment(getDisplayNameFromPrefs(), replyText, "Now");
            reply.setClientId(clientId);
            reply.setParentServerId(parentIdForOp); // store the parent reference (server OR client)

            // add optimistic reply to parent comment model
            comment.addReply(reply);

            // ensure the comment's replies are expanded so user sees their reply
            expandedKeys.add(key);

            // rebind this item to show the new reply
            notifyItemChanged(position);

            // enqueue create_comment with parent id (serverId or clientId)
            buffer.enqueueCreateComment(postId, replyText, parentIdForOp, clientId);
        });

        // Share click
        holder.layoutShare.setOnClickListener(v -> {
            if (System.currentTimeMillis() - mLastClickTime < 1000) return;
            mLastClickTime = System.currentTimeMillis();

            // friendly snippet (max 200 chars)
            String commentText = comment.getText() != null ? comment.getText() : "";
            String snippet = commentText.length() > 200 ? commentText.substring(0, 200).trim() + "…" : commentText;

            String timeLabel = comment.getTime() != null ? comment.getTime() : "just now";
            String subject = "Comment by @" + comment.getUser();

            // construct a best-effort URL: serverId (if available) else post page
            String commentUrl = "https://iceshardgames.com/posts/" + postId;
            if (comment.getServerId() != null && !comment.getServerId().isEmpty()) {
                commentUrl += "#comment-" + comment.getServerId();
            } else if (comment.getClientId() != null && !comment.getClientId().isEmpty()) {
                commentUrl += "#pending-" + comment.getClientId();
            }

            String shareText = new StringBuilder()
                    .append("Comment by @").append(comment.getUser()).append(" · ").append(timeLabel).append("\n\n")
                    .append(snippet).append("\n\n")
                    .append("View discussion: ").append(commentUrl).append("\n\n")
                    .append("Shared via IceShard Games Community")
                    .toString();

            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, subject);
            send.putExtra(Intent.EXTRA_TEXT, shareText);

            Intent callbackIntent = new Intent("com.iceshardgames.gamercommunity.SHARE_CHOSEN");
            callbackIntent.putExtra("share_post_id", postId);
            if (comment.getServerId() != null) callbackIntent.putExtra("share_comment_id", comment.getServerId());
            else if (comment.getClientId() != null) callbackIntent.putExtra("share_comment_id", comment.getClientId());
            callbackIntent.putExtra("share_text_preview", snippet);
            callbackIntent.setPackage(context.getPackageName());

            int requestCode = (int) (System.currentTimeMillis() & 0xfffffff);
            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Intent chooser = Intent.createChooser(send, "Share Comment", pi.getIntentSender());
            try {
                context.startActivity(chooser);
            } catch (Exception e) {
                // fallback chooser
                context.startActivity(Intent.createChooser(send, "Share Comment"));
            }
        });

    }

    private String getDisplayNameFromPrefs() {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("username", "You");
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvTime, tvText, tvLikeCount, tvDislikeCount, btnSendReply;
        ImageView imgLike, imgDislike;
        LinearLayout layoutLike, layoutDislike, layoutShare, layoutReplyBox, repliesContainer, btnReply;
        EditText etReply;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvCommentUser);
            tvTime = itemView.findViewById(R.id.tvCommentTime);
            tvText = itemView.findViewById(R.id.tvCommentText);
            tvLikeCount = itemView.findViewById(R.id.tvCommentLikeCount);
            tvDislikeCount = itemView.findViewById(R.id.tvCommentDislikeCount);

            layoutLike = itemView.findViewById(R.id.layoutCommentLike);
            layoutDislike = itemView.findViewById(R.id.layoutCommentDislike);
            layoutShare = itemView.findViewById(R.id.layoutcomShare);

            imgLike = itemView.findViewById(R.id.imgCommentLike);
            imgDislike = itemView.findViewById(R.id.imgCommentDislike);

            btnReply = itemView.findViewById(R.id.layoutcomReply);
            layoutReplyBox = itemView.findViewById(R.id.layoutReplyBox);
            etReply = itemView.findViewById(R.id.etReply);
            btnSendReply = itemView.findViewById(R.id.btnSendReply);

            repliesContainer = new LinearLayout(itemView.getContext());
            repliesContainer.setOrientation(LinearLayout.VERTICAL);
            ((LinearLayout) itemView).addView(repliesContainer);
        }
    }

    // Add this method to save comment reaction state
    // Replace existing method with this safer version
    private void saveCommentReactionState(String commentId, boolean liked, boolean disliked, int likeCount, int dislikeCount) {
        if (commentId == null) return;
        SharedPreferences prefs = context.getSharedPreferences("CommentReactions", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String key = "comment_" + commentId;

        editor.putBoolean(key + "_liked", liked);
        editor.putBoolean(key + "_disliked", disliked);

        // Save counts only when safe: prefer not to store spurious zeros that may be stale
        if (likeCount >= 0) editor.putInt(key + "_likes", likeCount);
        if (dislikeCount >= 0) editor.putInt(key + "_dislikes", dislikeCount);

        editor.apply();
    }

    // Add this method to load comment reaction state
    // Replace existing method with this improved version
    private void loadCommentReactionState(Comment comment) {
        String commentId = comment.getServerId() != null ? comment.getServerId() : comment.getClientId();
        if (commentId == null) return;

        SharedPreferences prefs = context.getSharedPreferences("CommentReactions", MODE_PRIVATE);
        String key = "comment_" + commentId;

        // load booleans (defaults to false if absent)
        comment.setLiked(prefs.getBoolean(key + "_liked", false));
        comment.setDisliked(prefs.getBoolean(key + "_disliked", false));

        // Only override counts if they were explicitly saved (avoid clobbering server values)
        if (prefs.contains(key + "_likes")) {
            comment.setLikeCount(prefs.getInt(key + "_likes", comment.getLikeCount()));
        }
        if (prefs.contains(key + "_dislikes")) {
            comment.setDislikeCount(prefs.getInt(key + "_dislikes", comment.getDislikeCount()));
        }
    }



    /**
     * Fetch replies for a comment from server (parentCommentId = comment.serverId).
     * On success, attach replies to the comment model, load saved reaction state for each,
     * and notify the adapter to rebind the item.
     */
    /**
     * Fetch replies for a comment from server (parentCommentId = comment.serverId).
     * On success, attach replies to the comment model using comment.addReply(...),
     * load saved reaction state for each reply, and refresh the item UI.
     */
    private void fetchRepliesForComment(final Comment comment, final String key, final int position, final CommentViewHolder holder) {
        final String parentId = comment.getServerId();
        if (parentId == null) {
            repliesLoading.remove(parentId);
            return;
        }

        // get token from prefs (adapter doesn't store accessToken)
        String token = context.getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("accessToken", "");

        api.listComments("Bearer " + token, postId, parentId, 1, 50).enqueue(new Callback<GenericResp<CommentsPage>>() {
            @Override
            public void onResponse(Call<GenericResp<CommentsPage>> call, Response<GenericResp<CommentsPage>> res) {
                repliesLoading.remove(parentId);
                if (!res.isSuccessful() || res.body() == null || !res.body().success) {
                    // failed -> remove loading placeholder and show reply input
                    mainHandler.post(() -> {
                        holder.repliesContainer.removeAllViews();
                        holder.repliesContainer.setVisibility(View.GONE);
                        holder.layoutReplyBox.setVisibility(View.VISIBLE);
                        notifyItemChanged(position);
                    });
                    return;
                }

                List<CommentItem> serverReplies = res.body().data.comments != null ? res.body().data.comments : new ArrayList<>();

                // clear any existing replies (we expect it empty) and add fetched ones
                try {
                    comment.getReplies().clear();
                } catch (Exception ignore) { }

                for (CommentItem it : serverReplies) {
                    long createdMillis = 0;
                    try { createdMillis = Utills.parseIso8601ToMillis(it.createdAt); } catch (Exception ignore) {}
                    Comment r = new Comment(getDisplayNameFromPrefs(), it.content, Utills.getTimeAgo(createdMillis));
                    r.setLikeCount(it.likes);
                    r.setDislikeCount(it.dislikes);
                    r.setServerId(it.id);
                    // load saved comment reaction state (from SharedPreferences)
                    loadCommentReactionState(r);
                    comment.addReply(r);
                }

                // ensure expanded so UI shows replies
                expandedKeys.add(key);

                // update UI on main thread (populate repliesContainer immediately)
                mainHandler.post(() -> {
                    holder.repliesContainer.removeAllViews();
                    for (Comment reply : comment.getReplies()) {
                        View replyView = LayoutInflater.from(context).inflate(R.layout.reply_item, holder.repliesContainer, false);
                        TextView tvReplyUser = replyView.findViewById(R.id.tvReplyUser);
                        TextView tvReplyTime = replyView.findViewById(R.id.tvReplyTime);
                        TextView tvReplyText = replyView.findViewById(R.id.tvReplyText);

                        tvReplyUser.setText("@" + reply.getUser());
                        tvReplyTime.setText("· " + reply.getTime());
                        tvReplyText.setText(reply.getText());

                        holder.repliesContainer.addView(replyView);
                    }
                    holder.repliesContainer.setVisibility(View.VISIBLE);
                    holder.layoutReplyBox.setVisibility(View.VISIBLE);
                    notifyItemChanged(position);
                });
            }

            @Override
            public void onFailure(Call<GenericResp<CommentsPage>> call, Throwable t) {
                repliesLoading.remove(parentId);
                Log.e("CommentAdapter", "Failed to fetch replies: " + t.getMessage());
                mainHandler.post(() -> {
                    holder.repliesContainer.removeAllViews();
                    holder.repliesContainer.setVisibility(View.GONE);
                    holder.layoutReplyBox.setVisibility(View.VISIBLE);
                    notifyItemChanged(position);
                });
            }
        });
    }



}
