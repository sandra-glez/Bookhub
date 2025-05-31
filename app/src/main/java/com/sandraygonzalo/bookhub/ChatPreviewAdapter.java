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

import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatPreviewAdapter extends RecyclerView.Adapter<ChatPreviewAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatPreview chat);
    }

    private List<ChatPreview> chatList;
    private Context context;
    private OnChatClickListener listener;

    public ChatPreviewAdapter(List<ChatPreview> chatList, Context context, OnChatClickListener listener) {
        this.chatList = chatList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_preview, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatPreview chat = chatList.get(position);

        holder.chatUsername.setText(chat.getUsername());
        holder.chatLastMessage.setText(chat.getLastMessage());

        Timestamp timestamp = chat.getLastMessageAt();
        if (timestamp != null) {
            String formattedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate());
            holder.chatTime.setText(formattedTime);
        }

        Picasso.get().load(chat.getBookCoverUrl()).placeholder(R.drawable.placeholder).into(holder.chatBookCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChatClick(chat);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView chatBookCover;
        TextView chatUsername, chatLastMessage, chatTime;

        ChatViewHolder(View itemView) {
            super(itemView);
            chatBookCover = itemView.findViewById(R.id.chatBookCover);
            chatUsername = itemView.findViewById(R.id.chatUsername);
            chatLastMessage = itemView.findViewById(R.id.chatLastMessage);
            chatTime = itemView.findViewById(R.id.chatTime);
        }
    }
}

