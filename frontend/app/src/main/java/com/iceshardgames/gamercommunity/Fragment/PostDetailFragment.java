package com.iceshardgames.gamercommunity.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Adapter.CommentAdapter;
import com.iceshardgames.gamercommunity.AddCommentBody;
import com.iceshardgames.gamercommunity.BulkOp;
import com.iceshardgames.gamercommunity.CommentItem;
import com.iceshardgames.gamercommunity.CommentResp;
import com.iceshardgames.gamercommunity.CommentsPage;
import com.iceshardgames.gamercommunity.GenericResp;
import com.iceshardgames.gamercommunity.InteractionsBuffer;
import com.iceshardgames.gamercommunity.Model.Comment;
import com.iceshardgames.gamercommunity.Model.Response.PostLikeResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.PendingStore;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailFragment extends Fragment {
    private String title, author, content;
    private RecyclerView recyclerComments;
    private CommentAdapter commentAdapter;
    private int replies;

    private List<Comment> commentList = new ArrayList<>();
    private int postLikes = 0, postDislikes = 0;
    private TextView commentCount;

    private boolean postLiked = false, postDisliked = false;
    private long createdAtMillis; // <-- add this
    private TextView tvTime; // keep a ref for ticker
    private final android.os.Handler timeHandler = new android.os.Handler();
    private String postId;
    private boolean isLikeRequestInFlight = false;
    private ApiService api;
    private InteractionsBuffer buffer;
    String accessToken;
    // UI fields (make these fragment fields so other methods can update them)
    private TextView tvLikeCount, tvDislikeCount;
    private ImageView imgLike, imgDislike;
    private LinearLayout layoutLike, layoutDislike, layoutShare;
    private long mLastClickTime = 0;
    // fragment fields
    private BroadcastReceiver shareChosenReceiver;
    private static final String ACTION_SHARE_CHOSEN = "com.iceshardgames.gamercommunity.SHARE_CHOSEN";


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
    // register in onCreate()
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shareChosenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("PostDetailFragment", "shareChosenReceiver: onReceive intent=" + intent);
                Toast.makeText(requireContext(), "Share target chosen", Toast.LENGTH_SHORT).show();

                // try to read passed post/comment ids from the callback Intent
                String callbackPostId = intent.hasExtra("share_post_id") ? intent.getStringExtra("share_post_id") : null;
                String callbackCommentId = intent.hasExtra("share_comment_id") ? intent.getStringExtra("share_comment_id") : null;
                String preview = intent.hasExtra("share_text_preview") ? intent.getStringExtra("share_text_preview") : null;

                Log.d("PostDetailFragment", "callbackPostId=" + callbackPostId + " callbackCommentId=" + callbackCommentId + " preview=" + preview);

                // chosen component (if provided)
                ComponentName chosen = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
                if (chosen != null) Log.d("PostDetailFragment", "CHOOSEN COMPONENT: " + chosen.flattenToString());

                // decide what to enqueue: prefer callback post id, fallback to fragment's postId
                String effectivePostId = callbackPostId != null ? callbackPostId : PostDetailFragment.this.postId;

                if (effectivePostId != null) {
                    // enqueue a post-level share
                    InteractionsBuffer.get(requireContext()).sharePost(effectivePostId);
                    // debug
//                    InteractionsBuffer.get(requireContext()).debugDumpBuffer();
                } else {
                    Log.w("PostDetailFragment", "No postId available to enqueue share");
                }
            }
        };

        // register receiver on Activity context
        ContextCompat.registerReceiver(requireActivity(), shareChosenReceiver, new IntentFilter(ACTION_SHARE_CHOSEN), ContextCompat.RECEIVER_NOT_EXPORTED);
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
            postLikes = getArguments().getInt("likes", 0);            // <- added
            createdAtMillis = getArguments().getLong("createdAt", 0L);
        }
        api = ApiClient.getRetrofit().create(ApiService.class);
        buffer = InteractionsBuffer.get(requireContext().getApplicationContext());
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("accessToken", null);
        Log.e("==lag", "postId: " + postId);
        Log.e("==lag", "accessToken: " + accessToken);

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

        imgLike = view.findViewById(R.id.imgPostLike);
        imgDislike = view.findViewById(R.id.imgPostDislike);
        tvLikeCount = view.findViewById(R.id.tvPostLikeCount);
        tvDislikeCount = view.findViewById(R.id.tvPostDislikeCount);
        commentCount = view.findViewById(R.id.commentcount);

        layoutLike = view.findViewById(R.id.layoutPostLike);
        layoutDislike = view.findViewById(R.id.layoutPostDislike);
        layoutShare = view.findViewById(R.id.layoutPostShare);
        Utills.GradientText(tvAuthor);
        Utills.GradientText(comtext);
