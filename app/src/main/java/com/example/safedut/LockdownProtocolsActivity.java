package com.example.safedut;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LockdownProtocolsActivity extends AppCompatActivity {

    private TextView tvActiveLockdownTitle, tvActiveLockdownInstructions;
    private Button btnDeactivateLockdown, btnCreateLockdown;
    private EditText etLockdownTitle, etLockdownInstructions;
    private Spinner spinnerLockdownCampus, spinnerLockdownSeverity;
    private DatabaseReference lockdownRef;
    private String activeLockdownId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockdown_protocols);


        tvActiveLockdownTitle = findViewById(R.id.tvActiveLockdownTitle);
        tvActiveLockdownInstructions = findViewById(R.id.tvActiveLockdownInstructions);
        btnDeactivateLockdown = findViewById(R.id.btnDeactivateLockdown);
        btnCreateLockdown = findViewById(R.id.btnCreateLockdown);
        etLockdownTitle = findViewById(R.id.etLockdownTitle);
        etLockdownInstructions = findViewById(R.id.etLockdownInstructions);
        spinnerLockdownCampus = findViewById(R.id.spinnerLockdownCampus);
        spinnerLockdownSeverity = findViewById(R.id.spinnerLockdownSeverity);


        lockdownRef = FirebaseDatabase.getInstance().getReference("lockdown");

        setupCampusSpinner();
        setupSeveritySpinner();


        loadActiveLockdown();

        btnCreateLockdown.setOnClickListener(v -> activateLockdown());
        btnDeactivateLockdown.setOnClickListener(v -> confirmDeactivateLockdown());
    }


    private void setupCampusSpinner() {
        String[] campuses = {"All Campuses", "Ritson Campus", "City Campus", "Steve Biko", "ML Sultan Campus"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, campuses);
        spinnerLockdownCampus.setAdapter(adapter);
    }


    private void setupSeveritySpinner() {
        String[] severityLevels = {"Low", "Medium", "High"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, severityLevels);
        spinnerLockdownSeverity.setAdapter(adapter);
    }

    private void loadActiveLockdown() {
        lockdownRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> lockdownData = (Map<String, Object>) snapshot.getValue();
                    if (lockdownData != null) {
                        String title = (String) lockdownData.get("title");
                        String instructions = (String) lockdownData.get("instructions");
                        String severity = (String) lockdownData.get("severity");
                        String campus = (String) lockdownData.get("campus");


                        tvActiveLockdownTitle.setText(title + " (" + severity + " - at " + campus + ")");
                        tvActiveLockdownInstructions.setText(instructions);


                        View lockdownSection = findViewById(R.id.activeLockdownSection);


                        switch (severity) {
                            case "High":
                                lockdownSection.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                                break;
                            case "Medium":
                                lockdownSection.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
                                break;
                            case "Low":
                                lockdownSection.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                                break;
                            default:
                                lockdownSection.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                break;
                        }


                        lockdownSection.setVisibility(View.VISIBLE);
                        btnDeactivateLockdown.setVisibility(View.VISIBLE);

                    }
                } else {

                    findViewById(R.id.activeLockdownSection).setVisibility(View.GONE);
                    tvActiveLockdownTitle.setText("No Active Lockdown");
                    tvActiveLockdownInstructions.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LockdownProtocolsActivity.this, "Failed to load lockdown data.", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void activateLockdown() {
        String title = etLockdownTitle.getText().toString().trim();
        String instructions = etLockdownInstructions.getText().toString().trim();
        String campus = spinnerLockdownCampus.getSelectedItem().toString();
        String severity = spinnerLockdownSeverity.getSelectedItem().toString();
        long timestamp = System.currentTimeMillis();

        if (TextUtils.isEmpty(title)) {
            etLockdownTitle.setError("Title is required");
            return;
        }
        if (TextUtils.isEmpty(instructions)) {
            etLockdownInstructions.setError("Instructions are required");
            return;
        }

        Map<String, Object> lockdownData = new HashMap<>();
        lockdownData.put("title", title);
        lockdownData.put("instructions", instructions);
        lockdownData.put("campus", campus);
        lockdownData.put("severity", severity);
        lockdownData.put("timestamp", timestamp);

        lockdownRef.setValue(lockdownData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Lockdown activated.", Toast.LENGTH_SHORT).show();
                etLockdownTitle.setText("");
                etLockdownInstructions.setText("");
                spinnerLockdownCampus.setSelection(0);
                spinnerLockdownSeverity.setSelection(0);
            } else {
                Toast.makeText(this, "Failed to activate lockdown.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void confirmDeactivateLockdown() {
        new AlertDialog.Builder(this)
                .setTitle("Deactivate Lockdown?")
                .setMessage("Are you sure you want to end the lockdown?")
                .setPositiveButton("Yes", (dialog, which) -> deactivateLockdown())
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void deactivateLockdown() {
        lockdownRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Lockdown deactivated.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to deactivate lockdown.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
