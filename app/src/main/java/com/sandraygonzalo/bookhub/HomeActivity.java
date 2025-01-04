package com.sandraygonzalo.bookhub;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Configurar el texto de saludo
        TextView userName = findViewById(R.id.user_name);
        userName.setText("Hola, Sandra"); // Cambia esto dinámicamente según el usuario

        // Configurar la barra de menú inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    // Acción para "Inicio"
                    return true;
                case R.id.nav_favorites:
                    // Acción para "Favoritos"
                    return true;
                case R.id.nav_messages:
                    // Acción para "Mensajes"
                    return true;
                case R.id.nav_profile:
                    // Acción para "Perfil"
                    return true;
            }
            return false;
        });
    }
}