// enable internal scrolling for the TextView
        tvContent.setMovementMethod(new ScrollingMovementMethod());

// ensure the outer ScrollView doesn't steal the vertical scroll when user interacts with tvContent
        tvContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // while touching the TextView, request that parent not intercept touch events
                v.getParent().requestDisallowInterceptTouchEvent(true);

                // when the user lifts finger, allow parent to intercept again (optional)
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false; // let TextView handle the touch (scrolling)
            }
        });
        Button btnFlushNow = view.findViewById(R.id.btnFlushNow);
        btnFlushNow.setOnClickListener(v -> {
            InteractionsBuffer.get(requireContext()).flushNow();
            Log.d("PostDetailFragment", "Manual flush triggered by DEV button");
        });

        // --- LIKE --- optimistic UI + enqueue to bulk ---
        // --- LIKE ---
        layoutLike.setOnClickListener(v -> {
            if (postId == null) return;

            // optimistic UI toggle
            if (postLiked) {
                postLikes = Math.max(0, postLikes - 1);
                postLiked = false;
                imgLike.setImageResource(R.drawable.ic_like_outline);
            } else {
                postLikes = postLikes + 1;
                postLiked = true;
                imgLike.setImageResource(R.drawable.ic_like_filled);
                if (postDisliked) {
                    postDislikes = Math.max(0, postDislikes - 1);
                    postDisliked = false;
                    imgDislike.setImageResource(R.drawable.ic_dislike_outline);
                }
            }
            if (tvLikeCount != null) tvLikeCount.setText(String.valueOf(postLikes));
            if (tvDislikeCount != null) tvDislikeCount.setText(String.valueOf(postDislikes));

            // ALWAYS call buffer so it can toggle/replace/remove as needed
            buffer.likePost(postId);
        });

