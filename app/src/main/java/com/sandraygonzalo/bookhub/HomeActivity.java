package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.books_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookList = new ArrayList<>();
        adapter = new BookAdapter(bookList);
        recyclerView.setAdapter(adapter);

        userAvatar = findViewById(R.id.user_avatar);
        userNameText = findViewById(R.id.user_name);
        userLocationText = findViewById(R.id.user_location);
        loadUserProfile();


        fetchUserBooks();
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
                        bookList.add(book);
                    }

                    BookAdapter adapter = new BookAdapter(bookList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener libros", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error", e);
                });
    }

}
