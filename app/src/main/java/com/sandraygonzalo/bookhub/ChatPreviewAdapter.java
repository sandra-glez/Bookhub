package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import android.content.Context;
import com.google.type.Date;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatPreviewAdapter extends RecyclerView.Adapter<ChatPreviewAdapter.PreviewViewHolder> {

    private final List<ChatPreview> previewList;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ChatPreview preview);
    }

    public ChatPreviewAdapter(List<ChatPreview> previewList, Context context, OnItemClickListener listener) {
        this.previewList = previewList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_preview, parent, false);
        return new PreviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
        ChatPreview preview = previewList.get(position);

        holder.usernameText.setText(preview.getUsername());
        holder.lastMessageText.setText(preview.getLastMessage());

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(preview.getLastMessageAt().toDate());
        holder.timeText.setText(time);

        Glide.with(context)
                .load(preview.getBookCoverUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.coverImage);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(preview));
    }

    @Override
    public int getItemCount() {
        return previewList.size();
    }

    static class PreviewViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImage;
        TextView usernameText, lastMessageText, timeText;

        public PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.chatBookCover);
            usernameText = itemView.findViewById(R.id.chatUsername);
            lastMessageText = itemView.findViewById(R.id.chatLastMessage);
            timeText = itemView.findViewById(R.id.chatTime);
        }
    }
}

