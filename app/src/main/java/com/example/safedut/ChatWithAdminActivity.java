package com.example.safedut;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ChatWithAdminActivity extends AppCompatActivity {

    private EditText inputMessage;
    private Button btnSend;
    private RecyclerView chatRecycler;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_admin);

        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
        chatRecycler = findViewById(R.id.chatRecycler);

        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messages);
        chatRecycler.setAdapter(chatAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMessages();

        btnSend.setOnClickListener(v -> {
            String text = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessage(text);
                inputMessage.setText("");
            }
        });
    }

    private void loadMessages() {
        String userId = currentUser.getUid();
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chat_messages").child(userId);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    ChatMessage msg = snap.getValue(ChatMessage.class);
                    if (msg != null) messages.add(msg);
                }
                chatAdapter.notifyDataSetChanged();
                chatRecycler.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatWithAdminActivity.this, "Failed to load chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String text) {
        String userId = currentUser.getUid();

        FirebaseDatabase.getInstance().getReference("users").child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String senderName = snapshot.exists() ? snapshot.getValue(String.class) : "You";
                        ChatMessage msg = new ChatMessage(userId, senderName, text, System.currentTimeMillis());

                        FirebaseDatabase.getInstance().getReference("chat_messages")
                                .child(userId)
                                .push()
                                .setValue(msg)
                                .addOnFailureListener(e ->
                                        Toast.makeText(ChatWithAdminActivity.this, "Failed to send", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatWithAdminActivity.this, "Name fetch failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
