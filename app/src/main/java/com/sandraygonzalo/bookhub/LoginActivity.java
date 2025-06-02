package com.sandraygonzalo.bookhub;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;


import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private boolean isDebugMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        isDebugMode = isRunningOnEmulator(); // Cambia a true si quieres forzarlo manualmente

        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView registerTextView = findViewById(R.id.registerTextView);

        loginButton.setOnClickListener(view -> {
            if (isDebugMode) {
                // 🔧 DEBUG: Login sin 2FA
                mAuth.signInAnonymously()
                        .addOnSuccessListener(authResult -> {
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(LoginActivity.this, "Login anónimo fallido: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                return;
            }

            // 🔒 LOGIN NORMAL CON 2FA
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Por favor completa los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(uid).get()
                                    .addOnSuccessListener(document -> {
                                        String phone = document.getString("phone");
                                        if (phone != null && !phone.isEmpty()) {
                                            Log.d("2FA", "Iniciando verificación por SMS con número: " + phone);

                                            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                                                    .setPhoneNumber(phone)
                                                    .setTimeout(60L, TimeUnit.SECONDS)
                                                    .setActivity(this)
                                                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                                        @Override
                                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                                            Log.d("2FA", "✅ Verificación automática completada");

                                                            // Solo reautenticamos si el número coincide
                                                            mAuth.getCurrentUser().reauthenticate(credential)
                                                                    .addOnSuccessListener(result -> {
                                                                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                                                                .update("isPhoneVerified", true)
                                                                                .addOnSuccessListener(aVoid -> {
                                                                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                                                    finish();
                                                                                });
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.e("2FA", "❌ Error en reauth automática: " + e.getMessage());
                                                                        FirebaseAuth.getInstance().signOut();
                                                                    });
                                                        }

                                                        @Override
                                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                                            Log.e("2FA", "❌ Falló la verificación por SMS: " + e.getMessage());
                                                            Toast.makeText(LoginActivity.this, "Error al verificar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                            FirebaseAuth.getInstance().signOut();
                                                        }

                                                        @Override
                                                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                                            Log.d("2FA", "📩 Código enviado correctamente");
                                                            Intent intent = new Intent(LoginActivity.this, VerifyCodeActivity.class);
                                                            intent.putExtra("verificationId", verificationId);
                                                            intent.putExtra("userId", uid);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    })
                                                    .build();

                                            PhoneAuthProvider.verifyPhoneNumber(options);

                                        } else {
                                            Toast.makeText(LoginActivity.this, "No se encontró número de teléfono", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        registerTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    // ✅ Detección automática de emulador
    private boolean isRunningOnEmulator() {
        return Build.FINGERPRINT.contains("generic")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
