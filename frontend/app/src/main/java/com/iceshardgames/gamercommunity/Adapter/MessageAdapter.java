package com.iceshardgames.gamercommunity.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
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
import com.iceshardgames.gamercommunity.Activity.ChatScreen.MediaViewerActivity;
import com.iceshardgames.gamercommunity.Model.MessageItem;
import com.iceshardgames.gamercommunity.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MessageItem> messageList;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    public MessageAdapter(List<MessageItem> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isSentByUser() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageItem message = messageList.get(position);
        String mediaUri = message.getMediaUri();
        String mimeType = message.getFileType();

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            SentMessageHolder sentHolder = (SentMessageHolder) holder;
            sentHolder.messageText.setText(message.getMessage());
            sentHolder.messageTime.setText(message.getTime());

            if (mediaUri != null) {
                sentHolder.cardsent.setVisibility(View.VISIBLE);

                if (mimeType != null && mimeType.startsWith("image/")) {
                    Glide.with(holder.itemView.getContext())
                            .load(Uri.parse(mediaUri))
                            .placeholder(R.drawable.placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(sentHolder.mediaPreview); // or receivedHolder.mediaPreview
                } else if (mimeType != null && mimeType.startsWith("video/")) {
                    try {
                        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
                                String.valueOf(new File(Uri.parse(mediaUri).getPath())),
                                MediaStore.Images.Thumbnails.MINI_KIND
                        );
                        sentHolder.mediaPreview.setImageBitmap(thumbnail);
                    } catch (Exception e) {
                        sentHolder.mediaPreview.setImageResource(R.drawable.attrects); // fallback
                    }
                } else {
                    sentHolder.mediaPreview.setImageResource(R.drawable.attrects); // generic file icon
                }
                sentHolder.mediaPreview.setOnClickListener(v -> {
                    Intent intent = new Intent(holder.itemView.getContext(), MediaViewerActivity.class);
                    intent.putExtra("image_uri", mediaUri.toString());
                    intent.putExtra("message_id", message.getMessageId());  // 🔥 Pass ID here

                    holder.itemView.getContext().startActivity(intent);
                });

                sentHolder.mediaPreview.setOnLongClickListener(v -> {
                    downloadMedia(holder.itemView.getContext(), Uri.parse(mediaUri), mimeType);
                    return true;
                });
            } else {
                sentHolder.cardsent.setVisibility(View.GONE);
            }

        } else {
            ReceivedMessageHolder receivedHolder = (ReceivedMessageHolder) holder;
            receivedHolder.messageText.setText(message.getMessage());
            receivedHolder.messageTime.setText(message.getTime());
            receivedHolder.username.setText(message.getUsername());
            receivedHolder.avatar.setImageResource(R.drawable.profilepic); // Replace with actual avatar later

            if (mediaUri != null) {
                receivedHolder.cardreceived.setVisibility(View.VISIBLE);

                if (mimeType != null && mimeType.startsWith("image/")) {
                    Glide.with(holder.itemView.getContext())
                            .load(Uri.parse(mediaUri))
                            .placeholder(R.drawable.placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(receivedHolder.mediaPreview); // or receivedHolder.mediaPreview
                } else if (mimeType != null && mimeType.startsWith("video/")) {
                    try {
                        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
                                String.valueOf(new File(Uri.parse(mediaUri).getPath())),
                                MediaStore.Images.Thumbnails.MINI_KIND
                        );
                        receivedHolder.mediaPreview.setImageBitmap(thumbnail);
                    } catch (Exception e) {
                        receivedHolder.mediaPreview.setImageResource(R.drawable.attrects);
                    }
                    receivedHolder.mediaPreview.setOnClickListener(v -> {
                        Intent intent = new Intent(holder.itemView.getContext(), MediaViewerActivity.class);
                        intent.putExtra("image_uri", mediaUri.toString());
                        intent.putExtra("message_id", message.getMessageId());  // 🔥 Pass ID here

                        holder.itemView.getContext().startActivity(intent);
                    });

                    receivedHolder.mediaPreview.setOnLongClickListener(v -> {
                        downloadMedia(holder.itemView.getContext(), Uri.parse(mediaUri), mimeType);
                        return true;
                    });
                } else {
                    receivedHolder.mediaPreview.setImageResource(R.drawable.attrects);
                }
            } else {
                receivedHolder.cardreceived.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private void downloadMedia(Context context, Uri uri, String mimeType) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String extension = mimeType.substring(mimeType.indexOf("/") + 1);
            String filename = "chatfile_" + System.currentTimeMillis() + "." + extension;

            File outFile = new File(downloadsDir, filename);
            OutputStream outputStream = new FileOutputStream(outFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            Toast.makeText(context, "Downloaded to: " + outFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }


    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;
        ImageView mediaPreview;
        CardView cardsent;
        SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            mediaPreview = itemView.findViewById(R.id.media_preview);
            cardsent = itemView.findViewById(R.id.cardsend);
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime, username;
        ImageView avatar;
        ImageView mediaPreview;
        CardView cardreceived;

        ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTime = itemView.findViewById(R.id.message_time);
            username = itemView.findViewById(R.id.username);
            avatar = itemView.findViewById(R.id.avatar);
            mediaPreview = itemView.findViewById(R.id.media_preview);
            cardreceived = itemView.findViewById(R.id.cardreceive);
        }
    }
}
