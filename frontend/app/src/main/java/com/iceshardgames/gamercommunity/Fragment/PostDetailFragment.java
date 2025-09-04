package com.iceshardgames.gamercommunity.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Adapter.CommentAdapter;
import com.iceshardgames.gamercommunity.Model.Comment;
import com.iceshardgames.gamercommunity.Model.Response.PostLikeResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends Fragment {
    private String title, author, content;
    private RecyclerView recyclerComments;
    private CommentAdapter commentAdapter;
    private int replies;
    private List<Comment> commentList = new ArrayList<>();
    private int postLikes = 0, postDislikes = 0;
    private boolean postLiked = false, postDisliked = false;
    private long createdAtMillis; // <-- add this
    private TextView tvTime; // keep a ref for ticker
    private final android.os.Handler timeHandler = new android.os.Handler();
    private String postId;
    private boolean isLikeRequestInFlight = false;

    private final Runnable timeTicker = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && tvTime != null && createdAtMillis > 0) {
                tvTime.setText(Utills.getTimeAgo(createdAtMillis));
                timeHandler.postDelayed(this, 60_000); // every 60s
            }
        }
    };

    public static PostDetailFragment newInstance(String postId, String title, String author, int likes, int replies, long createdAtMillis) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString("postId", postId);
        args.putString("title", title);
        args.putString("author", author);
        args.putInt("likes", likes);
        args.putInt("replies", replies);
        args.putLong("createdAt", createdAtMillis);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            title = getArguments().getString("title");
            author = getArguments().getString("author");
            content = getArguments().getString("content");
            replies = getArguments().getInt("replies");
            createdAtMillis = getArguments().getLong("createdAt", 0L);
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        Log.e("==lag", "postId: "+postId );
        Log.e("==lag", "accessToken: "+accessToken );

        TextView tvTitle = view.findViewById(R.id.tvPostTitle);
        TextView tvAuthor = view.findViewById(R.id.tvPostAuthor);
        tvTime = view.findViewById(R.id.tvPostTime);
        TextView tvContent = view.findViewById(R.id.tvPostContent);
        TextView comtext = view.findViewById(R.id.comtext);

        tvTitle.setText(title);
        tvAuthor.setText(title);
        tvContent.setText(author);
        // set initial time label
        if (createdAtMillis > 0) {
            tvTime.setText(Utills.getTimeAgo(createdAtMillis));
        } else {
            tvTime.setText(""); // or "just now"
        }
        LinearLayout layoutLike = view.findViewById(R.id.layoutPostLike);
        LinearLayout layoutDislike = view.findViewById(R.id.layoutPostDislike);
        LinearLayout layoutShare = view.findViewById(R.id.layoutPostShare);

        ImageView imgLike = view.findViewById(R.id.imgPostLike);
        ImageView imgDislike = view.findViewById(R.id.imgPostDislike);
        TextView tvLikeCount = view.findViewById(R.id.tvPostLikeCount);
        TextView tvDislikeCount = view.findViewById(R.id.tvPostDislikeCount);
        Utills.GradientText(tvAuthor);
        Utills.GradientText(comtext);
        layoutLike.setOnClickListener(v -> {
            if (postLiked) {
                postLikes--;
                postLiked = false;
                imgLike.setImageResource(R.drawable.ic_like_outline);
            } else {
                postLikes++;
                postLiked = true;
                imgLike.setImageResource(R.drawable.ic_like_filled);
                if (postDisliked) {
                    postDislikes--;
                    postDisliked = false;
                    imgDislike.setImageResource(R.drawable.ic_dislike_outline);
                }
            }
            tvLikeCount.setText(String.valueOf(postLikes));
            tvDislikeCount.setText(String.valueOf(postDislikes));
//            PostLikeAPI(imgLike, tvLikeCount, tvDislikeCount);
        });

        layoutDislike.setOnClickListener(v -> {

            if (postDisliked) {
                postDislikes--;
                postDisliked = false;
                imgDislike.setImageResource(R.drawable.ic_dislike_outline);
            } else {
                postDislikes++;
                postDisliked = true;
                imgDislike.setImageResource(R.drawable.ic_dislike_filled);
                if (postLiked) {
                    postLikes--;
                    postLiked = false;
                    imgLike.setImageResource(R.drawable.ic_like_outline);
                }
            }
            tvLikeCount.setText(String.valueOf(postLikes));
            tvDislikeCount.setText(String.valueOf(postDislikes));
        });

        layoutShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content);
            startActivity(Intent.createChooser(shareIntent, "Share Post"));
        });

        recyclerComments = view.findViewById(R.id.recyclerComments);
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(commentList, getContext());
        recyclerComments.setAdapter(commentAdapter);

        // Example comments
        Comment c1 = new Comment("VRGamer123", "This looks amazing!", "1h ago");
        Comment c2 = new Comment("TechReviewer", "Runs smooth on my PC.", "30m ago");
        commentList.add(c1);
        commentList.add(c2);
        commentAdapter.notifyDataSetChanged();

        // Add comment box
        EditText etAddComment = view.findViewById(R.id.etAddComment);
        TextView btnSendComment = view.findViewById(R.id.btnSendComment);

        btnSendComment.setOnClickListener(v -> {
            String commentText = etAddComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                Comment newComment = new Comment("You", commentText, "Now");
                commentList.add(0, newComment); // add to top
                commentAdapter.notifyItemInserted(0);
                recyclerComments.scrollToPosition(0);
                etAddComment.setText("");
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        timeHandler.post(timeTicker); // start refresh
    }

    @Override
    public void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timeTicker); // stop refresh
    }
}