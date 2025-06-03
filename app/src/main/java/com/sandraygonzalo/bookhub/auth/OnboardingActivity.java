package com.sandraygonzalo.bookhub.auth;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.sandraygonzalo.bookhub.R;

import android.Manifest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
            "Acción", "Aventura", "Biografía", "Ciencia", "Ciencia ficción",
            "Clásicos", "Comedia", "Contemporáneo", "Crimen", "Cuento",
            "Distopía", "Drama", "Educación", "Erótico", "Fantasía",
            "Ficción histórica", "Filosofía", "Novela", "Historia", "Infantil",
            "Juvenil", "Misterio", "Paranormal", "Poesía", "Realismo mágico",
            "Romance", "Suspense", "Terror", "Thriller", "Otros"

};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        usernameInput = findViewById(R.id.usernameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        locationInput = findViewById(R.id.locationInput);
        bioInput = findViewById(R.id.bioInput);
        profileImage = findViewById(R.id.profileImage);
        genreChipGroup = findViewById(R.id.genreChipGroup);
        Button saveButton = findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profilePictures");

        // Poblar ChipGroup con géneros
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String genre : genres) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_choice, genreChipGroup, false);
            chip.setText(genre);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int selectedCount = 0;
                for (int i = 0; i < genreChipGroup.getChildCount(); i++) {
                    Chip c = (Chip) genreChipGroup.getChildAt(i);
                    if (c.isChecked()) selectedCount++;
                }
                if (selectedCount > 4) {
                    chip.setChecked(false);
                    Toast.makeText(this, "Solo puedes seleccionar hasta 4 géneros", Toast.LENGTH_SHORT).show();
                }
            });

            genreChipGroup.addView(chip); // ✅ Solo una vez
        }



        // Selección de imagen
        profileImage.setOnClickListener(v -> openGallery());

        saveButton.setOnClickListener(v -> saveUserData());

        // PERMISOS DE UBICACIÓN
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
        } else {
            getLocation(); // ya tiene permiso
        }
        locationInput.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
            } else {
                getLocation(); // ya tiene permiso
            }
        });


    }
    // UBICACIÓN
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1002 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation(); // permiso aceptado
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            return;
        }

        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            fusedClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(
                                        location.getLatitude(), location.getLongitude(), 1);
                                if (!addresses.isEmpty()) {
                                    String city = addresses.get(0).getLocality();
                                    locationInput.setText(city);
                                } else {
                                    Toast.makeText(this, "No se pudo obtener la ciudad", Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error de seguridad al obtener ubicación", Toast.LENGTH_SHORT).show();
        }
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String username = usernameInput.getText().toString().trim();

        if (username.isEmpty()) {
            usernameInput.setError("Campo obligatorio");
            return;
        }

        List<String> selectedGenres = new ArrayList<>();
        for (int i = 0; i < genreChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) genreChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedGenres.add(chip.getText().toString());
            }
        }

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("username", username);
        userUpdates.put("firstName", firstNameInput.getText().toString().trim());
        userUpdates.put("lastName", lastNameInput.getText().toString().trim());
        userUpdates.put("location", locationInput.getText().toString().trim());
        userUpdates.put("bio", bioInput.getText().toString().trim());
        userUpdates.put("preferences", selectedGenres);

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(uid + ".jpg");
            fileRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return fileRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        userUpdates.put("profilePicture", uri.toString());
                        updateFirestore(uid, userUpdates);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error subiendo imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            updateFirestore(uid, userUpdates);
        }
    }


    private void updateFirestore(String uid, Map<String, Object> userUpdates) {
        db.collection("users").document(uid)
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Onboarding completo", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut(); // ← importante si ya está logueado
                    Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // limpia el backstack
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

