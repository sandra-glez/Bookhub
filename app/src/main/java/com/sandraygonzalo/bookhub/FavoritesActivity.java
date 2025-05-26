package com.sandraygonzalo.bookhub;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesRecyclerView;
    private BookAdapter adapter;
    private List<UserBook> favoriteBooks = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        favoritesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadFavorites();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorites);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_favorites) return true;

            if (itemId == R.id.nav_home) {
                startActivityWithTransition(HomeActivity.class);
                return true;
            }

            if (itemId == R.id.nav_profile) {
                startActivityWithTransition(ProfileActivity.class);
                return true;
            }

            if (itemId == R.id.nav_messages) {
                startActivityWithTransition(MessagesActivity.class);
                return true;
            }

            return false;
        });
    }

    private void startActivityWithTransition(Class<?> target) {
        Intent intent = new Intent(FavoritesActivity.this, target);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadFavorites() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(document -> {
                    List<String> favoriteIds = (List<String>) document.get("favorites");
                    if (favoriteIds == null) return;

                    db.collection("userBooks").get().addOnSuccessListener(querySnapshot -> {
                        favoriteBooks.clear();
                        for (DocumentSnapshot doc : querySnapshot) {
                            UserBook book = doc.toObject(UserBook.class);
                            if (book != null && favoriteIds.contains(doc.getId())) {
                                favoriteBooks.add(book);
                            }
                        }
                        adapter = new BookAdapter(favoriteBooks);
                        favoritesRecyclerView.setAdapter(adapter);
                    });
                });
    }
}
