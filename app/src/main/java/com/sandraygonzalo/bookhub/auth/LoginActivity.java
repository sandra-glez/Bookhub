package com.sandraygonzalo.bookhub.auth;

import android.app.AlertDialog;
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
import com.sandraygonzalo.bookhub.home.HomeActivity;
import com.sandraygonzalo.bookhub.R;

import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerTextView;
    private TextView forgotPasswordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        loginButton.setOnClickListener(v -> iniciarLogin());

        registerTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        //RECUPERAR CONTRASEÑA
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        forgotPasswordTextView.setOnClickListener(v -> mostrarDialogoRecuperacion());

        // BARRA TRANSPARENTE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
    private void mostrarDialogoRecuperacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Recuperar contraseña");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 10);

        final EditText emailInput = new EditText(this);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setHint("Introduce tu correo electrónico");
        emailInput.setBackgroundResource(R.drawable.edit_text_bg);
        emailInput.setPadding(40, 30, 40, 30);

        layout.addView(emailInput);
        builder.setView(layout);

        builder.setPositiveButton("Enviar", null); // se asigna luego
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // ENVIAR
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.verde));
            positiveButton.setOnClickListener(v -> {
                String email = emailInput.getText().toString().trim();
                if (!email.isEmpty()) {
                    enviarEmailRecuperacion(email);
                    dialog.dismiss();
                } else {
                    Toast.makeText(LoginActivity.this, "El campo no puede estar vacío", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // CANCELAR
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.verde));
        }
    }
    private void enviarEmailRecuperacion(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Se ha enviado un correo para restablecer tu contraseña", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error al enviar el correo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void iniciarLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String uid = user.getUid();

                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(document -> {
                                String phone = document.getString("phone");
                                if (phone == null || phone.isEmpty()) {
                                    Toast.makeText(this, "No hay teléfono registrado", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                    return;
                                }

                                enviarCodigoSMS(phone, email, password);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Login fallido: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void enviarCodigoSMS(String phone, String email, String password) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        // Ignoramos la verificación automática
                        Log.d("2FA", "✅ Verificación automática detectada, ignorada");
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e("2FA", "❌ Verificación fallida: " + e.getMessage());
                        Toast.makeText(LoginActivity.this, "Error al enviar SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d("2FA", "📩 Código enviado a " + phone);
                        Intent intent = new Intent(LoginActivity.this, VerifyCodeActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finish();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}

