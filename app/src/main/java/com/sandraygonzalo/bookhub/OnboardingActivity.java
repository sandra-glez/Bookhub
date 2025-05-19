package com.sandraygonzalo.bookhub;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity {

    private EditText usernameInput, firstNameInput, lastNameInput, locationInput, bioInput;
    private ImageView profileImage;
    private ChipGroup genreChipGroup;
    private Uri imageUri = null;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    private String[] genres = {
            "Fantasía", "Ciencia ficción", "Romance", "Thriller",
            "No ficción", "Misterio", "Clásicos", "Histórico",
            "Terror", "Biografía", "Juvenil", "Poesía"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        usernameInput = findViewById(R.id.usernameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        locationInput = findViewById(R.id.locationInput);
//        bioInput = findViewById(R.id.bioInput);
        profileImage = findViewById(R.id.profileImage);
        genreChipGroup = findViewById(R.id.genreChipGroup);
        Button saveButton = findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profilePictures");

        // Poblar ChipGroup con géneros
        for (String genre : genres) {
            Chip chip = new Chip(this);
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background); // Opcional
            chip.setTextColor(getResources().getColor(R.color.chip_text)); // Opcional
            genreChipGroup.addView(chip);
        }

        // Selección de imagen
        profileImage.setOnClickListener(v -> openGallery());

        saveButton.setOnClickListener(v -> saveUserData());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void saveUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        String username = usernameInput.getText().toString().trim();

        if (username.isEmpty()) {
            usernameInput.setError("Campo obligatorio");
            return;
        }

        // Obtener géneros seleccionados
        List<String> selectedGenres = new ArrayList<>();
        for (int i = 0; i < genreChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) genreChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedGenres.add(chip.getText().toString());
            }
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("firstName", firstNameInput.getText().toString().trim());
        userData.put("lastName", lastNameInput.getText().toString().trim());
        userData.put("location", locationInput.getText().toString().trim());
        userData.put("bio", bioInput.getText().toString().trim());
        userData.put("preferences", selectedGenres);
        userData.put("profilePicture", "");
        userData.put("rating", new HashMap<String, Object>() {{
            put("average", 0.0);
            put("totalExchanges", 0);
        }});
        userData.put("booksAvailable", new ArrayList<String>());
        userData.put("exchangeHistory", new ArrayList<String>());
        userData.put("favorites", new ArrayList<String>());
        userData.put("notificationsEnabled", true);

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(uid + ".jpg");
            fileRef.putFile(imageUri)
                    .continueWithTask(task -> fileRef.getDownloadUrl())
                    .addOnSuccessListener(uri -> {
                        userData.put("profilePicture", uri.toString());
                        saveToFirestore(uid, userData);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error subiendo imagen", Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveToFirestore(uid, userData);
        }
    }

    private void saveToFirestore(String uid, Map<String, Object> userData) {
        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Onboarding completo", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OnboardingActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

