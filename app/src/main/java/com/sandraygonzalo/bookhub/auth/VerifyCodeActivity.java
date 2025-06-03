package com.sandraygonzalo.bookhub.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.sandraygonzalo.bookhub.home.HomeActivity;
import com.sandraygonzalo.bookhub.R;

public class VerifyCodeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String verificationId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        mAuth = FirebaseAuth.getInstance();
        EditText codeEditText = findViewById(R.id.codeEditText);
        Button verifyButton = findViewById(R.id.verifyButton);

        verificationId = getIntent().getStringExtra("verificationId");
        userId = getIntent().getStringExtra("userId");

        verifyButton.setOnClickListener(v -> {
            String code = codeEditText.getText().toString();
            if (verificationId != null && !code.isEmpty()) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                mAuth.getCurrentUser().reauthenticate(credential)
                        .addOnSuccessListener(result -> {
                            Intent intent = new Intent(VerifyCodeActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Código incorrecto: " + e.getMessage(), Toast.LENGTH_LONG).show());

            }
        });

        // Estilo transparente para status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}
