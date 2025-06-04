package com.sandraygonzalo.bookhub.messages;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.sandraygonzalo.bookhub.R;

import java.util.Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.widget.*;

public class ChatActivity extends AppCompatActivity {

    private Button btnRequestExchange;
    private FirebaseFirestore db;
    private String currentUserId;
    private String chatId;
    private String otherUserId;
    private boolean yaPreguntadoValoracion = false;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView chatTitle;
    private ImageView bookCover;
    private TextView exchangeStatusText;
    private String user1IdGlobal;
    private String user2IdGlobal;

    private List<Message> messageList;
    private ChatAdapter chatAdapter;
    private CollectionReference messagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.sandraygonzalo.bookhub.R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");

        if (chatId == null || otherUserId == null) {
            Toast.makeText(this, "Datos del chat no disponibles", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView = findViewById(com.sandraygonzalo.bookhub.R.id.chatRecyclerView);
        messageInput = findViewById(com.sandraygonzalo.bookhub.R.id.messageInput);
        sendButton = findViewById(com.sandraygonzalo.bookhub.R.id.sendButton);
        btnRequestExchange = findViewById(com.sandraygonzalo.bookhub.R.id.btnRequestExchange);
        chatTitle = findViewById(com.sandraygonzalo.bookhub.R.id.bookTitleHeader);
        bookCover = findViewById(com.sandraygonzalo.bookhub.R.id.bookCoverSmall);
        exchangeStatusText = findViewById(com.sandraygonzalo.bookhub.R.id.exchangeStatusText); // ✅ nuevo

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        messagesRef = db.collection("chats").document(chatId).collection("messages");

        btnRequestExchange.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, ExchangeAgreementActivity.class);
            intent.putExtra("exchangeId", chatId);
            intent.putExtra("otherUserId", otherUserId);
            startActivity(intent);
        });

        loadMessages();
        setupSendButton();
        loadOtherUserInfo();
        loadBookInfo();        // 🔧 Corrige el fallo desde favoritos
        loadExchangeStatus(); // ✅ Muestra mensaje si está acordado

        // BOTÓN VOLVER
        ImageButton backButton = findViewById(com.sandraygonzalo.bookhub.R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MessagesActivity.class); // <-- cambia esto por el nombre real de tu pantalla de chats
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Hacer la barra de estado transparente
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExchangeStatus();
    }

    private void verificarSiYaValoro(String user1Id, String user2Id) {
        boolean soyUser1 = currentUserId.equals(user1Id);
        String campo = soyUser1 ? "user1Rated" : "user2Rated";

        db.collection("exchanges").document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Boolean yaValoro = doc.getBoolean(campo);
                    if (Boolean.TRUE.equals(yaValoro)) {
                        Toast.makeText(this, "Ya valoraste este intercambio", Toast.LENGTH_SHORT).show();
                    } else {
                        mostrarDialogoValoracionDesdeChat(user1Id, user2Id);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al verificar la valoración", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadExchangeStatus() {
        db.collection("exchanges").document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String status = doc.getString("status");
                    Boolean user1Rated = doc.getBoolean("user1Rated");
                    Boolean user2Rated = doc.getBoolean("user2Rated");
                    String bookRequestedId = doc.getString("bookRequestedId");
                    String bookOfferedId = doc.getString("bookOfferedId");

                    // Guardamos para el botón de valorar
                    user1IdGlobal = doc.getString("user1Id");
                    user2IdGlobal = doc.getString("user2Id");

                    boolean soyUser1 = currentUserId.equals(user1IdGlobal);
                    boolean yaValoro = soyUser1 ? Boolean.TRUE.equals(user1Rated) : Boolean.TRUE.equals(user2Rated);

                    if ("agreed".equals(status)) {
                        btnRequestExchange.setVisibility(View.GONE);
                        exchangeStatusText.setText("✅ Intercambio completado");
                        exchangeStatusText.setVisibility(View.VISIBLE);

                        if (!yaValoro && !yaPreguntadoValoracion) {
                            yaPreguntadoValoracion = true;
                            verificarSiYaValoro(user1IdGlobal, user2IdGlobal);
                        }

                        if (Boolean.TRUE.equals(user1Rated) && Boolean.TRUE.equals(user2Rated)) {
                            eliminarLibros(bookOfferedId, bookRequestedId);
                        }
                    } else {
                        exchangeStatusText.setVisibility(View.GONE);
                        btnRequestExchange.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void mostrarDialogoValoracionDesdeChat(String user1Id, String user2Id) {
        final String[] opciones = {"1 ★", "2 ★", "3 ★", "4 ★", "5 ★"};

        new AlertDialog.Builder(this)
                .setTitle("Valora tu intercambio")
                .setCancelable(false) // no permitir cerrarlo sin valorar
                .setItems(opciones, (dialog, which) -> {
                    int ratingSeleccionado = which + 1;
                    guardarValoracionDesdeChat(user1Id, user2Id, ratingSeleccionado);
                })
                .show();
    }
    private void guardarValoracionDesdeChat(String user1Id, String user2Id, int rating) {
        DocumentReference exchangeRef = db.collection("exchanges").document(chatId);
        boolean esUser1 = currentUserId.equals(user1Id);

        String ratingField = esUser1 ? "user1Rating" : "user2Rating";
        String ratedField = esUser1 ? "user1Rated" : "user2Rated";
        String targetUserId = esUser1 ? user2Id : user1Id;

        exchangeRef.update(ratingField, rating, ratedField, true)
                .addOnSuccessListener(unused -> actualizarPromedioUsuarioDesdeChat(targetUserId, rating))
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la valoración", Toast.LENGTH_SHORT).show());
    }
    private void actualizarPromedioUsuarioDesdeChat(String userId, int nuevaValoracion) {
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) return;

            Map<String, Object> ratingMap = (Map<String, Object>) doc.get("rating");
            double average = ratingMap != null && ratingMap.get("average") instanceof Number ?
                    ((Number) ratingMap.get("average")).doubleValue() : 0.0;
            int total = ratingMap != null && ratingMap.get("totalExchanges") instanceof Number ?
                    ((Number) ratingMap.get("totalExchanges")).intValue() : 0;

            double nuevoAverage = ((average * total) + nuevaValoracion) / (total + 1);
            int nuevoTotal = total + 1;

            Map<String, Object> nuevoRating = new HashMap<>();
            nuevoRating.put("average", nuevoAverage);
            nuevoRating.put("totalExchanges", nuevoTotal);

            userRef.update("rating", nuevoRating)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Valoración registrada ✅", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar rating", Toast.LENGTH_SHORT).show());
        });
    }
    private void eliminarLibros(String book1Id, String book2Id) {
        if (book1Id != null) {
            db.collection("userBooks").document(book1Id)
                    .update("available", false)
                    .addOnSuccessListener(unused -> Log.d("Exchange", "📕 Libro 1 marcado como no disponible"))
                    .addOnFailureListener(e -> Log.e("Exchange", "❌ Error al actualizar libro 1", e));
        }

        if (book2Id != null) {
            db.collection("userBooks").document(book2Id)
                    .update("available", false)
                    .addOnSuccessListener(unused -> Log.d("Exchange", "📘 Libro 2 marcado como no disponible"))
                    .addOnFailureListener(e -> Log.e("Exchange", "❌ Error al actualizar libro 2", e));
        }
    }
    private void loadBookInfo() {
        Log.d("CHAT", "Cargando datos del libro para chatId: " + chatId);

        db.collection("exchanges").document(chatId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Log.w("CHAT", "❌ El documento de intercambio no existe");
                        return;
                    }

                    String user1Id = doc.getString("user1Id");
                    String bookRequestedId = doc.getString("bookRequestedId");
                    String bookOfferedId = doc.getString("bookOfferedId");

                    Log.d("CHAT", "user1Id: " + user1Id);
                    Log.d("CHAT", "bookRequestedId: " + bookRequestedId);
                    Log.d("CHAT", "bookOfferedId: " + bookOfferedId);
                    Log.d("CHAT", "currentUserId: " + currentUserId);

                    if (user1Id == null || bookRequestedId == null) {
                        Log.e("CHAT", "⚠️ Faltan campos mínimos para cargar el libro");
                        return;
                    }

                    // Determinar cuál es el libro del otro usuario
                    String incomingBookId;

                    if (currentUserId.equals(user1Id)) {
                        incomingBookId = bookRequestedId;
                    } else {
                        incomingBookId = (bookOfferedId != null) ? bookOfferedId : bookRequestedId;
                    }

                    Log.d("CHAT", "📚 ID del libro entrante: " + incomingBookId);

                    db.collection("userBooks").document(incomingBookId)
                            .get()
                            .addOnSuccessListener(bookDoc -> {
                                if (!bookDoc.exists()) {
                                    Log.w("CHAT", "❌ El documento del libro no existe: " + incomingBookId);
                                    return;
                                }

                                String title = bookDoc.getString("title");
                                String author = bookDoc.getString("author");
                                String coverUrl = bookDoc.getString("coverImage");

                                Log.d("CHAT", "📖 Título: " + title);
                                Log.d("CHAT", "✍️ Autor: " + author);
                                Log.d("CHAT", "🖼️ URL portada: " + coverUrl);

                                if (title != null && author != null) {
                                    chatTitle.setText(title + " - " + author);
                                } else if (title != null) {
                                    chatTitle.setText(title);
                                } else {
                                    chatTitle.setText("Libro");
                                }

                                if (coverUrl != null && !coverUrl.isEmpty()) {
                                    Glide.with(this)
                                            .load(coverUrl)
                                            .placeholder(com.sandraygonzalo.bookhub.R.drawable.placeholder)
                                            .into(bookCover);
                                } else {
                                    bookCover.setImageResource(com.sandraygonzalo.bookhub.R.drawable.placeholder);
                                }
                            })
                            .addOnFailureListener(e -> Log.e("CHAT", "Error al obtener userBook", e));
                })
                .addOnFailureListener(e -> Log.e("CHAT", "Error al obtener exchange", e));
    }
    private void loadMessages() {
        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (snapshots == null || snapshots.isEmpty()) return;
                    messageList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Message msg = doc.toObject(Message.class);
                        messageList.add(msg);
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
    }
    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (content.isEmpty()) return;

            Message msg = new Message(currentUserId, content, new Timestamp(new Date()), "text");
            messagesRef.add(msg);
            messageInput.setText("");

            db.collection("chats").document(chatId)
                    .update("lastMessage", content, "lastMessageAt", FieldValue.serverTimestamp());
        });
    }
    private void loadOtherUserInfo() {
        TextView otherUsernameText = findViewById(R.id.otherUsername);
        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String username = doc.getString("username");
                        otherUsernameText.setText(username);
                    }
                });
    }
}

