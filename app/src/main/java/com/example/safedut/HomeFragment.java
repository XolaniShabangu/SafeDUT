package com.example.safedut;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.content.pm.PackageManager;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;


public class HomeFragment extends Fragment {


    private GoogleMap mMap;
    private static final LatLng CAMPUS_CENTER = new LatLng(-29.854676, 31.010047);
    //private Marker userMarker;
    private TextView tvWelcome;
    private RecyclerView rvHomeNotices;
    private HomeNoticeAdapter noticeAdapter;
    private List<Notice> noticeList = new ArrayList<>();

    private boolean isHolding = false;
    private int progress = 0;
    private Handler handler = new Handler();
    private Runnable progressRunnable;
    private FusedLocationProviderClient fusedLocationClient;

    private LinearLayout lockdownContainer;
    private TextView tvLockdownTitle, tvLockdownCampus, tvLockdownInstructions;

    private DatabaseReference lockdownRef;







    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u != null) {

            String name = u.getDisplayName();
            if (name != null && !name.isEmpty()) {
                tvWelcome.setText("Welcome, " + name);
            } else {

                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(u.getUid())
                        .child("name")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                                String fetched = snap.exists()
                                        ? snap.getValue(String.class)
                                        : "there";
                                tvWelcome.setText("Welcome, " + fetched);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError err) {
                                tvWelcome.setText("Welcome");
                            }
                        });
            }
        }



        // ─── Notices ────────────────────────────────────────────────────────
        rvHomeNotices = view.findViewById(R.id.rvHomeNotices);
        rvHomeNotices.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        noticeAdapter = new HomeNoticeAdapter(noticeList);
        rvHomeNotices.setAdapter(noticeAdapter);


        FirebaseDatabase.getInstance()
                .getReference("notices")
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        noticeList.clear();
                        for (DataSnapshot child: snap.getChildren()) {
                            Notice n = child.getValue(Notice.class);
                            if (n != null && !n.resolved) {
                                noticeList.add(n);
                            }
                        }
                        noticeAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError err) { }
                });


        // ─── ──────────────────────────────────────────────────────
        lockdownContainer = view.findViewById(R.id.lockdownContainer);
        tvLockdownTitle = view.findViewById(R.id.tvLockdownTitle);
        tvLockdownCampus = view.findViewById(R.id.tvLockdownCampus);
        tvLockdownInstructions = view.findViewById(R.id.tvLockdownInstructions);
        tvLockdownReminder     = view.findViewById(R.id.tvLockdownReminder);


        lockdownRef = FirebaseDatabase.getInstance().getReference("lockdown");
        listenForLockdown();


