package com.sandraygonzalo.bookhub.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sandraygonzalo.bookhub.home.HomeActivity;
import com.sandraygonzalo.bookhub.R;

public class VerifyCodeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText codeEditText;
    private Button verifyButton;
    private String verificationId, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        mAuth = FirebaseAuth.getInstance();

        codeEditText = findViewById(R.id.codeEditText);
        verifyButton = findViewById(R.id.verifyButton);

        verificationId = getIntent().getStringExtra("verificationId");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        verifyButton.setOnClickListener(v -> verificarCodigo());

        // Estética: barra de estado transparente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void verificarCodigo() {
        String code = codeEditText.getText().toString().trim();

        if (verificationId == null || code.isEmpty()) {
            Toast.makeText(this, "Introduce el código", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // Validamos solo el código, no cambiamos de usuario
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    // Vuelve a loguearse con email/password para mantener sesión del usuario real
                    mAuth.signOut();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                startActivity(new Intent(VerifyCodeActivity.this, HomeActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error tras verificación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Código incorrecto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
