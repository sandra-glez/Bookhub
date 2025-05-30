package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ChatPreview chat = doc.toObject(ChatPreview.class);
                        if (chat != null) {
                            chatList.add(chat);
                        }
                    }

                    // Ordenar por último mensaje (más reciente primero)
                    chatList.sort((c1, c2) -> {
                        Timestamp t1 = c1.getLastMessageAt();
                        Timestamp t2 = c2.getLastMessageAt();
                        return t2.compareTo(t1);
                    });

                    // Inicializar y asignar el adapter
                    adapter = new ChatPreviewAdapter(chatList, MessagesActivity.this, new ChatPreviewAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(ChatPreview preview) {
                            Intent intent = new Intent(MessagesActivity.this, ChatActivity.class);
                            intent.putExtra("exchangeId", preview.getExchangeId());
                            intent.putExtra("otherUserName", preview.getUsername());
                            intent.putExtra("bookCoverUrl", preview.getBookCoverUrl());
                            startActivity(intent);
                        }
                    });

                    // ✅ ¡Aquí estaba el fallo!
                    messagesRecyclerView.setAdapter(adapter);
                });
    }

}