// --- DISLIKE ---
        layoutDislike.setOnClickListener(v -> {
            if (postId == null) return;

            if (postDisliked) {
                postDislikes = Math.max(0, postDislikes - 1);
                postDisliked = false;
                imgDislike.setImageResource(R.drawable.ic_dislike_outline);
            } else {
                postDislikes = postDislikes + 1;
                postDisliked = true;
                imgDislike.setImageResource(R.drawable.ic_dislike_filled);
                if (postLiked) {
                    postLikes = Math.max(0, postLikes - 1);
                    postLiked = false;
                    imgLike.setImageResource(R.drawable.ic_like_outline);
                }
            }
            if (tvLikeCount != null) tvLikeCount.setText(String.valueOf(postLikes));
            if (tvDislikeCount != null) tvDislikeCount.setText(String.valueOf(postDislikes));

            // ALWAYS call buffer so it can toggle/replace/remove as needed
            buffer.dislikePost(postId);
        });


        // --- SHARE ---
        // --- SHARE ---
        layoutShare.setOnClickListener(v -> {
            if (System.currentTimeMillis() - mLastClickTime < 1000) return;
            mLastClickTime = System.currentTimeMillis();

            // Build a tidy excerpt (max 200 chars)
            String excerpt = "";
            if (content != null) {
                excerpt = content.length() > 200 ? content.substring(0, 200).trim() + "…" : content;
            }

            String timeLabel = createdAtMillis > 0 ? Utills.getTimeAgo(createdAtMillis) : "just now";

            // Construct a friendly share text and subject
            String subject = title != null ? title : "Check out this post";
            String postUrl = "https://iceshardgames.com/posts/" + (postId != null ? postId : "");
            String shareText = new StringBuilder()
                    .append(subject).append("\n")
                    .append("By ").append(author != null ? author : "Unknown").append(" · ").append(timeLabel).append("\n\n")
                    .append(excerpt).append("\n\n")
                    .append("Read more: ").append(postUrl).append("\n\n")
                    .append("Shared via IceShard Games Community")
                    .toString();

            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, subject);
            send.putExtra(Intent.EXTRA_TEXT, shareText);

            // Build callback intent for our receiver with metadata
            Intent callbackIntent = new Intent(ACTION_SHARE_CHOSEN);
            callbackIntent.putExtra("share_post_id", postId);
            callbackIntent.putExtra("share_text_preview", excerpt);
            callbackIntent.setPackage(requireContext().getPackageName());

            PendingIntent pi = PendingIntent.getBroadcast(
                    requireContext(),
                    (int) (System.currentTimeMillis() & 0xfffffff), // reduce collision risk
                    callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Intent chooser = Intent.createChooser(send, "Share Post", pi.getIntentSender());
            try {
                startActivity(chooser);
            } catch (Exception e) {
                // fallback to plain chooser if IntentSender not supported on some devices
                startActivity(Intent.createChooser(send, "Share Post"));
            }
        });


        recyclerComments = view.findViewById(R.id.recyclerComments);
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter(commentList, getContext(), buffer, api, postId);
        recyclerComments.setAdapter(commentAdapter);

        loadComments(/*parent*/null);

        // Add comment box
        EditText etAddComment = view.findViewById(R.id.etAddComment);
        TextView btnSendComment = view.findViewById(R.id.btnSendComment);

        btnSendComment.setOnClickListener(v -> {
            String commentText = etAddComment.getText().toString().trim();
            if (commentText.isEmpty()) return;

            // create temporary client id
            String clientId = java.util.UUID.randomUUID().toString();

            // optimistic UI: show the comment and mark it with clientId
            Comment newComment = new Comment(getDisplayName(getContext()), commentText, "Now");
            newComment.setClientId(clientId);     // add this helper to your Comment model
            commentList.add(0, newComment);
            commentAdapter.notifyItemInserted(0);
            recyclerComments.scrollToPosition(0);
            etAddComment.setText("");
// update comment count after optimistic add (top-level only)
            try {
                commentCount.setText(String.valueOf(computeTopLevelComments(commentList)));
            } catch (Exception ignore) {}

            // enqueue as bulk op (create_comment)
            buffer.enqueueCreateComment(postId, commentText, /* parentComment */ null, clientId);

            // (optional) if you want immediate server create instead of bulk:
            // api.addComment("Bearer " + accessToken, postId, new AddCommentBody(commentText))
        });
        buffer.setOnBulkSuccessListener(new InteractionsBuffer.OnBulkSuccessListener() {
            @Override
            public void onBulkSuccess(List<String> affectedPostIds) {
                // existing behaviour: refresh comments if needed
                if (affectedPostIds != null && affectedPostIds.contains(postId)) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> loadComments(null));
                    }
                }
            }

            @Override
            public void onCreateCommentConfirmed(String postIdConfirmed, String clientId, CommentItem created) {
                if (!PostDetailFragment.this.postId.equals(postIdConfirmed)) {
                    // different post — still remove pending by clientId so it won't be re-inserted forever
                    PendingStore.get(requireContext()).removeByClientId(clientId);
                    return;
                }

                if (!isAdded()) {
                    // fragment not active, still remove from PendingStore
                    PendingStore.get(requireContext()).removeByClientId(clientId);
                    return;
                }

                // Ensure UI updates happen on main thread
                requireActivity().runOnUiThread(() -> {
                    try {
                        boolean replaced = replaceOptimisticComment(clientId, created);

                        // If we replaced an optimistic entry in-place, just refresh adapter
                        if (replaced) {
                            // Adapter will reflect updated serverId/time/likes etc.
                            commentAdapter.notifyDataSetChanged();
                        } else {
                            // If we couldn't find the optimistic item, reload from server to be safe
                            loadComments(null);
                        }

                        // Update top-level comment counter (assumes commentCount is a fragment field)
                        try {
                            if (commentCount != null) {
                                commentCount.setText(String.valueOf(computeTopLevelComments(commentList)));
                            }
                        } catch (Exception ignore) {}

                    } catch (Exception e) {
                        Log.e("PostDetailFragment", "onCreateCommentConfirmed error", e);
                        // Fallback: reload comments to keep UI consistent
                        try { loadComments(null); } catch (Exception ignore) {}
                    }
                });
            }
        });


        return view;
    }

    // return true if found & updated
