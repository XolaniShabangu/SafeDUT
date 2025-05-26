package com.example.safedut;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private EditText nameEditText, studentNumberEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private RadioGroup roleRadioGroup;
    private RadioButton studentRadioButton, staffRadioButton;
    private Spinner  spinnerCampus;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public RegisterFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_register, container, false);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find views
        nameEditText = rootView.findViewById(R.id.nameEditText);
        studentNumberEditText = rootView.findViewById(R.id.studentNumberEditText);
        emailEditText = rootView.findViewById(R.id.emailEditText);
        passwordEditText = rootView.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = rootView.findViewById(R.id.confirmPasswordEditText);
        registerButton = rootView.findViewById(R.id.registerButton);
        roleRadioGroup = rootView.findViewById(R.id.roleRadioGroup);
        studentRadioButton = rootView.findViewById(R.id.studentRadioButton);
        staffRadioButton = rootView.findViewById(R.id.staffRadioButton);
        spinnerCampus = rootView.findViewById(R.id.spinnerCampus);


        studentRadioButton.setChecked(true);


        registerButton.setOnClickListener(v -> registerUser());


        String[] campuses = {"Ritson Campus", "City Campus", "Steve Biko Campus", "ML Sultan Campus"};
        ArrayAdapter<String> campusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                campuses
        );
        spinnerCampus.setAdapter(campusAdapter);


        return rootView;
    }

    private void registerUser() {
        String name = nameEditText.getText().toString();
        String studentNumber = studentNumberEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String campus = spinnerCampus.getSelectedItem().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Basic validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(studentNumber) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();


                            String role = "student";
                            if (staffRadioButton.isChecked()) {
                                role = "staff";
                            }


                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("studentNumber", studentNumber);
                            userData.put("email", email);
                            userData.put("role", role);
                            userData.put("campus", campus);

                            // Saving data to Realtime Database
                            DatabaseReference userRef = mDatabase.child("users").child(userId);
                            userRef.setValue(userData)
                                    .addOnSuccessListener(aVoid -> {

                                        Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), DashboardActivity.class));
                                        getActivity().finish();
                                    })
                                    .addOnFailureListener(e -> {

                                        Toast.makeText(getActivity(), "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {

                        Toast.makeText(getActivity(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
