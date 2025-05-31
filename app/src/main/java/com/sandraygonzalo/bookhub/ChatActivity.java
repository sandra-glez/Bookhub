package com.sandraygonzalo.bookhub;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private String chatId;
    private String currentUserId;
    private String otherUserId;
    private FirebaseFirestore db;
    private CollectionReference messagesRef;

    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");

        if (chatId == null || otherUserId == null) {
            Toast.makeText(this, "Datos del chat no disponibles", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        messagesRef = db.collection("chats").document(chatId).collection("messages");

        loadMessages();
        setupSendButton();
        loadOtherUserInfo();
    }

    private void loadMessages() {
        messagesRef.orderBy("sentAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("CHAT_DEBUG", "Error cargando mensajes", error);
                        return;
                    }

                    messageList.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Message msg = doc.toObject(Message.class);
                        if (msg != null) messageList.add(msg);
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String content = messageInput.getText().toString().trim();
            if (content.isEmpty()) return;

            Map<String, Object> msgData = new HashMap<>();
            msgData.put("content", content);
            msgData.put("senderId", currentUserId);
            msgData.put("sentAt", FieldValue.serverTimestamp());
            msgData.put("type", "text");

            messagesRef.add(msgData).addOnSuccessListener(documentReference -> {
                Map<String, Object> lastMsgUpdate = new HashMap<>();
                lastMsgUpdate.put("lastMessage", content);
                lastMsgUpdate.put("lastMessageAt", FieldValue.serverTimestamp());

                db.collection("chats").document(chatId).update(lastMsgUpdate);
                messageInput.setText("");
            });
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
