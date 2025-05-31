package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddBookActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView bookCoverImage;
    private Uri imageUri;

    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText titleEditText, authorEditText, descriptionEditText, conditionEditText;
    private Button saveBookButton, selectImageButton;

    private ChipGroup chipGroupGenres;
    private List<String> selectedGenres = new ArrayList<>();

    private String[] genres = {
            "Acción", "Aventura", "Biografía", "Ciencia", "Ciencia ficción",
            "Clásicos", "Comedia", "Contemporáneo", "Crimen", "Cuento",
            "Distopía", "Drama", "Educación", "Erótico", "Fantasía",
            "Ficción histórica", "Filosofía", "Graphic novel", "Historia", "Infantil",
            "Juvenil", "Misterio", "Paranormal", "Poesía", "Realismo mágico",
            "Romance", "Suspense", "Terror", "Thriller", "Otros"
    };
    private ChipGroup chipGroupCondition;
    private String[] bookConditions = {
            "Como nuevo", "Muy buen estado", "Buen estado", "Usado pero decente", "Tocado pero usable"
    };
    private String selectedCondition = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        bookCoverImage = findViewById(R.id.bookCoverImage);
        selectImageButton = findViewById(R.id.selectImageButton);
        titleEditText = findViewById(R.id.titleEditText);
        authorEditText = findViewById(R.id.authorEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        saveBookButton = findViewById(R.id.saveBookButton);
        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        chipGroupCondition = findViewById(R.id.chipGroupCondition);
        setupConditionChips();


        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupGenreChips();

        selectImageButton.setOnClickListener(v -> openFileChooser());

        saveBookButton.setOnClickListener(v -> {
            if (imageUri == null) {
                Toast.makeText(this, "Selecciona una imagen de portada", Toast.LENGTH_SHORT).show();
            } else if (selectedGenres.isEmpty()) {
                Toast.makeText(this, "Selecciona al menos un género", Toast.LENGTH_SHORT).show();
            } else {
                uploadImageAndSaveBook();
            }
        });
    }

    private void setupConditionChips() {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (String condition : bookConditions) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_choice, chipGroupCondition, false);
            chip.setText(condition);

            chip.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    selectedCondition = condition;
                }
            });

            chipGroupCondition.addView(chip);
        }
    }


    private void setupGenreChips() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String genre : genres) {
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_choice, chipGroupGenres, false);
            chip.setText(genre);
            chip.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    if (selectedGenres.size() >= 4) {
                        chip.setChecked(false);
                        Toast.makeText(this, "Solo puedes seleccionar hasta 4 géneros", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedGenres.add(genre);
                    }
                } else {
                    selectedGenres.remove(genre);
                }
            });
            chipGroupGenres.addView(chip);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            bookCoverImage.setImageURI(imageUri);
        }
    }

    private void uploadImageAndSaveBook() {
        String fileName = UUID.randomUUID().toString();
        StorageReference fileRef = storage.getReference("book_covers").child(fileName);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveBookToFirestore(uri.toString());
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show());
    }

    private void saveBookToFirestore(String imageUrl) {
        String uid = auth.getCurrentUser().getUid();

        String title = titleEditText.getText().toString().trim();
        String author = authorEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String condition = selectedCondition;


        Map<String, Object> bookData = new HashMap<>();
        bookData.put("title", title);
        bookData.put("author", author);
        bookData.put("description", description);
        bookData.put("condition", condition);
        bookData.put("genres", selectedGenres); // ahora usamos selectedGenres directamente
        bookData.put("coverImage", imageUrl);
        bookData.put("ownerId", uid);
        bookData.put("available", true);
        bookData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("userBooks")
                .add(bookData)
                .addOnSuccessListener(documentReference -> {
                    String docId = documentReference.getId();
                    documentReference.update("id", docId) // importante para que funcione el botón de eliminar luego
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Libro guardado", Toast.LENGTH_SHORT).show();
                                finish(); // volver al perfil
                            });
                });

    }
}

