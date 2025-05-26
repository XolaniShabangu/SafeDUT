package com.example.safedut;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.List;

public class AdminChatListAdapter extends RecyclerView.Adapter<AdminChatListAdapter.ViewHolder> {

    private final List<String> studentIds;
    private final OnStudentClickListener listener;

    public interface OnStudentClickListener {
        void onStudentClick(String studentId);
    }

    public AdminChatListAdapter(List<String> studentIds, OnStudentClickListener listener) {
        this.studentIds = studentIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String studentId = studentIds.get(position);

        // Fetch user's display name
        FirebaseDatabase.getInstance().getReference("users")
                .child(studentId)
                .child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot nameSnap) {
                        String name = nameSnap.exists() ? nameSnap.getValue(String.class) : "Unknown";

                        // Check latest message
                        FirebaseDatabase.getInstance().getReference("chat_messages")
                                .child(studentId)
                                .limitToLast(1)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot messageSnap) {
                                        boolean showSiren = false;

                                        for (DataSnapshot msgSnap : messageSnap.getChildren()) {
                                            ChatMessage lastMsg = msgSnap.getValue(ChatMessage.class);
                                            if (lastMsg != null && studentId.equals(lastMsg.getSenderId())) {
                                                showSiren = true;
                                            }

                                        }

                                        String display = showSiren ? "🚨 " + name : name;
                                        holder.userName.setText(display);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        holder.itemView.setOnClickListener(v -> listener.onStudentClick(studentId));
    }

    @Override
    public int getItemCount() {
        return studentIds.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.textUserName);
        }
    }
}
