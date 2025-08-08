package com.iceshardgames.gamercommunity.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;

public class ScreenshotAdapter extends RecyclerView.Adapter<ScreenshotAdapter.ScreenshotViewHolder> {

    private ArrayList<Integer> screenshotList;

    public ScreenshotAdapter(ArrayList<Integer> screenshotList) {
        this.screenshotList = screenshotList;
    }

    @NonNull
    @Override
    public ScreenshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_screenshot, parent, false);
        return new ScreenshotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreenshotViewHolder holder, int position) {
        holder.screenshotImage.setImageResource(screenshotList.get(position));
    }

    @Override
    public int getItemCount() {
        return screenshotList.size();
    }

    public static class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        ImageView screenshotImage;

        public ScreenshotViewHolder(@NonNull View itemView) {
            super(itemView);
            screenshotImage = itemView.findViewById(R.id.screenshotImage);
        }
    }
}
