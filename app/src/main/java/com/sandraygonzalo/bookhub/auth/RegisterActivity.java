package com.sandraygonzalo.bookhub.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.sandraygonzalo.bookhub.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        EditText emailEditText = findViewById(R.id.registerEmailEditText);
        EditText passwordEditText = findViewById(R.id.registerPasswordEditText);
        EditText confirmPasswordEditText = findViewById(R.id.registerConfirmPasswordEditText);
        Button registerButton = findViewById(R.id.registerButton);
        CheckBox termsCheckBox = findViewById(R.id.termsCheckBox);
        EditText phoneEditText = findViewById(R.id.registerPhoneEditText);

        registerButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();

            // Validaciones
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor completa los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!email.contains("@")) {
                Toast.makeText(RegisterActivity.this, "Por favor introduce un correo electrónico válido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor introduce tu número de teléfono", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!termsCheckBox.isChecked()) {
                Toast.makeText(RegisterActivity.this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                return;
            }

            // Registrar usuario en Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user, phone);
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        termsCheckBox.setOnClickListener(v -> {
            if (termsCheckBox.isChecked()) {
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Términos y Condiciones y política de privacidad")
                        .setMessage("\n" +
                                "TÉRMINOS Y CONDICIONES" + "\n" +"Fecha de entrada en vigor: [Indicar fecha de publicación o lanzamiento de la app]\n" +
                                "La aplicación Bookhub ha sido desarrollada como parte de un Trabajo de Fin de Grado (TFG) por estudiantes del Grado en Ingeniería de Software del IMMUNE TECHNOLOGY INSTITUTE. La titularidad legal de la plataforma corresponde a IMMUNE CODING INSTITUTE S.L., con CIF B87979407 y domicilio en Calle de Zurbano, 92, 28003 Madrid, España (en adelante, “Bookhub”, “nosotros” o “la plataforma”).\n" +
                                "Estos Términos y Condiciones regulan el acceso, registro y uso de la app Bookhub, disponible en dispositivos iOS y Android.\n" +
                                "\n1. ACEPTACIÓN DE LOS TÉRMINOS" +
                                "\n" +
                                "Al registrarte y utilizar la app, aceptas estos Términos y Condiciones y te comprometes a cumplirlos. Si no estás de acuerdo con los mismos, no deberías utilizar Bookhub.\n" +
                                "\n2. USO DE LA PLATAFORMA" +
                                "\n" +
                                "Edad mínima: Para registrarte en Bookhub debes tener al menos 16 años.\n" +
                                "Ámbito territorial: El uso está previsto principalmente para usuarios residentes en España.\n" +
                                "Finalidad: Bookhub es una plataforma de intercambio de libros físicos entre personas usuarias, que se realiza mediante acuerdos presenciales. La app facilita el contacto, gestión de perfil, preferencias lectoras, valoraciones y comunicación mediante chat.\n" +
                                "\n3. CUENTAS DE USUARIO" +
                                "\n" +
                                "El registro requiere la creación de una cuenta mediante Firebase Authentication.\n" +
                                "Cada usuario es responsable de mantener la seguridad de su cuenta y contraseña.\n" +
                                "Nos reservamos el derecho de suspender o cancelar cuentas que violen estos términos, hagan un uso indebido o atenten contra la comunidad.\n" +
                                "\n4. INTERCAMBIO DE LIBROS" +
                                "\n" +
                                "Bookhub no interviene directamente en el proceso de intercambio ni en el transporte o entrega física de los libros.\n" +
                                "Los usuarios son totalmente responsables del cumplimiento del acuerdo, de la entrega de los libros y de cualquier consecuencia derivada.\n" +
                                "Bookhub no se responsabiliza por libros no entregados, deteriorados o pérdidas.\n" +
                                "\n5. CONTENIDO DEL USUARIO" +
                                "\n" +
                                "Los usuarios pueden subir:\n" +
                                "Imágenes de perfil.\n" +
                                "Fotos de portadas de libros.\n" +
                                "Reseñas, descripciones, notas y comentarios.\n" +
                                "Está estrictamente prohibido subir contenido que:\n" +
                                "Sea ofensivo, difamatorio, discriminatorio, violento o sexualmente explícito.\n" +
                                "Infrinja derechos de terceros o de propiedad intelectual.\n" +
                                "Contenga spam o automatizaciones.\n" +
                                "Atente contra la comunidad o el propósito de la plataforma.\n" +
                                "Bookhub se reserva el derecho de eliminar cualquier contenido que infrinja estas normas sin previo aviso.\n" +
                                "\n6. LICENCIA DE USO" +
                                "\n" +
                                "Bookhub otorga al usuario una licencia de uso limitada, personal, no exclusiva y revocable para utilizar la aplicación de acuerdo con estos términos.\n" +
                                "\n7. PRIVACIDAD Y USO DE DATOS" +
                                "\n" +
                                "El tratamiento de datos personales se realiza conforme al Reglamento General de Protección de Datos (RGPD) y se detalla en nuestra Política de Privacidad. Los datos pueden ser utilizados para:\n" +
                                "Gestionar tu cuenta y actividad dentro de la app.\n" +
                                "Enviar notificaciones push y correos electrónicos.\n" +
                                "Análisis estadístico y mejora del servicio.\n" +
                                "Publicidad, promociones y futuras funcionalidades comerciales.\n" +
                                "El usuario puede configurar las notificaciones en cualquier momento.\n" +
                                "\n8. NOTIFICACIONES" +
                                "\n" +
                                "La app podrá enviarte notificaciones push, correos electrónicos u otras comunicaciones.\n" +
                                "Puedes desactivar estas notificaciones en los ajustes de tu cuenta o del dispositivo.\n" +
                                "\n9. CÓDIGO DE CONDUCTA" +
                                "\n" +
                                "La comunidad de Bookhub está basada en el respeto y la colaboración. Por ello, se prohíbe:\n" +
                                "Compartir contenido violento, sexual, amenazante o que incite al odio.\n" +
                                "Molestar, acosar o suplantar a otros usuarios.\n" +
                                "Utilizar la app con fines comerciales sin consentimiento previo.\n" +
                                "El incumplimiento podrá dar lugar a la suspensión o eliminación inmediata de la cuenta.\n" +
                                "\n10. PUBLICIDAD Y FUTURAS FUNCIONALIDADES" +
                                "\n" +
                                "Actualmente, Bookhub es un servicio gratuito.\n" +
                                "En el futuro podría incluir publicidad, anuncios promocionales o servicios de pago. Cualquier cambio será notificado debidamente al usuario y podrá decidir si continuar usando la app.\n" +
                                "\n11. MODIFICACIONES" +
                                "\n" +
                                "Bookhub podrá actualizar estos Términos y Condiciones cuando lo considere necesario. En caso de cambios relevantes, se notificará a los usuarios mediante correo o desde la propia app.\n" +
                                "\n12. LIMITACIÓN DE RESPONSABILIDAD" +
                                "\n" +
                                "Bookhub no se hace responsable de interrupciones, errores o pérdidas derivadas del uso de la app.\n" +
                                "No asumimos responsabilidad por daños derivados de los actos de otros usuarios, intercambios fallidos o mal uso de la plataforma.\n" +
                                "El uso de Bookhub es bajo riesgo y responsabilidad exclusiva del usuario.\n" +
                                "\n13. PROPIEDAD INTELECTUAL" +
                                "\n" +
                                "Todos los derechos sobre el nombre, diseño, código, interfaz y contenido de la app pertenecen a IMMUNE CODING INSTITUTE S.L. o sus licenciantes. Queda prohibida su reproducción o distribución sin autorización expresa.\n" +
                                "\n14. TERMINACIÓN" +
                                "\n" +
                                "Bookhub podrá suspender o cancelar el acceso a la app de cualquier usuario que:\n" +
                                "Infrinja estos términos.\n" +
                                "Afecte negativamente a otros usuarios o al funcionamiento de la plataforma.\n" +
                                "Use la app con fines ilícitos o no autorizados.\n" +
                                "\n15. LEGISLACIÓN Y JURISDICCIÓN" +
                                "\n" +
                                "Estos Términos se rigen por la legislación española. En caso de conflicto, las partes se someterán a los Juzgados y Tribunales de Madrid, renunciando a cualquier otro fuero.\n" +
                                "\n16. CONTACTO" +
                                "\n" +
                                "Para cualquier consulta, sugerencia o reclamación, puedes escribirnos a:\n" +
                                "\uD83D\uDCE7 bookhub.team@gmail.com" +
                                "\n" +"\n" +
                                "----------------------------"+
                                "\n" +"\n" +
                                "POLÍTICA DE PRIVACIDAD" + "\n" +
                                "\n1. ¿QUÉ DATOS RECOPILAMOS?" +
                                "\n" +
                                "A través de Firebase y del uso directo de la app, recopilamos los siguientes datos personales:\n" +
                                "Datos de registro:\n" +
                                "Nombre y apellidos\n" +
                                "Nombre de usuario\n" +
                                "Correo electrónico\n" +
                                "Ubicación (ciudad)\n" +
                                "Imagen de perfil (opcional)\n" +
                                "Preferencias de género literario\n" +
                                "Datos de actividad:\n" +
                                "Libros añadidos o marcados como favoritos\n" +
                                "Historial de intercambios\n" +
                                "Valoraciones y comentarios\n" +
                                "Mensajes y conversaciones en el chat\n" +
                                "Notificaciones activadas o desactivadas\n" +
                                "Datos técnicos:\n" +
                                "Dirección IP, tipo de dispositivo, versión del sistema operativo, idioma, fecha y hora de acceso\n" +
                                "\n2. ¿CÓMO RECOGEMOS ESTOS DATOS?" +
                                "\n" +
                                "A través del registro y uso de la app\n" +
                                "Mediante Firebase Authentication y Firestore\n" +
                                "Cuando interactúas con otras personas usuarias\n" +
                                "Al contactar con nosotros o enviarnos mensajes\n" +
                                "\n3. ¿CON QUÉ FINALIDAD USAMOS TUS DATOS?" +
                                "\n" +
                                "Utilizamos tus datos personales para:\n" +
                                "Gestionar tu cuenta y permitir el uso de las funciones principales de la app (intercambios, chat, perfil, etc.)\n" +
                                "Mejorar la experiencia de usuario y personalizar el contenido\n" +
                                "Analizar el uso de la app (analítica interna)\n" +
                                "Enviarte notificaciones push y correos electrónicos (que puedes desactivar)\n" +
                                "Posiblemente, en el futuro, mostrarte publicidad personalizada o promociones\n" +
                                "\n4. BASE LEGAL PARA EL TRATAMIENTO" +
                                "\n" +
                                "La base legal para el tratamiento de tus datos es:\n" +
                                "Tu consentimiento al registrarte y aceptar esta política\n" +
                                "La ejecución de un contrato (proporcionarte el servicio)\n" +
                                "Interés legítimo (mejorar el servicio, prevenir fraudes)\n" +
                                "Cumplimiento legal cuando sea requerido\n" +
                                "\n5. ¿DURANTE CUÁNTO TIEMPO CONSERVAMOS TUS DATOS?" +
                                "\n" +
                                "Conservamos tus datos mientras mantengas una cuenta activa en Bookhub. Si decides eliminar tu cuenta, eliminaremos o anonimizaremos tus datos, salvo aquellos que debamos conservar por motivos legales.\n" +
                                "\n6. ¿CON QUIÉN COMPARTIMOS TUS DATOS?" +
                                "\n" +
                                "No vendemos ni compartimos tus datos personales con terceros con fines comerciales.\n" +
                                "Podemos compartir información con:\n" +
                                "Google Firebase, como proveedor de servicios en la nube y base de datos\n" +
                                "Autoridades públicas si lo exige la ley\n" +
                                "Servicios de analítica y mantenimiento, bajo acuerdos de confidencialidad\n" +
                                "Todos los datos se almacenan en servidores seguros y cumplen con la normativa europea.\n" +
                                "\n7. TRANSFERENCIAS INTERNACIONALES" +
                                "\n" +
                                "Bookhub utiliza Firebase (Google), cuyos servidores pueden estar ubicados fuera del Espacio Económico Europeo (EEE). Google garantiza la protección de datos mediante cláusulas contractuales tipo aprobadas por la Comisión Europea.\n" +
                                "\n8. TUS DERECHOS" +
                                "\n" +
                                "Como usuario, puedes ejercer los siguientes derechos:\n" +
                                "Acceder a tus datos personales\n" +
                                "Rectificar datos inexactos o incompletos\n" +
                                "Solicitar la eliminación de tus datos\n" +
                                "Limitar u oponerte al tratamiento\n" +
                                "Portabilidad de los datos a otro proveedor\n" +
                                "Retirar tu consentimiento en cualquier momento\n" +
                                "Puedes ejercer estos derechos enviando un correo a bookhub.team@gmail.com indicando claramente tu solicitud y adjuntando una copia de tu DNI o documento identificativo.\n" +
                                "\n9. SEGURIDAD" +
                                "\n" +
                                "Utilizamos medidas técnicas y organizativas adecuadas para proteger tu información, como:\n" +
                                "Cifrado mediante HTTPS\n" +
                                "Accesos autenticados\n" +
                                "Limitación de permisos en Firebase\n" +
                                "\n10. CAMBIOS EN ESTA POLÍTICA" +
                                "\n" +
                                "Bookhub se reserva el derecho de modificar esta Política de Privacidad. Si introducimos cambios relevantes, lo notificaremos a través de la app o por correo electrónico.\n" +
                                "\n11. CONTACTO" +
                                "\n" +
                                "Si tienes dudas sobre esta Política o quieres ejercer tus derechos, contáctanos en:\n" +
                                "\uD83D\uDCE7 bookhub.team@gmail.com\n" +
                                "Última actualización: [Fecha actual]")
                        .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                        .setNegativeButton("Cancelar", (dialog, which) -> {
                            dialog.dismiss();
                            termsCheckBox.setChecked(false); // desmarcar si cancela
                        })
                        .show();
            }
        });



        // Hacer la barra de estado transparente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void saveUserToFirestore(FirebaseUser user, String phone) {
        Map<String, Object> userData = new HashMap<>();

        // Campos definidos en el modelo
        userData.put("username", "");  // Se completará en onboarding
        userData.put("email", user.getEmail());
        userData.put("location", "");
        userData.put("profilePicture", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

        // Campo adicional personalizado
        userData.put("phone", phone);

        // Campos estructurados
        userData.put("preferences", new ArrayList<String>());

        Map<String, Object> rating = new HashMap<>();
        rating.put("average", 0.0);
        rating.put("totalExchanges", 0);
        userData.put("rating", rating);

        userData.put("booksAvailable", new ArrayList<String>());
        userData.put("exchangeHistory", new ArrayList<String>());
        userData.put("favorites", new ArrayList<String>());
        userData.put("notificationsEnabled", true);

        // Guardar en Firestore
        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, OnboardingActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterActivity.this, "Error al guardar en Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}

