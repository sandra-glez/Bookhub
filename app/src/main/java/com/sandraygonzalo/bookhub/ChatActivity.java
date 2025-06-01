package com.sandraygonzalo.bookhub;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;
import android.widget.*;

public class ChatActivity extends AppCompatActivity {

    private Button btnRequestExchange;
    private FirebaseFirestore db;
    private String currentUserId;
    private String chatId;
    private String otherUserId;

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView chatTitle;
    private ImageView bookCover;
    private TextView exchangeStatusText; // NUEVO: para mostrar el estado "✅ Intercambio completado"

    private List<Message> messageList;
    private ChatAdapter chatAdapter;
    private CollectionReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");

        if (chatId == null || otherUserId == null) {
            Toast.makeText(this, "Datos del chat no disponibles", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        btnRequestExchange = findViewById(R.id.btnRequestExchange);
        chatTitle = findViewById(R.id.bookTitleHeader);
        bookCover = findViewById(R.id.bookCoverSmall);
        exchangeStatusText = findViewById(R.id.exchangeStatusText); // ✅ nuevo

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        messagesRef = db.collection("chats").document(chatId).collection("messages");

        btnRequestExchange.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ExchangeAgreementActivity.class);
            intent.putExtra("exchangeId", chatId);
            intent.putExtra("otherUserId", otherUserId);
            startActivity(intent);
        });

        loadMessages();
        setupSendButton();
        loadOtherUserInfo();
        loadBookInfo();        // 🔧 Corrige el fallo desde favoritos
        loadExchangeStatus(); // ✅ Muestra mensaje si está acordado
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExchangeStatus(); // por si vuelve del acuerdo
    }

    private void loadExchangeStatus() {
        db.collection("exchanges").document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String status = doc.getString("status");
                    if ("agreed".equals(status)) {
                        btnRequestExchange.setVisibility(View.GONE);
                        exchangeStatusText.setText("✅ Intercambio completado");
                        exchangeStatusText.setVisibility(View.VISIBLE);
                    } else {
                        exchangeStatusText.setVisibility(View.GONE);
                        btnRequestExchange.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadBookInfo() {
        db.collection("exchanges").document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String bookRequestedId = doc.getString("bookRequestedId");
                    String bookOfferedId = doc.getString("bookOfferedId");
                    String user1Id = doc.getString("user1Id");

                    if (bookRequestedId == null || bookOfferedId == null || user1Id == null)
                        return;

                    String incomingBookId = currentUserId.equals(user1Id) ? bookRequestedId : bookOfferedId;

                    db.collection("userBooks").document(incomingBookId)
                            .get()
                            .addOnSuccessListener(bookDoc -> {
                                if (bookDoc.exists()) {
                                    chatTitle.setText(bookDoc.getString("title"));
                                    String coverUrl = bookDoc.getString("coverImage");
                                    if (coverUrl != null && !coverUrl.isEmpty()) {
                                        Glide.with(this)
                                                .load(coverUrl)
                                                .placeholder(R.drawable.placeholder)
                                                .into(bookCover);
                                    } else {
                                        bookCover.setImageResource(R.drawable.placeholder);
                                    }
                                }
                            });
                });
    }

    private void loadMessages() {
        messagesRef.orderBy("sentAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (snapshots == null || snapshots.isEmpty()) return;
                    messageList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Message msg = doc.toObject(Message.class);
                        messageList.add(msg);
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (content.isEmpty()) return;

            Message msg = new Message(currentUserId, content, new Timestamp(new Date()), "text");
            messagesRef.add(msg);
            messageInput.setText("");

            db.collection("chats").document(chatId)
                    .update("lastMessage", content, "lastMessageAt", FieldValue.serverTimestamp());
        });
    }

    private void loadOtherUserInfo() {
        TextView otherUsernameText = findViewById(R.id.otherUsername);
        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        otherUsernameText.setText(username);
                    }
                });
    }
}

