package com.sandraygonzalo.bookhub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sandraygonzalo.bookhub.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

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
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Acción para Inicio
                return true;
            } else if (itemId == R.id.nav_favorites) {
                // Acción para Favoritos
                return true;
            } else if (itemId == R.id.nav_messages) {
                // Acción para Mensajes
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT); // Barra transparente
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}
