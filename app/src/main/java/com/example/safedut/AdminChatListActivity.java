package com.example.safedut;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AdminChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminChatListAdapter adapter;
    private List<String> studentIds = new ArrayList<>();
    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        recyclerView = findViewById(R.id.chatListRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminChatListAdapter(studentIds, this::openChatWithStudent);
        recyclerView.setAdapter(adapter);

        loadChatThreads();
    }

    private void loadChatThreads() {
        chatRef = FirebaseDatabase.getInstance().getReference("chat_messages");
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentIds.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    studentIds.add(snap.getKey()); // each key is a studentId
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminChatListActivity.this, "Failed to load chats", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChatWithStudent(String studentId) {
        Intent intent = new Intent(this, ChatWithStudentActivity.class);
        intent.putExtra("studentId", studentId);
        startActivity(intent);
    }
}
