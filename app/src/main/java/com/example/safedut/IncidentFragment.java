package com.example.safedut;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.FirebaseDatabase;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;
import com.example.safedut.IncidentReport;
import com.example.safedut.ReportAdapter;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;
import android.Manifest;
import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class IncidentFragment extends Fragment {

    private static final int PICK_MEDIA_REQUEST = 1001;
    private static final int STORAGE_PERMISSION_CODE = 123;
    private static final int LOCATION_PERMISSION_CODE = 124;

    private Spinner spinnerReportType;
    private EditText editDescription, editLocation;
    private Button btnUploadMedia, btnSubmit, btnGetLocation;
    private TextView mediaSelectedText, locationStatus;
    private CheckBox checkboxAnonymous;

    private Uri mediaUri = null;
    private FirebaseUser currentUser;
    private FusedLocationProviderClient fusedLocationClient;

    public IncidentFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incident, container, false);

        spinnerReportType = view.findViewById(R.id.spinnerReportType);
        editDescription = view.findViewById(R.id.editDescription);
        editLocation = view.findViewById(R.id.editLocation);
        btnUploadMedia = view.findViewById(R.id.btnUploadMedia);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnGetLocation = view.findViewById(R.id.btnGetLocation);
        mediaSelectedText = view.findViewById(R.id.mediaSelectedText);
        locationStatus = view.findViewById(R.id.locationStatus);
        checkboxAnonymous = view.findViewById(R.id.checkboxAnonymous);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());


        requestStoragePermission();

        String[] reportTypes = {"Theft", "Assault", "Suspicious Activity", "Vandalism", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, reportTypes);
        spinnerReportType.setAdapter(adapter);

        btnUploadMedia.setOnClickListener(v -> openFileChooser());

        btnGetLocation.setOnClickListener(v -> fetchLocation());

        btnSubmit.setOnClickListener(v -> {
            String reportType = spinnerReportType.getSelectedItem().toString();
            String description = editDescription.getText().toString().trim();
            String location = editLocation.getText().toString().trim();
            boolean isAnonymous = checkboxAnonymous.isChecked();

            if (description.isEmpty()) {
                editDescription.setError("Description required");
                return;
            }

            if (location.isEmpty()) {
                editLocation.setError("Location required");
                return;
            }

            if (mediaUri != null) {
                convertAndSaveAsBase64(reportType, description, location, isAnonymous);
            } else {
                saveReportToRealtimeDB(reportType, description, location, null, isAnonymous);
            }
        });

        layoutReports = view.findViewById(R.id.layoutReports);
        btnToggleReports = view.findViewById(R.id.btnToggleReports);
        reportsRecycler = view.findViewById(R.id.reportsRecycler);
        reportsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        btnToggleReports.setOnClickListener(v -> {
            if (layoutReports.getVisibility() == View.GONE) {
                layoutReports.setVisibility(View.VISIBLE);
                btnToggleReports.setText("Hide Reports ⬆");
                loadUserReports();
            } else {
                layoutReports.setVisibility(View.GONE);
                btnToggleReports.setText("Your Reports ⬇");
            }
        });

        return view;
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_MEDIA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            mediaUri = data.getData();
            mediaSelectedText.setText("Selected: " + mediaUri.getLastPathSegment());
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        }
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();


                    String locationText = "Lat: " + latitude + ", Lon: " + longitude;
                    editLocation.setText(locationText);

                    // Convert to readable address
                    getAddressFromLocation(latitude, longitude);

                } else {
                    locationStatus.setText("Location not available ❌");
                }
            });
        } else {
            locationStatus.setText("Location permission not granted ❌");
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                editLocation.setText(addressText);
                locationStatus.setText("Location fetched successfully ✅");
            } else {
                locationStatus.setText("Address not found ❌");
            }
        } catch (IOException e) {
            locationStatus.setText("Error fetching address ❌");
            e.printStackTrace();
        }
    }


    private void convertAndSaveAsBase64(String reportType, String description, String location, boolean isAnonymous) {
        try {
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(mediaUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
            byte[] imageBytes = baos.toByteArray();

            String base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            if (base64String.length() > 260000) {
                Toast.makeText(getContext(), "Image too large. Please choose a smaller one.", Toast.LENGTH_LONG).show();
                return;
            }

            saveReportToRealtimeDB(reportType, description, location, base64String, isAnonymous);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveReportToRealtimeDB(String reportType, String description, String location, @Nullable String mediaBase64, boolean isAnonymous) {
        String userId = (currentUser != null && !isAnonymous) ? currentUser.getUid() : null;
        long timestamp = System.currentTimeMillis();
        String reportId = UUID.randomUUID().toString();

        IncidentReport report = new IncidentReport(reportType, description, mediaBase64, location, userId, isAnonymous, false, timestamp);
        report.reportId = reportId;

        FirebaseDatabase.getInstance().getReference("incident_reports").child(reportId).setValue(report)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Report submitted successfully", Toast.LENGTH_LONG).show();
                    resetForm();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show());
    }

    private LinearLayout layoutReports;
    private Button btnToggleReports;
    private RecyclerView reportsRecycler;
    private ReportAdapter reportAdapter;
    private List<IncidentReport> userReports = new ArrayList<>();

    private void loadUserReports() {
        userReports.clear();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();


        FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot nameSnapshot) {
                        String displayName = nameSnapshot.exists() ? nameSnapshot.getValue(String.class) : "You";

                        // 🔁 Now load the reports
                        FirebaseDatabase.getInstance().getReference("incident_reports")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot snap : snapshot.getChildren()) {
                                            IncidentReport report = snap.getValue(IncidentReport.class);
                                            if (report != null && !report.anonymous && uid.equals(report.submittedBy)) {
                                                report.displayName = displayName; // 🔐 inject display name
                                                userReports.add(report);
                                            }
                                        }
                                        reportAdapter = new ReportAdapter(userReports);
                                        reportsRecycler.setAdapter(reportAdapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), "Failed to load reports.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Could not fetch user name.", Toast.LENGTH_SHORT).show();
                    }
                });

    }



    private void resetForm() {
        editDescription.setText("");
        editLocation.setText("");
        mediaSelectedText.setText("No file selected");
        checkboxAnonymous.setChecked(false);
        locationStatus.setText("");
        mediaUri = null;
        loadUserReports();
    }
}
