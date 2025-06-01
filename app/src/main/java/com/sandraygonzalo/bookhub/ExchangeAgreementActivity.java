package com.sandraygonzalo.bookhub;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ExchangeAgreementActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String currentUserId;
    private String exchangeId;
    private String otherUserId;
    private String user1Id;

    private TextView txtTitleIncoming, txtTitleOutgoing;
    private ImageView imgIncoming, imgOutgoing;
    private Button btnChooseBook, btnConfirmExchange;
    private LinearLayout outgoingBookContainer;
    private TextView exchangeAgreementStatusText;

    private ListenerRegistration exchangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_agreement);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        exchangeId = getIntent().getStringExtra("exchangeId");
        otherUserId = getIntent().getStringExtra("otherUserId");

        if (exchangeId == null || otherUserId == null) {
            Toast.makeText(this, "Datos de intercambio no disponibles", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtTitleIncoming = findViewById(R.id.txtTitleIncoming);
        txtTitleOutgoing = findViewById(R.id.txtTitleOutgoing);
        imgIncoming = findViewById(R.id.imgIncoming);
        imgOutgoing = findViewById(R.id.imgOutgoing);
        btnChooseBook = findViewById(R.id.btnChooseBook);
        btnConfirmExchange = findViewById(R.id.btnConfirmExchange);
        outgoingBookContainer = findViewById(R.id.outgoingBookContainer);
        exchangeAgreementStatusText = findViewById(R.id.exchangeStatusText);

        exchangeAgreementStatusText.setVisibility(View.GONE);
        btnConfirmExchange.setVisibility(View.GONE);

        btnChooseBook.setOnClickListener(v -> showBookSelectionDialog());
        btnConfirmExchange.setOnClickListener(v -> confirmExchange());

        loadExchangeInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        exchangeListener = db.collection("exchanges").document(exchangeId)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && snapshot.exists()) {
                        loadExchangeInfo(); // tiempo real
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exchangeListener != null) exchangeListener.remove();
    }

    private void confirmExchange() {
        String confirmField = currentUserId.equals(user1Id) ? "user1Confirmed" : "user2Confirmed";
        db.collection("exchanges").document(exchangeId)
                .update(confirmField, true)
                .addOnSuccessListener(unused -> checkIfBothConfirmedAndReturn());
    }

    private void checkIfBothConfirmedAndReturn() {
        db.collection("exchanges").document(exchangeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean u1 = doc.getBoolean("user1Confirmed");
                        Boolean u2 = doc.getBoolean("user2Confirmed");

                        if (Boolean.TRUE.equals(u1) && Boolean.TRUE.equals(u2)) {
                            db.collection("exchanges").document(exchangeId)
                                    .update("status", "agreed");
                        }

                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("chatId", exchangeId);
                        intent.putExtra("otherUserId", otherUserId);
                        intent.putExtra("bookCoverUrl", getIntent().getStringExtra("bookCoverUrl"));
                        intent.putExtra("title", getIntent().getStringExtra("title"));
                        intent.putExtra("author", getIntent().getStringExtra("author"));
                        intent.putExtra("exchangeConfirmed", true);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void loadExchangeInfo() {
        db.collection("exchanges").document(exchangeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String bookRequestedId = doc.getString("bookRequestedId");
                    String bookOfferedId = doc.getString("bookOfferedId");
                    String status = doc.getString("status");
                    user1Id = doc.getString("user1Id");

                    boolean isUser1 = currentUserId.equals(user1Id);
                    String incomingBookId = isUser1 ? bookRequestedId : bookOfferedId;
                    String outgoingBookId = isUser1 ? bookOfferedId : bookRequestedId;

                    if (incomingBookId != null)
                        loadBookInfo(incomingBookId, txtTitleIncoming, imgIncoming);

                    if (outgoingBookId != null) {
                        outgoingBookContainer.setVisibility(View.VISIBLE);
                        loadBookInfo(outgoingBookId, txtTitleOutgoing, imgOutgoing);
                    } else {
                        outgoingBookContainer.setVisibility(View.GONE);
                    }

                    Boolean u1 = doc.getBoolean("user1Confirmed");
                    Boolean u2 = doc.getBoolean("user2Confirmed");

                    boolean iConfirmed = isUser1 ? Boolean.TRUE.equals(u1) : Boolean.TRUE.equals(u2);
                    boolean otherConfirmed = isUser1 ? Boolean.TRUE.equals(u2) : Boolean.TRUE.equals(u1);

                    if ("agreed".equals(status) || (Boolean.TRUE.equals(u1) && Boolean.TRUE.equals(u2))) {
                        showExchangeStatus("✅ Intercambio acordado", "#C8E6C9");
                        btnConfirmExchange.setVisibility(View.GONE);
                    } else if (iConfirmed && !otherConfirmed) {
                        showExchangeStatus("⏳ Esperando confirmación del otro usuario...", "#FFF9C4");
                        btnConfirmExchange.setVisibility(View.GONE);
                    } else if (!iConfirmed && otherConfirmed) {
                        showExchangeStatus("📌 El otro usuario ya ha confirmado. Revisa y confirma.", "#FFE0B2");
                        btnConfirmExchange.setVisibility(View.VISIBLE);
                    } else {
                        exchangeAgreementStatusText.setVisibility(View.GONE);
                        btnConfirmExchange.setVisibility(
                                (bookRequestedId != null && bookOfferedId != null) ? View.VISIBLE : View.GONE
                        );
                    }
                });
    }

    private void showExchangeStatus(String msg, String colorHex) {
        exchangeAgreementStatusText.setText(msg);
        exchangeAgreementStatusText.setBackgroundColor(Color.parseColor(colorHex));
        exchangeAgreementStatusText.setVisibility(View.VISIBLE);
    }

    private void showBookSelectionDialog() {
        db.collection("userBooks")
                .whereEqualTo("ownerId", currentUserId)
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(query -> {
                    List<String> titles = new ArrayList<>();
                    List<DocumentSnapshot> books = new ArrayList<>();

                    for (DocumentSnapshot doc : query) {
                        books.add(doc);
                        titles.add(doc.getString("title"));
                    }

                    if (books.isEmpty()) {
                        Toast.makeText(this, "No tienes libros disponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Selecciona un libro para ofrecer")
                            .setItems(titles.toArray(new String[0]), (dialog, which) -> {
                                String selectedId = books.get(which).getId();
                                db.collection("exchanges").document(exchangeId)
                                        .update("bookOfferedId", selectedId)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Libro seleccionado", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .show();
                });
    }

    private void loadBookInfo(String bookId, TextView titleView, ImageView imageView) {
        db.collection("userBooks").document(bookId)
                .get()
                .addOnSuccessListener(bookDoc -> {
                    if (bookDoc.exists()) {
                        titleView.setText(bookDoc.getString("title"));
                        String coverUrl = bookDoc.getString("coverImage");
                        if (coverUrl != null && !coverUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(coverUrl)
                                    .placeholder(R.drawable.placeholder)
                                    .error(R.drawable.placeholder)
                                    .into(imageView);
                        } else {
                            imageView.setImageResource(R.drawable.placeholder);
                        }
                    }
                });
    }
}


