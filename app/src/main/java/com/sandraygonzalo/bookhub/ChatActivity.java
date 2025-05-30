package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String currentUserId;
    private String exchangeId;
    private String otherUserName;
    private String bookCoverUrl;

    private FirebaseFirestore db;
    private CollectionReference messagesRef;
    private DocumentReference chatRef;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView otherUsernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 🔐 Inicializar datos del Intent
        exchangeId = getIntent().getStringExtra("exchangeId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        bookCoverUrl = getIntent().getStringExtra("bookCoverUrl");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db = FirebaseFirestore.getInstance();
        messagesRef = db.collection("chats").document(exchangeId).collection("messages");
        chatRef = db.collection("chats").document(exchangeId);

        // 🔧 UI
        otherUsernameText = findViewById(R.id.otherUsername);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        otherUsernameText.setText(otherUserName);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // ▶️ Escuchar mensajes nuevos
        listenForMessages();

        // ➤ Enviar mensaje
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void listenForMessages() {
        messagesRef.orderBy("sentAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }

                    chatMessages.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        chatMessages.add(message);
                    }
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
                });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        long timestamp = System.currentTimeMillis();

        // Crear nuevo mensaje
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("content", content);
        messageMap.put("senderId", currentUserId);
        messageMap.put("sentAt", timestamp);
        messageMap.put("type", "text");

        messagesRef.add(messageMap)
                .addOnSuccessListener(documentReference -> {
                    // Actualizar chat principal
                    chatRef.update("lastMessage", content, "lastMessageAt", timestamp);
                    messageInput.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ChatActivity.this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show());
    }
}
