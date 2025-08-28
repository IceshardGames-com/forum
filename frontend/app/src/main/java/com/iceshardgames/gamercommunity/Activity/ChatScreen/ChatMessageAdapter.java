package com.iceshardgames.gamercommunity.Activity.ChatScreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.iceshardgames.gamercommunity.Model.MessageDto;
import com.iceshardgames.gamercommunity.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int OUT = 1, IN = 2;
    private final String myUserId;
    private final List<MessageDto> list = new ArrayList<>();
    private final String partnerName;
    private final Activity activity;

    public ChatMessageAdapter(String myUserId, String partnerName, Activity activity) {
        this.myUserId = myUserId;
        this.partnerName = partnerName;
        this.activity = activity;
        setHasStableIds(true);
    }

    public void setAll(List<MessageDto> msgs) {
        list.clear();
        if (msgs != null) list.addAll(msgs);
        notifyDataSetChanged();
    }

    public void add(MessageDto m) {
        if (m == null) return;
        list.add(m);
        notifyItemInserted(list.size() - 1);
    }

    public void setItems(List<MessageDto> newItems) {
        list.clear();
        if (newItems != null) list.addAll(newItems);
        notifyDataSetChanged();
    }

    public List<MessageDto> getItems() { return list; }

    /** Merge server ACK into the last optimistic outgoing message. */
    public void ackLastOutgoingFromMe(long confirmedTs, String serverId) {
        for (int i = list.size() - 1; i >= 0; i--) {
            MessageDto m = list.get(i);
            if (m == null) continue;
            if (myUserId != null && myUserId.equals(m.getSender())) {
                m.setId(serverId);
                m.setSentAt(confirmedTs);
                notifyItemChanged(i);
                return;
            }
        }
    }

    /** Update a message timestamp directly by id. (Unused but handy.) */
    public void updateMessageTimestampById(String messageId, long epochMillis) {
        if (messageId == null) return;
        for (int i = 0; i < list.size(); i++) {
            MessageDto m = list.get(i);
            if (messageId.equals(m.getId())) {
                m.setSentAt(epochMillis);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @Override public long getItemId(int position) {
        return list.get(position).getId().hashCode();
    }

    @Override public int getItemViewType(int position) {
        MessageDto m = list.get(position);
        return (m != null && myUserId != null && myUserId.equals(m.getSender())) ? OUT : IN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        int layout = vt == OUT ? R.layout.item_msg_out : R.layout.item_msg_in;
        View v = LayoutInflater.from(p.getContext()).inflate(layout, p, false);
        return vt == OUT ? new SentVH(v) : new ReceivedVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int i) {
        MessageDto message = list.get(i);
        String mediaUri = message.getMediaUri();
        String mimeType = message.getFileType();
        String timeText = formatTimeOrDash(message.getSentAt());

        if (getItemViewType(i) == OUT) {
            SentVH holder = (SentVH) h;
            holder.messageText.setText(message.getText());
            holder.messageTime.setText(timeText);

            if (mediaUri != null) {
                holder.cardsent.setVisibility(View.VISIBLE);
                loadMedia(holder.itemView.getContext(), mediaUri, mimeType, holder.mediaPreview);
            } else {
                holder.cardsent.setVisibility(View.GONE);
            }
        } else {
            ReceivedVH holder = (ReceivedVH) h;
            holder.messageText.setText(message.getText());
            holder.messageTime.setText(timeText);
            holder.username.setText(partnerName != null ? partnerName : (message.getSender() != null ? message.getSender() : "User"));
            holder.avatar.setImageResource(R.drawable.profilepic); // replace if you have real avatars

            if (mediaUri != null) {
                holder.cardreceived.setVisibility(View.VISIBLE);
                loadMedia(holder.itemView.getContext(), mediaUri, mimeType, holder.mediaPreview);
            } else {
                holder.cardreceived.setVisibility(View.GONE);
            }
        }
    }

    private void loadMedia(Context context, String mediaUri, String mimeType, ImageView preview) {
        if (mimeType != null && mimeType.startsWith("image/")) {
            Glide.with(context)
                    .load(Uri.parse(mediaUri))
                    .placeholder(R.drawable.placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(preview);
        } else if (mimeType != null && mimeType.startsWith("video/")) {
            try {
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
                        String.valueOf(new File(Uri.parse(mediaUri).getPath())),
                        MediaStore.Images.Thumbnails.MINI_KIND
                );
                preview.setImageBitmap(thumbnail);
            } catch (Exception e) {
                preview.setImageResource(R.drawable.attrects);
            }
        } else {
            preview.setImageResource(R.drawable.attrects); // generic file icon
        }

        preview.setOnClickListener(v -> {
            Intent intent = new Intent(context, MediaViewerActivity.class);
            intent.putExtra("image_uri", mediaUri);
            context.startActivity(intent);
        });

        preview.setOnLongClickListener(v -> {
            Toast.makeText(context, "Long press: implement download", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override public int getItemCount() { return list.size(); }

    static class SentVH extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, username;
        ImageView mediaPreview;
        CardView cardsent;
        SentVH(View v) {
            super(v);
            messageText = v.findViewById(R.id.tv);
            messageTime = v.findViewById(R.id.message_time);
            mediaPreview = v.findViewById(R.id.media_preview);
            cardsent = v.findViewById(R.id.cardsend);
        }
    }

    static class ReceivedVH extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, username;
        ImageView avatar, mediaPreview;
        CardView cardreceived;
        ReceivedVH(View v) {
            super(v);
            messageText = v.findViewById(R.id.tv);
            messageTime = v.findViewById(R.id.message_time);
            username = v.findViewById(R.id.username);
            avatar = v.findViewById(R.id.avatar);
            mediaPreview = v.findViewById(R.id.media_preview);
            cardreceived = v.findViewById(R.id.cardreceive);
        }
    }

    // ----- Utils -----
    private static String formatTimeOrDash(long epochMillis) {
        if (epochMillis <= 0) return "—";
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(epochMillis));
    }
}
