package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private ChatPreviewAdapter adapter;
    private List<ChatPreview> chatList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = user.getUid();

        loadUserChats();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_messages);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_messages) return true;

            if (itemId == R.id.nav_home) {
                startActivityWithTransition(HomeActivity.class);
                return true;
            }

            if (itemId == R.id.nav_profile) {
                startActivityWithTransition(ProfileActivity.class);
                return true;
            }

            if (itemId == R.id.nav_favorites) {
                startActivityWithTransition(FavoritesActivity.class);
                return true;
            }

            return false;
        });

        // Estilo transparente para status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void startActivityWithTransition(Class<?> target) {
        Intent intent = new Intent(MessagesActivity.this, target);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void loadUserChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    chatList.clear();

                    for (QueryDocumentSnapshot chatDoc : querySnapshot) {
                        String exchangeId = chatDoc.getId();
                        List<String> participants = (List<String>) chatDoc.get("participants");
                        String lastMessage = chatDoc.getString("lastMessage");
                        Object rawTimestamp = chatDoc.get("lastMessageAt");
                        Timestamp lastMessageAt;

                        if (rawTimestamp instanceof Timestamp) {
                            lastMessageAt = (Timestamp) rawTimestamp;
                        } else if (rawTimestamp instanceof Long) {
                            lastMessageAt = new Timestamp(new Date((Long) rawTimestamp));
                        } else {
                            lastMessageAt = Timestamp.now();
                        }

                        String otherUserId = "";
                        for (String id : participants) {
                            if (!id.equals(currentUserId)) {
                                otherUserId = id;
                                break;
                            }
                        }

                        String finalOtherUserId = otherUserId;
                        String finalLastMessage = lastMessage;
                        Timestamp finalLastMessageAt = lastMessageAt;

                        db.collection("users").document(otherUserId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String otherUsername = userDoc.getString("username");

                                    db.collection("exchanges").document(exchangeId).get()
                                            .addOnSuccessListener(exchangeDoc -> {
                                                String userBookId = exchangeDoc.getString("bookRequestedId"); // o "bookOfferedId"

                                                db.collection("userBooks").document(userBookId).get()
                                                        .addOnSuccessListener(bookDoc -> {
                                                            String bookCoverUrl = bookDoc.getString("coverImage");
                                                            String title = bookDoc.getString("title");
                                                            String author = bookDoc.getString("author");

                                                            ChatPreview preview = new ChatPreview(
                                                                    exchangeId,
                                                                    finalOtherUserId,
                                                                    otherUsername,
                                                                    finalLastMessage != null ? finalLastMessage : "",
                                                                    finalLastMessageAt != null ? finalLastMessageAt : Timestamp.now(),
                                                                    bookCoverUrl != null ? bookCoverUrl : ""
                                                            );

                                                            chatList.add(preview);
                                                            chatList.sort((c1, c2) -> c2.getLastMessageAt().compareTo(c1.getLastMessageAt()));

                                                            if (adapter == null) {
                                                                adapter = new ChatPreviewAdapter(chatList, MessagesActivity.this, preview1 -> {
                                                                    Intent intent = new Intent(MessagesActivity.this, ChatActivity.class);
                                                                    intent.putExtra("chatId", preview1.getExchangeId());
                                                                    intent.putExtra("otherUserId", preview1.getOtherUserId());
                                                                    intent.putExtra("bookCoverUrl", preview1.getBookCoverUrl());
                                                                    intent.putExtra("title", title != null ? title : "");
                                                                    intent.putExtra("author", author != null ? author : "");
                                                                    startActivity(intent);
                                                                });
                                                                messagesRecyclerView.setAdapter(adapter);
                                                            } else {
                                                                adapter.notifyDataSetChanged();
                                                            }
                                                        });
                                            });
                                });
                    }
                });
    }
}
