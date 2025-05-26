package com.example.safedut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;




import androidx.fragment.app.Fragment;

public class ChatFragment extends Fragment {

    public ChatFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        Button btnChatAdmin = view.findViewById(R.id.btnChatAdmin);
        Button btnChatAI = view.findViewById(R.id.btnChatAI);

        btnChatAdmin.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Opening Chat with Admin", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), ChatWithAdminActivity.class);
            startActivity(intent);
        });

        btnChatAI.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatWithAIActivity.class);
            startActivity(intent);
        });


        return view;
    }
}
