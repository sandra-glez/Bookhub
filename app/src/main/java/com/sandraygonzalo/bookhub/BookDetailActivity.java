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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Toast;

public class BookDetailActivity extends AppCompatActivity {

    private ImageView bookCoverLarge;
    private TextView bookTitle, bookAuthor, bookCondition, bookDescription, bookLocation;
    private ChipGroup genreChipGroup;
    private ImageButton backButton, favoriteButton;
    private boolean isFavorite = false;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // Inicializar vistas
        bookCoverLarge = findViewById(R.id.bookCoverLarge);
        bookTitle = findViewById(R.id.bookTitle);
        bookAuthor = findViewById(R.id.bookAuthor);
        bookCondition = findViewById(R.id.bookCondition);
        bookDescription = findViewById(R.id.bookDescription);
        bookLocation = findViewById(R.id.bookLocation);
        genreChipGroup = findViewById(R.id.genreChipGroup);
        backButton = findViewById(R.id.backButton);
        favoriteButton = findViewById(R.id.favoriteButton);

        // Por defecto, icono vacío
        favoriteButton.setImageResource(R.drawable.vacio);

        // Inicializar Firebase
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Volver atrás
        backButton.setOnClickListener(v -> finish());

        // Obtener libro
        String bookJson = getIntent().getStringExtra("book");
        UserBook book = new Gson().fromJson(bookJson, UserBook.class);

        if (book == null) {
            Toast.makeText(this, "Error al cargar libro", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar que tenga ID (importante)
        if (book.getId() == null) {
            Toast.makeText(this, "Este libro no tiene ID (book.setId() faltante)", Toast.LENGTH_LONG).show();
            return;
        }

        // Mostrar datos del libro
        Glide.with(this)
                .load(book.getCoverImage())
                .placeholder(R.drawable.placeholder)
                .into(bookCoverLarge);

        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        bookCondition.setText("Estado: " + book.getCondition());
        bookDescription.setText(book.getDescription());

        if (book.getGenres() != null) {
            for (String genre : book.getGenres()) {
                Chip chip = new Chip(this);
                chip.setText(genre);
                chip.setChipBackgroundColorResource(R.color.verde);
                chip.setTextColor(Color.BLACK);
                chip.setClickable(false);
                genreChipGroup.addView(chip);
            }
        }

        // Mostrar ubicación del dueño
        if (book.getOwnerId() != null) {
            db.collection("users").document(book.getOwnerId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String location = documentSnapshot.getString("location");
                        if (location != null && !location.isEmpty()) {
                            bookLocation.setText(location);
                        } else {
                            bookLocation.setText("Ubicación no disponible");
                        }
                    })
                    .addOnFailureListener(e -> bookLocation.setText("Error al cargar ubicación"));
        }

        // Cargar si el libro está en favoritos
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> favorites = (List<String>) snapshot.get("favorites");
                    if (favorites != null && favorites.contains(book.getId())) {
                        isFavorite = true;
                        favoriteButton.setImageResource(R.drawable.lleno);
                    }
                });

        // Manejar clic en el botón de favoritos
        favoriteButton.setOnClickListener(v -> {
            DocumentReference userRef = db.collection("users").document(uid);
            if (!isFavorite) {
                userRef.update("favorites", FieldValue.arrayUnion(book.getId()))
                        .addOnSuccessListener(unused -> {
                            favoriteButton.setImageResource(R.drawable.lleno);
                            isFavorite = true;
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar favorito", Toast.LENGTH_SHORT).show());
            } else {
                userRef.update("favorites", FieldValue.arrayRemove(book.getId()))
                        .addOnSuccessListener(unused -> {
                            favoriteButton.setImageResource(R.drawable.vacio);
                            isFavorite = false;
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar favorito", Toast.LENGTH_SHORT).show());
            }
        });

        // SOLICITAR INTERCAMBIO
        Button requestExchangeButton = findViewById(R.id.requestExchangeButton);

        requestExchangeButton.setOnClickListener(v -> {
            if (book.getId() == null || book.getOwnerId() == null) {
                Toast.makeText(this, "Este libro no tiene información válida", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String currentUserId = auth.getCurrentUser().getUid();
            String requestedBookId = book.getId();
            String ownerId = book.getOwnerId();

            // Crear nuevo intercambio
            DocumentReference exchangeRef = db.collection("exchanges").document();
            String exchangeId = exchangeRef.getId();

            Map<String, Object> exchangeData = new HashMap<>();
            exchangeData.put("user1Id", currentUserId);
            exchangeData.put("user2Id", ownerId);
            exchangeData.put("bookRequestedId", requestedBookId);
            exchangeData.put("bookOfferedId", null); // aún no elegido
            exchangeData.put("user1Confirmed", false);
            exchangeData.put("user2Confirmed", false);
            exchangeData.put("status", "pending");
            exchangeData.put("createdAt", new Timestamp(new Date()));

            // Crear el chat relacionado
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("exchangeId", exchangeId);
            chatData.put("participants", Arrays.asList(currentUserId, ownerId));
            chatData.put("lastMessage", "");
            chatData.put("lastMessageAt", new Timestamp(new Date()));

            // Guardar intercambio y chat en paralelo
            Task<Void> exchangeTask = exchangeRef.set(exchangeData);
            Task<Void> chatTask = db.collection("chats").document(exchangeId).set(chatData);

            Tasks.whenAllSuccess(exchangeTask, chatTask).addOnSuccessListener(unused -> {
                // Abrir el chat tras creación
                Intent intent = new Intent(BookDetailActivity.this, ChatActivity.class);
                intent.putExtra("chatId", exchangeId);
                intent.putExtra("otherUserId", ownerId);
                startActivity(intent);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al crear el intercambio", Toast.LENGTH_SHORT).show();
                Log.e("BookDetail", "Error al crear exchange/chat", e);
            });
        });

        // VER USUARIO
        TextView bookOwnerProfile = findViewById(R.id.viewUserButton);

        bookOwnerProfile.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailActivity.this, OtherUserActivity.class);
            intent.putExtra("userId", book.getOwnerId());
            startActivity(intent);
        });

        // Estilo transparente para status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

    }
}

