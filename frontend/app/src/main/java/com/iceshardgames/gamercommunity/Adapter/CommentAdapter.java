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
import com.iceshardgames.gamercommunity.InteractionsBuffer;
import com.iceshardgames.gamercommunity.Model.Comment;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.PendingStore;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    private final Set<String> expandedKeys = new HashSet<>();

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

        // a stable key for this comment row (prefer serverId, then clientId, else position)
        final String key = comment.getServerId() != null ? comment.getServerId()
                : (comment.getClientId() != null ? comment.getClientId() : "pos:" + position);

        // basic bind
        holder.tvUser.setText(comment.getUser());
        holder.tvTime.setText("· " + comment.getTime());
        holder.tvText.setText(comment.getText());
        holder.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));
        holder.tvDislikeCount.setText(String.valueOf(comment.getDislikeCount()));

        // icons from model state
        holder.imgLike.setImageResource(comment.isLiked() ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
        holder.imgDislike.setImageResource(comment.isDisliked() ? R.drawable.ic_dislike_filled : R.drawable.ic_dislike_outline);

        // Like click (always call buffer with best available id)
        holder.layoutLike.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            // optimistic UI toggle
            if (comment.isLiked()) {
                comment.setLiked(false);
                comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                holder.imgLike.setImageResource(R.drawable.ic_like_outline);
            } else {
                comment.setLiked(true);
                comment.setLikeCount(comment.getLikeCount() + 1);
                holder.imgLike.setImageResource(R.drawable.ic_like_filled);
                if (comment.isDisliked()) {
                    comment.setDisliked(false);
                    comment.setDislikeCount(Math.max(0, comment.getDislikeCount() - 1));
                    holder.imgDislike.setImageResource(R.drawable.ic_dislike_outline);
                }
            }
            holder.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));
            holder.tvDislikeCount.setText(String.valueOf(comment.getDislikeCount()));

            // decide reaction target: prefer serverId, fall back to clientId (optimistic comments)
            String target = comment.getServerId() != null ? comment.getServerId() : comment.getClientId();
            if (target != null) {
                buffer.likeComment(target);
            } else {
                Log.d("InteractionsBuffer", "likeComment: no target id (serverId/clientId) for optimistic comment");
            }
        });

        // Dislike click (always call buffer)
        holder.layoutDislike.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            if (comment.isDisliked()) {
                comment.setDisliked(false);
                comment.setDislikeCount(Math.max(0, comment.getDislikeCount() - 1));
                holder.imgDislike.setImageResource(R.drawable.ic_dislike_outline);
            } else {
                comment.setDisliked(true);
                comment.setDislikeCount(comment.getDislikeCount() + 1);
                holder.imgDislike.setImageResource(R.drawable.ic_dislike_filled);
                if (comment.isLiked()) {
                    comment.setLiked(false);
                    comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
                    holder.imgLike.setImageResource(R.drawable.ic_like_outline);
                }
            }
            holder.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));
            holder.tvDislikeCount.setText(String.valueOf(comment.getDislikeCount()));

            String target = comment.getServerId() != null ? comment.getServerId() : comment.getClientId();
            if (target != null) {
                buffer.dislikeComment(target);
            } else {
                Log.d("InteractionsBuffer", "dislikeComment: no target id (serverId/clientId) for optimistic comment");
            }
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
        } else {
            holder.repliesContainer.setVisibility(View.GONE);
        }

        // reply input always hidden initially
        holder.layoutReplyBox.setVisibility(View.GONE);

        // ===== btnReply click behavior =====
        holder.btnReply.setOnClickListener(v -> {
            // If there are in-memory replies -> toggle expansion
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                if (expandedKeys.contains(key)) {
                    // collapse
                    expandedKeys.remove(key);
                    notifyItemChanged(position);
                } else {
                    // expand
                    expandedKeys.add(key);
                    notifyItemChanged(position);
                }
                return;
            }

            // No in-memory replies -> check PendingStore (off UI thread)
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
                        expandedKeys.add(key);
                        notifyItemChanged(position);
                    } else {
                        // toggle reply input box
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
}
