package com.iceshardgames.gamercommunity.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.Comment;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList;
    private Context context;
    final int[] likeCount = {0};
    final int[] dislikeCount = {0};
    final boolean[] liked = {false};
    final boolean[] disliked = {false};
    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
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

        holder.tvUser.setText(comment.getUser());
        holder.tvTime.setText("· " + comment.getTime());
        holder.tvText.setText(comment.getText());
        holder.tvLikeCount.setText(String.valueOf(comment.getLikeCount()));
        holder.tvDislikeCount.setText(String.valueOf(comment.getDislikeCount()));

        holder.layoutLike.setOnClickListener(v -> {
            if (liked[0]) {
                likeCount[0]--;
                liked[0] = false;
                holder.imgLike.setImageResource(R.drawable.ic_like_outline);
            } else {
                likeCount[0]++;
                liked[0] = true;
                holder.imgLike.setImageResource(R.drawable.ic_like_filled);
                if (disliked[0]) {
                    dislikeCount[0]--;
                    disliked[0] = false;
                    holder.imgDislike.setImageResource(R.drawable.ic_dislike_outline);
                }
            }
            holder.tvLikeCount.setText(String.valueOf(likeCount[0]));
            holder.tvDislikeCount.setText(String.valueOf(dislikeCount[0]));
        });

        holder.layoutDislike.setOnClickListener(v -> {
            if (disliked[0]) {
                dislikeCount[0]--;
                disliked[0] = false;
                holder.imgDislike.setImageResource(R.drawable.ic_dislike_outline);
            } else {
                dislikeCount[0]++;
                disliked[0] = true;
                holder.imgDislike.setImageResource(R.drawable.ic_dislike_filled);
                if (liked[0]) {
                    likeCount[0]--;
                    liked[0] = false;
                    holder.imgLike.setImageResource(R.drawable.ic_like_outline);
                }
            }
            holder.tvLikeCount.setText(String.valueOf(likeCount[0]));
            holder.tvDislikeCount.setText(String.valueOf(dislikeCount[0]));
        });



        // Reply box toggle
        holder.btnReply.setOnClickListener(v -> {
            holder.layoutReplyBox.setVisibility(
                    holder.layoutReplyBox.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });

        // Send reply
        holder.btnSendReply.setOnClickListener(v -> {
            String replyText = holder.etReply.getText().toString().trim();
            if (!replyText.isEmpty()) {
                Comment reply = new Comment("You", replyText, "Now");
                comment.addReply(reply);
                holder.etReply.setText("");
                holder.layoutReplyBox.setVisibility(View.GONE);
                notifyItemChanged(position);
            }
        });

        // Share
        holder.layoutShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, comment.getUser() + " commented: " + comment.getText());
            context.startActivity(Intent.createChooser(shareIntent, "Share Comment"));
        });

        // Nested replies
        holder.repliesContainer.removeAllViews();
        for (Comment reply : comment.getReplies()) {
            View replyView = LayoutInflater.from(context).inflate(R.layout.reply_item, holder.repliesContainer, false);

            TextView tvReplyUser = replyView.findViewById(R.id.tvReplyUser);
            TextView tvReplyTime = replyView.findViewById(R.id.tvReplyTime);
            TextView tvReplyText = replyView.findViewById(R.id.tvReplyText);

            tvReplyUser.setText(reply.getUser());
            tvReplyTime.setText("· " + reply.getTime());
            tvReplyText.setText(reply.getText());

            Utills.GradientText(holder.tvCommentUser);
            holder.repliesContainer.addView(replyView);
        }

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvTime, tvText, tvLikeCount, tvDislikeCount, btnSendReply,tvCommentUser;
        ImageView imgLike, imgDislike;
        LinearLayout layoutLike, layoutDislike, layoutShare, layoutReplyBox, repliesContainer,btnReply;
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
            tvCommentUser = itemView.findViewById(R.id.tvCommentUser);

            repliesContainer = new LinearLayout(itemView.getContext());
            repliesContainer.setOrientation(LinearLayout.VERTICAL);
            ((LinearLayout) itemView).addView(repliesContainer);


        }
    }
}
