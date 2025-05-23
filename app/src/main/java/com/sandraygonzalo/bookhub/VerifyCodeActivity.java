package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

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
                mAuth.getCurrentUser().linkWithCredential(credential)
                        .addOnSuccessListener(result -> {
                            Intent intent = new Intent(VerifyCodeActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