// ─── Panic Button Logic ─────────────────────────────────────────────────
        Button panicButton = view.findViewById(R.id.panicButton);
        CircularProgressIndicator progressIndicator = view.findViewById(R.id.holdProgress);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());


        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isHolding) {
                    progress += 5;
                    progressIndicator.setProgress(progress);

                    if (progress >= 100) {
                        isHolding = false;


                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);

                        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if (locationResult != null && locationResult.getLocations().size() > 0) {
                                    Location location = locationResult.getLastLocation();
                                    // Now you can use the location object to get latitude and longitude
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    long timestamp = System.currentTimeMillis();
                                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                    PanicAlert alert = new PanicAlert(userId, latitude, longitude, timestamp);
                                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("panic_alerts");
                                    dbRef.push().setValue(alert)
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(getContext(), "Alert sent: HELP IS COMING", Toast.LENGTH_LONG).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(getContext(), "Location not available", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, Looper.getMainLooper());

                    }
                    handler.postDelayed(this, 150);
                }
            }
        };

        panicButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isHolding = true;
                    progress = 0;
                    progressIndicator.setProgress(0);
                    handler.post(progressRunnable);
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isHolding = false;
                    handler.removeCallbacks(progressRunnable);
                    progressIndicator.setProgress(0); // reset
                    return true;
            }
            return false;
        });




        fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(requireActivity());


        SupportMapFragment mapFrag = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);

        mapFrag.getMapAsync(googleMap -> {
            mMap = googleMap;


            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(CAMPUS_CENTER, 16f));

            enableMyLocation();
            startLocationUpdates();

            // ──  ────────────────────────────────

            LatLng ritson      = new LatLng(-29.851087, 31.006860);
            LatLng cityCampus  = new LatLng(-29.861068, 31.010587);
            LatLng steveBiko   = new LatLng(-29.853657, 31.005880);
            LatLng mlSultan    = new LatLng(-29.849961, 31.010277);

            mMap.addMarker(new MarkerOptions()
                    .position(ritson)
                    .title("Ritson Campus")
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)));

            mMap.addMarker(new MarkerOptions()
                    .position(cityCampus)
                    .title("City Campus")
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)));

            mMap.addMarker(new MarkerOptions()
                    .position(steveBiko)
                    .title("Steve Biko Campus")
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)));

            mMap.addMarker(new MarkerOptions()
                    .position(mlSultan)
                    .title("ML Sultan Campus")
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED)));
        });

        return view;
    }

    // ——————————————————————————————————————————————————————————————

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] perms,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1001
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            Toast.makeText(requireContext(),
                    "Location permission denied",
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void startLocationUpdates() {
        LocationRequest req = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(
                req,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(
                            @NonNull LocationResult result) {
                        Location loc = result.getLastLocation();
                        if (loc == null || mMap == null) return;

                        LatLng me = new LatLng(
                                loc.getLatitude(),
                                loc.getLongitude()
                        );

//                        if (userMarker == null) {
//                            userMarker = mMap.addMarker(new MarkerOptions()
//                                    .position(me)
//                                    .title("You"));
//                        } else {
//                            userMarker.setPosition(me);
//                        }
                    }
                },
                requireActivity().getMainLooper()
        );
    }





    private TextView tvLockdownReminder;
    private ValueEventListener lockdownListener;

    private void listenForLockdown() {
        lockdownListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {

                    lockdownContainer.setVisibility(View.GONE);
                    tvLockdownReminder.setVisibility(View.GONE);
                    return;
                }


                String title        = snapshot.child("title").getValue(String.class);
                String instructions = snapshot.child("instructions").getValue(String.class);
                String campus       = snapshot.child("campus").getValue(String.class);
                String severity     = snapshot.child("severity").getValue(String.class);

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    lockdownContainer.setVisibility(View.GONE);
                    tvLockdownReminder.setVisibility(View.GONE);
                    return;
                }

                // fetch the user’s registered campus
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(user.getUid())
                        .child("campus");

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnap) {
                        String userCampus = userSnap.getValue(String.class);
                        boolean isAtUserCampus = "All Campuses".equals(campus)
                                || campus.equals(userCampus);

                        if (isAtUserCampus) {
                            // show the full lockdown card
                            tvLockdownTitle.setText(title);
                            tvLockdownCampus.setText("Campus: " + campus);
                            tvLockdownInstructions.setText(instructions);

                            int colorRes;
                            switch (severity) {
                                case "High":
                                    colorRes = android.R.color.holo_red_light;
                                    break;
                                case "Medium":
                                    colorRes = android.R.color.holo_orange_light;
                                    break;
                                case "Low":
                                    colorRes = android.R.color.holo_green_light;
                                    break;
                                default:
                                    colorRes = android.R.color.darker_gray;
                            }

                            lockdownContainer.setBackgroundColor(
                                    ContextCompat.getColor(requireContext(), colorRes)
                            );

                            lockdownContainer.setVisibility(View.VISIBLE);
                            tvLockdownReminder.setVisibility(View.GONE);
                        } else {

                            lockdownContainer.setVisibility(View.GONE);
                            tvLockdownReminder.setText(
                                    "Lockdown at " + campus + "! Stay away!"
                            );
                            tvLockdownReminder.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        lockdownContainer.setVisibility(View.GONE);
                        tvLockdownReminder.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load lockdown data.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        lockdownRef.addValueEventListener(lockdownListener);
    }






    @Override
    public void onDestroyView() {
        super.onDestroyView();


        if (lockdownListener != null) {
            lockdownRef.removeEventListener(lockdownListener);
            lockdownListener = null;
        }
    }



}
