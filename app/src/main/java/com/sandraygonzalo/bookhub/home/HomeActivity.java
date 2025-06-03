package com.sandraygonzalo.bookhub.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sandraygonzalo.bookhub.R;
import com.sandraygonzalo.bookhub.auth.MainActivity;
import com.sandraygonzalo.bookhub.messages.MessagesActivity;
import com.sandraygonzalo.bookhub.profiles.ProfileActivity;
import com.sandraygonzalo.bookhub.profiles.UserBook;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<UserBook> bookList;
    private ImageView userAvatar;
    private TextView userNameText, userLocationText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 🔐 VERIFICACIÓN DE SESIÓN
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, MainActivity.class); // o LoginActivity si lo tienes separado
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // 💡 Detener ejecución del resto de onCreate()
        }
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.books_recycler_view);
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        bookList = new ArrayList<>();
        adapter = new BookAdapter(bookList, this);
        recyclerView.setAdapter(adapter);

        userAvatar = findViewById(R.id.user_avatar);
        userNameText = findViewById(R.id.user_name);
        userLocationText = findViewById(R.id.user_location);
        loadUserProfile();


        fetchUserBooks();
        // BARRA DE ESTADO Y MENÚ
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_favorites) {
                startActivityWithTransition(FavoritesActivity.class);
                return true;
            }

            if (itemId == R.id.nav_home) {
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


        // BUSCADOR

        EditText searchBar = findViewById(R.id.search_bar);
        Spinner genreSpinner = findViewById(R.id.genre_spinner);
        Spinner conditionSpinner = findViewById(R.id.condition_spinner);

// Opciones de filtro
        String[] genres = {
                "Género", // <-- opción por defecto
                "Acción", "Aventura", "Biografía", "Ciencia", "Ciencia ficción",
                "Clásicos", "Comedia", "Contemporáneo", "Crimen", "Cuento",
                "Distopía", "Drama", "Educación", "Erótico", "Fantasía",
                "Ficción histórica", "Filosofía", "Graphic novel", "Historia", "Infantil",
                "Juvenil", "Misterio", "Paranormal", "Poesía", "Realismo mágico",
                "Romance", "Suspense", "Terror", "Thriller", "Otros"
        };

        String[] conditions = {
                "Estado", // <-- opción por defecto
                "Como nuevo", "Muy buen estado", "Buen estado", "Usado pero decente", "Tocado pero usable"
        };


// Adaptadores
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genres);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(genreAdapter);

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conditionSpinner.setAdapter(conditionAdapter);

        searchBar.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String query = searchBar.getText().toString().trim();
                String selectedGenre = genreSpinner.getSelectedItem().toString();
                String selectedCondition = conditionSpinner.getSelectedItem().toString();

                applyFilters(query, selectedGenre, selectedCondition);
                return true;
            }
            return false;
        });



    }
    //BUSCADOR
    private void applyFilters(String query, String genreFilter, String conditionFilter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("userBooks")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<UserBook> filteredList = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot) {
                        UserBook book = doc.toObject(UserBook.class);
                        if (book == null) continue;

                        boolean matchText = query.isEmpty()
                                || containsIgnoreCase(book.getTitle(), query)
                                || containsIgnoreCase(book.getAuthor(), query);

                        boolean matchGenre = genreFilter.equals("Género")
                                || (book.getGenres() != null && book.getGenres().contains(genreFilter));

                        boolean matchCondition = conditionFilter.equals("Estado")
                                || conditionEquals(book.getCondition(), conditionFilter);

                        if (matchText && matchGenre && matchCondition) {
                            filteredList.add(book);
                        }
                    }

                    recyclerView.setAdapter(new BookAdapter(filteredList, this));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al aplicar filtros", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error", e);
                });
    }

    // Helpers
    private boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase().contains(query.toLowerCase());
    }

    private boolean conditionEquals(String a, String b) {
        return a != null && a.equalsIgnoreCase(b);
    }



    private void startActivityWithTransition(Class<?> target) {
        Intent intent = new Intent(HomeActivity.this, target);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        String location = documentSnapshot.getString("location");
                        String profilePictureUrl = documentSnapshot.getString("profilePicture");

                        userNameText.setText("Hola, " + firstName + " " + lastName);
                        userLocationText.setText("📍 " + (location != null ? location : "Sin ubicación"));

                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(profilePictureUrl)
                                    .placeholder(R.drawable.ic_user_avatar)
                                    .circleCrop()
                                    .into(userAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener perfil de usuario", e);
                });
    }


    private void fetchUserBooks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("userBooks")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserBook> bookList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        UserBook book = doc.toObject(UserBook.class);

                        // Validación para evitar libros vacíos o placeholders
                        if (book != null &&
                                book.getTitle() != null && !book.getTitle().trim().isEmpty() &&
                                book.getAuthor() != null && !book.getAuthor().trim().isEmpty()) {
                            book.setId(doc.getId());
                            bookList.add(book);
                        }
                    }

                    BookAdapter adapter = new BookAdapter(bookList, this);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener libros", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error", e);
                });
    }

}
