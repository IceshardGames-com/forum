package com.iceshardgames.gamercommunity.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.UserSearchResponse;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.ViewHolder> {
    private Context context;
    private List<UserSearchResponse.User> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserSearchResponse.User user);
    }

    public UserSearchAdapter(Context context, List<UserSearchResponse.User> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserSearchResponse.User user = users.get(position);
        holder.username.setText(user.getUsername());
        holder.email.setText(user.getEmail());

        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, email;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.textUsername);
            email = itemView.findViewById(R.id.textEmail);
        }
    }
}

