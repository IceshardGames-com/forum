package com.iceshardgames.gamercommunity.Activity.ChatScreen;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.DB.ChatMessageDao;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.databinding.ActivityMediaViewerBinding;

import java.io.OutputStream;

public class MediaViewerActivity extends AppCompatActivity {

    ActivityMediaViewerBinding binding;
    private String imageUri;
    private int messageId = -1; // ID to delete from Room DB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMediaViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.previewToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageUri = getIntent().getStringExtra("image_uri");
        messageId = getIntent().getIntExtra("message_id", -1);
        Log.e("==sana", "onCreate: "+imageUri );
        if (imageUri != null) {
            Glide.with(this).load(Uri.parse(imageUri)).into(binding.fullscreenImage);
        } else {
            Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_preview_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        int itemId = item.getItemId();
        if (itemId == R.id.action_download) {
            downloadImage();
            return true;
        } else if (itemId == R.id.action_share) {
            shareImage();
            return true;
        } /*else if (itemId == R.id.action_delete) {
            deleteImage();
            return true;
        } */else if (itemId == R.id.action_info) {
            showImageInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //download path : Internal Storage > Pictures > GamerCommunity
    private void downloadImage() {
        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        saveImageToGallery(resource);
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                    }
                });
    }

    private void saveImageToGallery(Bitmap bitmap) {
        try {
            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GamerCommunity");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new Exception("Failed to create MediaStore entry");

            OutputStream out = getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);

            Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(imageUri));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share image via"));
        } catch (Exception e) {
            Toast.makeText(this, "Share failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteImage() {
        try {
            // Delete from MediaStore
            getContentResolver().delete(Uri.parse(imageUri), null, null);

            // Delete message from Room DB
            if (messageId != -1) {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                    ChatMessageDao dao = db.chatMessageDao();
                    dao.getMessageById(String.valueOf(messageId));
                }).start();
            }

            Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
        }
    }


    private void showImageInfo() {
        Toast.makeText(this, "URI: " + imageUri.toString(), Toast.LENGTH_LONG).show();
    }

}