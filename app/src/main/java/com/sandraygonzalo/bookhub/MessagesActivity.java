package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
            finish(); // o redirige a LoginActivity
            return;
        }
        currentUserId = user.getUid();


        loadUserChats();

        // Menú inferior
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
                        Timestamp lastMessageAt = null;

                        if (rawTimestamp instanceof Timestamp) {
                            lastMessageAt = (Timestamp) rawTimestamp;
                        } else if (rawTimestamp instanceof Long) {
                            lastMessageAt = new Timestamp(new Date((Long) rawTimestamp));
                        } else {
                            lastMessageAt = Timestamp.now();
                        }

                        // ✅ 1. Obtener el UID del otro usuario
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

                        // ✅ 2. Obtener nombre del usuario y portada del libro
                        db.collection("users").document(otherUserId).get()
                                .addOnSuccessListener(userDoc -> {
                                    String otherUsername = userDoc.getString("username");

                                    // Buscar en exchanges el ID del libro
                                    db.collection("exchanges").document(exchangeId).get()
                                            .addOnSuccessListener(exchangeDoc -> {
                                                String userBookId = exchangeDoc.getString("bookRequestedId"); // o bookOfferedId, según lógica

                                                // Obtener imagen de portada del libro
                                                db.collection("userBooks").document(userBookId).get()
                                                        .addOnSuccessListener(bookDoc -> {
                                                            String bookCoverUrl = bookDoc.getString("coverImage");

                                                            // Crear ChatPreview completo
                                                            ChatPreview preview = new ChatPreview(
                                                                    exchangeId,
                                                                    finalOtherUserId,              // ✅ this is otherUserId
                                                                    otherUsername,                 // ✅ username
                                                                    finalLastMessage != null ? finalLastMessage : "",
                                                                    finalLastMessageAt != null ? finalLastMessageAt : Timestamp.now(),
                                                                    bookCoverUrl != null ? bookCoverUrl : ""
                                                            );

                                                            chatList.add(preview);

                                                            // Ordenar y actualizar adapter
                                                            chatList.sort((c1, c2) -> c2.getLastMessageAt().compareTo(c1.getLastMessageAt()));

                                                            if (adapter == null) {
                                                                adapter = new ChatPreviewAdapter(chatList, MessagesActivity.this, preview1 -> {
                                                                    Log.d("CHAT_DEBUG", "exchangeId: " + preview1.getExchangeId());
                                                                    Log.d("CHAT_DEBUG", "otherUserId: " + preview1.getOtherUserId());
                                                                    Intent intent = new Intent(MessagesActivity.this, ChatActivity.class);
                                                                    intent.putExtra("chatId", preview1.getExchangeId());           // 🔁 mismo que exchangeId
                                                                    intent.putExtra("otherUserId", preview1.getOtherUserId());     // ✅ el UID real

                                                                    intent.putExtra("bookCoverUrl", preview1.getBookCoverUrl());
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

