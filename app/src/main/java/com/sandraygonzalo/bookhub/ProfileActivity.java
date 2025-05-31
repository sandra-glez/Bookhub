package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        ImageView imgProfile = findViewById(R.id.avatarImageView);

        FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String imageUrl = documentSnapshot.getString("profilePicture");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).into(imgProfile);
                    } else {
                        imgProfile.setImageResource(R.drawable.ic_user_avatar); // fallback si está vacío
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error cargando foto de perfil", e);
                    imgProfile.setImageResource(R.drawable.ic_user_avatar);
                });


        ImageButton settingsButton = findViewById(R.id.settingsButton);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<UserBook> bookList = new ArrayList<>();
        UserBooksAdapter adapter = new UserBooksAdapter(bookList, this);
        recyclerView.setAdapter(adapter);

        String uid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("userBooks")
                .whereEqualTo("ownerId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserBook book = doc.toObject(UserBook.class);
                        book.setId(doc.getId());
                        bookList.add(book);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Error al cargar libros", e));

        //AÑADIR LIBRO
        Button addBookButton = findViewById(R.id.addBookButton);
        addBookButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddBookActivity.class);
            startActivity(intent);
        });


        // 🔧 Acción del botón de ajustes
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Estilo transparente para status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // MENÚ
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

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
        Intent intent = new Intent(ProfileActivity.this, target);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
