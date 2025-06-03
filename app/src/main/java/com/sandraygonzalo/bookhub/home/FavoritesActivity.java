package com.sandraygonzalo.bookhub.home;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sandraygonzalo.bookhub.R;
import com.sandraygonzalo.bookhub.messages.MessagesActivity;
import com.sandraygonzalo.bookhub.profiles.ProfileActivity;
import com.sandraygonzalo.bookhub.profiles.UserBook;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<UserBook> favoriteBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView = findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        adapter = new BookAdapter(favoriteBooks, this);
        recyclerView.setAdapter(adapter);

        loadFavoriteBooks();

        // BARRA DE ESTADO Y MENÚ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorites);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_favorites) {
                startActivityWithTransition(FavoritesActivity.class);
                return true;
            }

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

    private void loadFavoriteBooks() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    List<String> favoriteIds = (List<String>) userSnapshot.get("favorites");

                    if (favoriteIds == null || favoriteIds.isEmpty()) {
                        Toast.makeText(this, "Sin libros favoritos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> cleanIds = new ArrayList<>();
                    for (String id : favoriteIds) {
                        if (id != null) cleanIds.add(id);
                    }

                    if (cleanIds.isEmpty()) {
                        Toast.makeText(this, "Favoritos inválidos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("userBooks")
                            .whereIn(FieldPath.documentId(), favoriteIds)
                            .get()
                            .addOnSuccessListener(bookSnapshots -> {
                                favoriteBooks.clear();
                                for (DocumentSnapshot doc : bookSnapshots) {
                                    UserBook book = doc.toObject(UserBook.class);
                                    book.setId(doc.getId());
                                    favoriteBooks.add(book);
                                }
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FavoritesActivity", "Error al cargar libros", e);
                                Toast.makeText(this, "Error al cargar favoritos", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FavoritesActivity", "Error al obtener usuario", e);
                    Toast.makeText(this, "Error al cargar usuario", Toast.LENGTH_SHORT).show();
                });
    }
}

