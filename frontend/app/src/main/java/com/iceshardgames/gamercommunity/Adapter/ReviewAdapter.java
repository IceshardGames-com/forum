package com.iceshardgames.gamercommunity.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.ReviewModel;
import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private ArrayList<ReviewModel> reviewList;

    public ReviewAdapter(ArrayList<ReviewModel> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel model = reviewList.get(position);
        holder.username.setText(model.getUsername());
        holder.title.setText(model.getTitle());
        holder.description.setText(model.getDescription());
        holder.time.setText(model.getTime());
        holder.ratingBar.setRating(model.getRating());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView username, title, description, time;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.reviewUsername);
            title = itemView.findViewById(R.id.reviewTitle);
            description = itemView.findViewById(R.id.reviewDescription);
            time = itemView.findViewById(R.id.reviewTime);
            ratingBar = itemView.findViewById(R.id.reviewRatingBar);
        }
    }
}
