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
import java.util.Locale;

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

        // STATS
        TextView userFullName = findViewById(R.id.userFullName);
        TextView statValue1 = findViewById(R.id.statValue1); // Intercambios
        TextView statLabel1 = findViewById(R.id.statLabel1);
        TextView statValue2 = findViewById(R.id.statValue2); // Rating
        TextView statLabel2 = findViewById(R.id.statLabel2);
        TextView statValue3 = findViewById(R.id.statValue3); // Libros
        TextView statLabel3 = findViewById(R.id.statLabel3);

// Etiquetas
        statLabel1.setText("Intercambios");
        statLabel2.setText("Valoración");
        statLabel3.setText("Libros");

// 1. Datos del usuario
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Mostrar nombre y apellido (los tienes como "firstName" y "lastName")
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        userFullName.setText((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));

                        // Rating
                        Double avgRating = documentSnapshot.getDouble("rating.average");
                        statValue2.setText(String.format(Locale.US, "%.1f/5", avgRating != null ? avgRating : 0.0));
                    }
                })
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Error al cargar perfil", e));

// 2. Libros que ha subido
        db.collection("userBooks")
                .whereEqualTo("ownerId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int bookCount = querySnapshot.size();
                    statValue3.setText(String.valueOf(bookCount));
                })
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Error al contar libros", e));

// 3. Intercambios completados
        db.collection("exchanges")
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int completedExchanges = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String user1Id = doc.getString("user1Id");
                        String user2Id = doc.getString("user2Id");
                        if (uid.equals(user1Id) || uid.equals(user2Id)) {
                            completedExchanges++;
                        }
                    }
                    statValue1.setText(String.valueOf(completedExchanges));
                })
                .addOnFailureListener(e -> Log.e("ProfileActivity", "Error al contar intercambios", e));

    }
    private void startActivityWithTransition(Class<?> target) {
        Intent intent = new Intent(ProfileActivity.this, target);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
