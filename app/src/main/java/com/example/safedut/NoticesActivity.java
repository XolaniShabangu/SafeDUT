package com.example.safedut;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Collections;
import java.util.Comparator;


import java.util.ArrayList;
import java.util.List;

public class NoticesActivity extends AppCompatActivity {
    private RecyclerView rvNotices;
    private NoticeAdapter adapter;
    private List<Notice> noticeList = new ArrayList<>();

    private EditText etTitle, etDescription;
    private Spinner spinnerCampus;
    private Button btnCreate;

    private DatabaseReference noticesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notices);


        rvNotices      = findViewById(R.id.rvNotices);
        etTitle        = findViewById(R.id.etTitle);
        spinnerCampus  = findViewById(R.id.spinnerCampus);
        etDescription  = findViewById(R.id.etDescription);
        btnCreate      = findViewById(R.id.btnCreateNotice);


        String[] campuses = {"All Campuses", "Ritson Campus", "City Campus", "Steve Biko", "ML Sultan Campus"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, campuses
        );
        spinnerCampus.setAdapter(spAdapter);


        rvNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoticeAdapter(noticeList);
        rvNotices.setAdapter(adapter);


        noticesRef = FirebaseDatabase.getInstance()
                .getReference("notices");


        noticesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                noticeList.clear();
                for (DataSnapshot child : snap.getChildren()) {
                    Notice n = child.getValue(Notice.class);
                    if (n == null) continue;
                    n.id = child.getKey();
                    noticeList.add(n);
                }


                Collections.sort(noticeList, new Comparator<Notice>() {
                    @Override
                    public int compare(Notice a, Notice b) {
                        if (a.resolved != b.resolved) {

                            return a.resolved ? +1 : -1;
                        }

                        return Long.compare(b.timestamp, a.timestamp);
                    }
                });
                // ────────────────────────────────────────────────────────

                adapter.notifyDataSetChanged();
                if (!noticeList.isEmpty()) {
                    rvNotices.scrollToPosition(0);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError err) {
                Toast.makeText(NoticesActivity.this,
                        "Failed to load notices", Toast.LENGTH_SHORT).show();
            }
        });


        btnCreate.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String campus = spinnerCampus.getSelectedItem().toString();
            String desc = etDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Required");
                return;
            }
            if (TextUtils.isEmpty(desc)) {
                etDescription.setError("Required");
                return;
            }

            String key = noticesRef.push().getKey();
            if (key == null) {
                Toast.makeText(this, "Error generating key", Toast.LENGTH_SHORT).show();
                return;
            }

            long now = System.currentTimeMillis();
            Notice notice = new Notice(key, title, campus, desc, false, now);

            noticesRef.child(key).setValue(notice)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "Notice created", Toast.LENGTH_SHORT).show();
                        etTitle.setText("");
                        etDescription.setText("");
                        spinnerCampus.setSelection(0);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
