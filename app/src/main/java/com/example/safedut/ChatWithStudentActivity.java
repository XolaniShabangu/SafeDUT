package com.example.safedut;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.*;

public class ChatWithStudentActivity extends AppCompatActivity {

    private RecyclerView chatRecycler;
    private EditText inputMessage;
    private Button btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private String studentId;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_student);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "Invalid chat target", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
        chatRecycler = findViewById(R.id.chatRecycler);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatAdapter(messages);
        chatRecycler.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

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
        FirebaseDatabase.getInstance().getReference("chat_messages").child(studentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            ChatMessage msg = snap.getValue(ChatMessage.class);
                            if (msg != null) messages.add(msg);
                        }
                        adapter.notifyDataSetChanged();
                        chatRecycler.scrollToPosition(messages.size() - 1);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatWithStudentActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage(String text) {
        ChatMessage msg = new ChatMessage(
                currentUser.getUid(),
                "Staff",
                text,
                System.currentTimeMillis()
        );

        FirebaseDatabase.getInstance().getReference("chat_messages").child(studentId)
                .push().setValue(msg)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show());
    }
}