// return true if found & updated
    private boolean replaceOptimisticComment(String clientId, CommentItem created) {
        if (clientId == null) return false;

        // Keep created id for later mapping
        final String createdServerId = created != null ? created.id : null;

        // 1) search top-level comments
        for (Comment c : commentList) {
            // top-level optimistic
            if (clientId.equals(c.getClientId())) {
                // update fields from created (CommentItem)
                c.setServerId(createdServerId);
                if (created != null && created.createdAt != null) {
                    long createdMillis = Utills.parseIso8601ToMillis(created.createdAt);
                    c.setTime(Utills.getTimeAgo(createdMillis));
                    c.setLikeCount(created.likes);
                    c.setDislikeCount(created.dislikes);
                }
                // clear clientId — it's now confirmed
                c.setClientId(null);

                // IMPORTANT: update any pending create_comment ops that used this parent's clientId
                // so replies that referenced the old clientId will now point to the serverId.
                if (createdServerId != null) {
                    List<BulkOp> pending = PendingStore.get(requireContext()).loadAll();
                    boolean changed = false;
                    for (BulkOp op : pending) {
                        if ("create_comment".equals(op.op) && clientId.equals(op.parentComment)) {
                            op.parentComment = createdServerId;
                            changed = true;
                        }
                    }
                    if (changed) PendingStore.get(requireContext()).saveAll(pending);
                }

                return true;
            }

            // search replies (child optimistic)
            for (Comment r : c.getReplies()) {
                if (clientId.equals(r.getClientId())) {
                    r.setServerId(createdServerId);
                    if (created != null && created.createdAt != null) {
                        long createdMillis = Utills.parseIso8601ToMillis(created.createdAt);
                        r.setTime(Utills.getTimeAgo(createdMillis));
                        r.setLikeCount(created.likes);
                        r.setDislikeCount(created.dislikes);
                    }
                    r.setClientId(null);
                    return true;
                }
            }
        }
        return false;
    }


    private String getDisplayName(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("username", "You");
    }

    private void loadComments(String parentId) {
        List<BulkOp> pending = PendingStore.get(requireContext()).loadAll();
        Log.d("InteractionsBuffer", "Pending ops size=" + pending.size());
        int totalPending = pending != null ? pending.size() : 0;

        for (BulkOp p : pending) {
//            Log.d("InteractionsBuffer", "op=" + p.op + " type=" + p.type + " postId=" + p.postId + " commentId=" + p.commentId + " clientId=" + p.clientId);
        }
        // filter to current postId
        List<BulkOp> pendingForThisPost = new ArrayList<>();
        if (pending != null && postId != null) {
            for (BulkOp p : pending) {
                if (p == null) continue;
                if (postId.equals(p.postId)) pendingForThisPost.add(p);
            }
        }

        Log.d("InteractionsBuffer", "Pending ops total=" + totalPending + " pendingForThisPost=" + pendingForThisPost.size());
        for (BulkOp p : pendingForThisPost) {
            Log.d("InteractionsBuffer", "PENDING[post=" + p.postId + " op=" + p.op + " type=" + p.type +
                    " commentId=" + p.commentId + " parent=" + p.parentComment + " clientId=" + p.clientId + "]");
        }

        api.listComments("Bearer " + accessToken, postId, parentId, 1, 50).enqueue(new Callback<GenericResp<CommentsPage>>() {
            @Override
            public void onResponse(Call<GenericResp<CommentsPage>> call, Response<GenericResp<CommentsPage>> res) {
                if (!res.isSuccessful() || res.body() == null || !res.body().success) return;

                // --- Build server model and maps ---
                java.util.Map<String, Comment> byServerId = new java.util.HashMap<>();
                List<CommentItem> serverItems = res.body().data.comments != null ? res.body().data.comments : new ArrayList<>();

                for (CommentItem it : serverItems) {
                    long createdMillis = Utills.parseIso8601ToMillis(it.createdAt);
                    Comment c = new Comment(getDisplayName(getContext()), it.content, Utills.getTimeAgo(createdMillis));
                    c.setLikeCount(it.likes);
                    c.setDislikeCount(it.dislikes);
                    c.setServerId(it.id);
                    byServerId.put(it.id, c);
                }

                // Attach server replies to their server parents
                List<Comment> topLevel = new ArrayList<>();
                for (CommentItem it : serverItems) {
                    Comment c = byServerId.get(it.id);
                    String serverParentId = null;
                    // common parent field name variations: parent, parentCommentId, parentComment
                    if (it.parentCommentId != null) serverParentId = it.parentCommentId;
                    else {
                        try {
                            java.lang.reflect.Field f = it.getClass().getDeclaredField("parent");
                            f.setAccessible(true);
                            Object val = f.get(it);
                            if (val != null) serverParentId = String.valueOf(val);
                        } catch (Exception ignored) {}
                    }
                    if (serverParentId != null && !serverParentId.isEmpty()) {
                        Comment parent = byServerId.get(serverParentId);
                        if (parent != null) parent.addReply(c);
                        else topLevel.add(c); // parent not present in page -> fallback to top-level
                    } else {
                        topLevel.add(c); // no parent -> top-level
                    }
                }

                // --- Merge pending optimistic creates from PendingStore ---
                commentList.clear();
                commentList.addAll(topLevel);

                List<BulkOp> pending = PendingStore.get(requireContext()).loadAll();

                // map of clientId->Comment for in-memory optimistic comments we add below
                java.util.Map<String, Comment> byClientId = new java.util.HashMap<>();
                for (Comment c : commentList) {
                    if (c.getClientId() != null) byClientId.put(c.getClientId(), c);
                }

                // 1) add top-level pending create_comment (parentComment == null)
                for (BulkOp op : pending) {
                    if (!"create_comment".equals(op.op)) continue;
                    if (op.postId == null || !op.postId.equals(postId)) continue;
                    if (op.parentComment == null) {
                        boolean exists = false;
                        if (op.clientId != null) {
                            for (Comment c : commentList) {
                                if (op.clientId.equals(c.getClientId())) { exists = true; break; }
                            }
                        }
                        if (!exists) {
                            Comment opt = new Comment(getDisplayName(getContext()), op.content, "Now");
                            opt.setClientId(op.clientId);
                            commentList.add(0, opt);
                            if (op.clientId != null) byClientId.put(op.clientId, opt);
                        }
                    }
                }

                // 2) attach pending replies to parent (parent may be serverId or clientId)
                for (BulkOp op : pending) {
                    if (!"create_comment".equals(op.op)) continue;
                    if (op.postId == null || !op.postId.equals(postId)) continue;
                    if (op.parentComment == null) continue;

                    // dedupe by clientId
                    boolean alreadyAttached = false;
                    if (op.clientId != null) {
                        for (Comment parentCheck : commentList) {
                            for (Comment r : parentCheck.getReplies()) {
                                if (op.clientId.equals(r.getClientId())) { alreadyAttached = true; break; }
                            }
                            if (alreadyAttached) break;
                        }
                    }
                    if (alreadyAttached) continue;

                    // parent lookup: server first, then optimistic clients
                    Comment parent = byServerId.get(op.parentComment);
                    if (parent == null) parent = byClientId.get(op.parentComment);

                    if (parent != null) {
                        Comment optReply = new Comment(getDisplayName(getContext()), op.content, "Now");
                        optReply.setClientId(op.clientId);
                        optReply.setParentServerId(op.parentComment);
                        parent.addReply(optReply);
                    } else {
                        // create a placeholder parent (parentComment was likely a clientId we don't have)
                        Comment placeholderParent = new Comment(getDisplayName(getContext()), "[reply]", "Now");
                        placeholderParent.setClientId(op.parentComment); // store the clientId so it can be matched later
                        commentList.add(0, placeholderParent);
                        byClientId.put(placeholderParent.getClientId(), placeholderParent);

                        Comment optReply = new Comment(getDisplayName(getContext()), op.content, "Now");
                        optReply.setClientId(op.clientId);
                        optReply.setParentServerId(op.parentComment);
                        placeholderParent.addReply(optReply);
                    }
                }

                // --- Attach confirmed created stubs (saved during flush) so confirmed replies persist even if server list hasn't included them yet ---
                try {
                    List<String> confirmed = PendingStore.get(requireContext()).loadConfirmedCreatedComments(postId);
                    if (confirmed != null) {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        for (String j : confirmed) {
                            if (j == null) continue;
                            try {
                                java.util.Map map = gson.fromJson(j, java.util.Map.class);
                                String clientId = map.get("clientId") == null ? null : String.valueOf(map.get("clientId"));
                                String serverId = map.get("id") == null ? null : String.valueOf(map.get("id"));
                                String content = map.get("content") == null ? "" : String.valueOf(map.get("content"));
                                String createdAt = map.get("createdAt") == null ? null : String.valueOf(map.get("createdAt"));
                                String parent = map.get("parent") == null ? null : String.valueOf(map.get("parent"));
                                int likes = map.get("likes") == null ? 0 : ((Number) map.get("likes")).intValue();
                                int dislikes = map.get("dislikes") == null ? 0 : ((Number) map.get("dislikes")).intValue();

                                // Check if already present (either by serverId or by clientId)
                                boolean alreadyPresent = false;
                                if (serverId != null) {
                                    for (Comment c : commentList) {
                                        if (serverId.equals(c.getServerId())) { alreadyPresent = true; break; }
                                        for (Comment r : c.getReplies()) if (serverId.equals(r.getServerId())) { alreadyPresent = true; break; }
                                        if (alreadyPresent) break;
                                    }
                                }
                                if (!alreadyPresent && clientId != null) {
                                    for (Comment c : commentList) {
                                        if (clientId.equals(c.getClientId())) { alreadyPresent = true; break; }
                                        for (Comment r : c.getReplies()) if (clientId.equals(r.getClientId())) { alreadyPresent = true; break; }
                                        if (alreadyPresent) break;
                                    }
                                }

                                if (alreadyPresent) {
                                    // update optimistic entry with server id/time/likes/dislikes and remove saved confirmed entry
                                    for (Comment c : commentList) {
                                        if (clientId != null && clientId.equals(c.getClientId())) {
                                            c.setServerId(serverId);
                                            if (createdAt != null) c.setTime(Utills.getTimeAgo(Utills.parseIso8601ToMillis(createdAt)));
                                            c.setLikeCount(likes);
                                            c.setDislikeCount(dislikes);
                                            c.setClientId(null);
                                        }
                                        for (Comment r : c.getReplies()) {
                                            if (clientId != null && clientId.equals(r.getClientId())) {
                                                r.setServerId(serverId);
                                                if (createdAt != null) r.setTime(Utills.getTimeAgo(Utills.parseIso8601ToMillis(createdAt)));
                                                r.setLikeCount(likes);
                                                r.setDislikeCount(dislikes);
                                                r.setClientId(null);
                                            }
                                        }
                                    }
                                    PendingStore.get(requireContext()).removeConfirmedCreatedByClientId(postId, clientId);
                                    continue;
                                }

                                // Not present -> attach confirmed record
                                Comment confirmedComment = new Comment(getDisplayName(getContext()), content,
                                        createdAt != null ? Utills.getTimeAgo(Utills.parseIso8601ToMillis(createdAt)) : "Now");
                                confirmedComment.setClientId(clientId);
                                confirmedComment.setServerId(serverId);
                                confirmedComment.setLikeCount(likes);
                                confirmedComment.setDislikeCount(dislikes);
                                confirmedComment.setParentServerId(parent);

                                if (parent == null || parent.isEmpty()) {
                                    // top-level
                                    commentList.add(0, confirmedComment);
                                } else {
                                    // attach to parent by serverId or clientId
                                    Comment parentC = byServerId.get(parent);
                                    if (parentC == null) parentC = byClientId.get(parent);
                                    if (parentC != null) parentC.addReply(confirmedComment);
                                    else {
                                        // fallback -> top-level
                                        commentList.add(0, confirmedComment);
                                    }
                                }
                            } catch (Exception ignore) {}
                        }
                    }
                } catch (Exception ignore) {}

                // --- Apply pending reaction ops (likes/dislikes) ---
                applyPendingToComments(commentList);

                // --- Update post UI counters/icons (fragment fields) ---
                if (tvLikeCount != null) tvLikeCount.setText(String.valueOf(postLikes));
                if (tvDislikeCount != null) tvDislikeCount.setText(String.valueOf(postDislikes));
                if (imgLike != null) imgLike.setImageResource(postLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
                if (imgDislike != null) imgDislike.setImageResource(postDisliked ? R.drawable.ic_dislike_filled : R.drawable.ic_dislike_outline);
// update comments counter: top-level only
                try {
                    final int total = computeTopLevelComments(commentList);
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> commentCount.setText(String.valueOf(total)));
                    } else {
                        commentCount.setText(String.valueOf(total));
                    }
                } catch (Exception ignore) {}

                // --- Notify adapter (adapter renders comment.getReplies()) ---
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<GenericResp<CommentsPage>> call, Throwable t) {
                // optionally show load error / fallback
            }
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView(); /* optional: InteractionsBuffer.get(...).flush(); */
        if (shareChosenReceiver != null) {
            try { requireActivity().unregisterReceiver(shareChosenReceiver); } catch (Exception ignore) {}
            shareChosenReceiver = null;
        }
        if (buffer != null) buffer.clearOnBulkSuccessListener();
        // avoid leaking views
        tvLikeCount = null;
        tvDislikeCount = null;
        imgLike = null;
        imgDislike = null;
        layoutLike = null;
        layoutDislike = null;
        layoutShare = null;
    }

    /**
     * Merge pending local ops (from PendingStore) onto server-fetched comments and post counters.
     * Call this AFTER commentList is populated from server and BEFORE notifyDataSetChanged().
     */
    /**
     * Merge pending local ops (from PendingStore) onto server-fetched comments and post counters.
     * This version computes the latest effective reaction per target and applies it once
     * to avoid double-counting duplicate/pending ops.
     */
    private void applyPendingToComments(List<Comment> comments) {
        // load persisted pending ops
        List<BulkOp> pending = PendingStore.get(requireContext()).loadAll();

        // Start from server values
        int adjPostLikes = postLikes;
        int adjPostDislikes = postDislikes;
        boolean adjPostLiked = postLiked;
        boolean adjPostDisliked = postDisliked;

        // Build maps of the *latest* pending reaction per postId and per commentId.
        // We iterate in list order (oldest->newest). If PendingStore preserves order where
        // newer ops are appended, the later entry will win. To be robust, we record later ones.
        java.util.Map<String, String> lastPostReaction = new java.util.HashMap<>();     // postId -> "like"/"dislike"
        java.util.Map<String, String> lastCommentReaction = new java.util.HashMap<>();  // commentId -> "like"/"dislike"

        for (BulkOp op : pending) {
            if (op == null || op.op == null) continue;
            if ("post_reaction".equals(op.op) && op.postId != null && op.type != null) {
                // later ops override earlier: just put() will make the last seen win
                lastPostReaction.put(op.postId, op.type);
            } else if ("comment_reaction".equals(op.op) && op.commentId != null && op.type != null) {
                lastCommentReaction.put(op.commentId, op.type);
            }
        }

        // Apply post-level reactions: only one effective op per post
        if (!lastPostReaction.isEmpty()) {
            String myPostType = lastPostReaction.get(postId); // only care about this post
            if (myPostType != null) {
                if ("like".equals(myPostType)) {
                    // If server already says liked, don't double-increment; we assume server values are base
                    // The simplest robust policy: set liked=true and compute counts conservatively
                    adjPostLiked = true;
                    adjPostDisliked = false;
                    // If server value didn't have the like yet, increment by 1
                    // We check whether the server value already reflected a like by checking postLiked (server-origin).
                    if (!postLiked) adjPostLikes = Math.max(0, adjPostLikes + 1);
                } else if ("dislike".equals(myPostType)) {
                    adjPostDisliked = true;
                    adjPostLiked = false;
                    if (!postDisliked) adjPostDislikes = Math.max(0, adjPostDislikes + 1);
                }
            }
        }

        // Apply comment-level reactions using the lastCommentReaction map
        if (!lastCommentReaction.isEmpty()) {
            for (Comment c : comments) {
                String last = lastCommentReaction.get(c.getServerId());
                if (last == null) continue;
                if ("like".equals(last)) {
                    // ensure idempotency: only apply if model doesn't already reflect it
                    if (!c.isLiked()) {
                        c.setLikeCount(c.getLikeCount() + 1);
                    }
                    c.setLiked(true);
                    c.setDisliked(false);
                } else if ("dislike".equals(last)) {
                    if (!c.isDisliked()) {
                        c.setDislikeCount(c.getDislikeCount() + 1);
                    }
                    c.setDisliked(true);
                    c.setLiked(false);
                }
            }
        }

        // Write adjusted values back to fragment fields
        postLikes = adjPostLikes;
        postDislikes = adjPostDislikes;
        postLiked = adjPostLiked;
        postDisliked = adjPostDisliked;

        // Note: UI views are updated by caller (loadComments) after this method runs.
    }

    private int computeTopLevelComments(List<Comment> list) {
        return list == null ? 0 : list.size();
    }

}