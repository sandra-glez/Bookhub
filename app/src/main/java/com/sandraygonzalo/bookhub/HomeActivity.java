package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText searchInput;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<UserBook> bookList = new ArrayList<>();
    private String currentUserId;
    private List<String> userPreferences = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        searchInput = findViewById(R.id.search_input);
        recyclerView = findViewById(R.id.books_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(bookList);
        recyclerView.setAdapter(bookAdapter);

        cargarDatosUsuario();

        // Buscar al escribir
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    mostrarLibrosRecomendados();
                } else {
                    buscarLibrosYUsuarios(s.toString());
                }
            }
        });
    }

    private void cargarDatosUsuario() {
        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        userPreferences = (List<String>) document.get("preferences");
                        mostrarLibrosRecomendados();
                    }
                });
    }

    private void mostrarLibrosRecomendados() {
        db.collection("userBooks")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(query -> {
                    bookList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String genre = doc.getString("genre");
                        if (userPreferences.contains(genre)) {
                            UserBook book = doc.toObject(UserBook.class);
                            if (!doc.getString("ownerId").equals(currentUserId)) {
                                book.setId(doc.getId());
                                bookList.add(book);
                            }
                        }
                    }
                    bookAdapter.notifyDataSetChanged();
                });
    }

    private void buscarLibrosYUsuarios(String keyword) {
        bookList.clear();

        // Búsqueda en libros
        db.collection("userBooks")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        String title = doc.getString("title");
                        if (title != null && title.toLowerCase().contains(keyword.toLowerCase())) {
                            UserBook book = doc.toObject(UserBook.class);
                            book.setId(doc.getId());
                            bookList.add(book);
                        }
                    }

                    // Búsqueda en usuarios (username)
                    db.collection("users")
                            .get()
                            .addOnSuccessListener(users -> {
                                for (QueryDocumentSnapshot userDoc : users) {
                                    String username = userDoc.getString("username");
                                    if (username != null && username.toLowerCase().contains(keyword.toLowerCase())) {
                                        // Mostrar en forma de "libro falso" o sugerencia de usuario
                                        UserBook fake = new UserBook();
                                        fake.setTitle("Usuario: @" + username);
                                        fake.setAuthor(userDoc.getString("firstName"));
                                        fake.setDescription("Perfil encontrado");
                                        bookList.add(fake);
                                    }
                                }
                                bookAdapter.notifyDataSetChanged();
                            });
                });
    }
}

