package com.sandraygonzalo.bookhub.profiles;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sandraygonzalo.bookhub.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OtherUserActivity extends AppCompatActivity {

    private static final String TAG = "OtherUserActivity";
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");

        Log.d(TAG, "UserID recibido: " + userId);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "ID de usuario no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView imgProfile = findViewById(R.id.avatarImageView);
        TextView userFullName = findViewById(R.id.userFullName);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<UserBook> bookList = new ArrayList<>();
        UserBooksAdapter adapter = new UserBooksAdapter(bookList, this, false);
        recyclerView.setAdapter(adapter);

        TextView statValue1 = findViewById(R.id.statValue1);
        TextView statLabel1 = findViewById(R.id.statLabel1);
        TextView statValue2 = findViewById(R.id.statValue2);
        TextView statLabel2 = findViewById(R.id.statLabel2);
        TextView statValue3 = findViewById(R.id.statValue3);
        TextView statLabel3 = findViewById(R.id.statLabel3);

        statLabel1.setText("Intercambios");
        statLabel2.setText("Valoración");
        statLabel3.setText("Libros");

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    Log.d(TAG, "Usuario encontrado: " + doc.getId());
                    String imageUrl = doc.getString("profilePicture");
                    String firstName = doc.getString("firstName");
                    String lastName = doc.getString("lastName");
                    Double rating = doc.getDouble("rating.average");

                    Log.d(TAG, "Nombre: " + firstName + " " + lastName + " - Rating: " + rating);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).into(imgProfile);
                    } else {
                        imgProfile.setImageResource(R.drawable.ic_user_avatar);
                    }

                    userFullName.setText((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                    statValue2.setText(String.format(Locale.US, "%.1f/5", rating != null ? rating : 0.0));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error obteniendo usuario", e));

        db.collection("userBooks")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d(TAG, "Libros encontrados: " + snapshot.size());
                    statValue3.setText(String.valueOf(snapshot.size()));

                    for (QueryDocumentSnapshot doc : snapshot) {
                        try {
                            UserBook book = doc.toObject(UserBook.class);
                            book.setId(doc.getId());

                            Log.d(TAG, "Libro cargado: " + book.getTitle() + " - " + book.getAuthor());

                            bookList.add(book);
                        } catch (Exception e) {
                            Log.e(TAG, "Error convirtiendo libro: " + doc.getId(), e);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter notificado. Total libros en lista: " + bookList.size());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error obteniendo libros del usuario", e));

        db.collection("exchanges")
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int completed = 0;
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String user1 = doc.getString("user1Id");
                        String user2 = doc.getString("user2Id");

                        if (userId.equals(user1) || userId.equals(user2)) {
                            completed++;
                        }
                    }
                    Log.d(TAG, "Intercambios completados por el usuario: " + completed);
                    statValue1.setText(String.valueOf(completed));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error obteniendo intercambios", e));

        // Estilo transparente para status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

}
