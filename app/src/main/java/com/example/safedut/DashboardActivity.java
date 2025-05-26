package com.example.safedut;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.net.Uri;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;






public class DashboardActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private String userRole;
    private String[] tabTitles = {"Home", "Incident", "Chat", "Safety", "More"};
    private int[] tabIcons = {R.drawable.ic_home_selector, R.drawable.ic_incident_selector, R.drawable.ic_chat_selector, R.drawable.ic_safety_selector, R.drawable.ic_more_selector};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // 🧠 Get role from intent
        userRole = getIntent().getStringExtra("userRole");

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String role = prefs.getString("userRole", "");

        if ("staff".equals(role)) {
            listenForPanicAlerts();


        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "geofence_channel_id",
                    "Geofence Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for geofence transitions");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent serviceIntent = new Intent(this, LockdownListenerService.class);
        startService(serviceIntent);





        // adapter with role-based fragments
        DashboardPagerAdapter adapter = new DashboardPagerAdapter(this, userRole);
        viewPager.setAdapter(adapter);


        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {

            View tabView = LayoutInflater.from(DashboardActivity.this).inflate(R.layout.tab_item, null);


            ImageView tabIcon = tabView.findViewById(R.id.tabIcon);
            tabIcon.setImageResource(tabIcons[position]);

            TextView tabText = tabView.findViewById(R.id.tabText);
            tabText.setText(tabTitles[position]);

            tab.setCustomView(tabView);


            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab selectedTab) {

                    ImageView icon = selectedTab.getCustomView().findViewById(R.id.tabIcon);
                    TextView text = selectedTab.getCustomView().findViewById(R.id.tabText);

                    // icon color (highlighted)
                    icon.setColorFilter(getResources().getColor(R.color.selectedColor));
                    text.setTextColor(getResources().getColor(R.color.selectedColor));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab unselectedTab) {

                    ImageView icon = unselectedTab.getCustomView().findViewById(R.id.tabIcon);
                    TextView text = unselectedTab.getCustomView().findViewById(R.id.tabText);


                    icon.setColorFilter(getResources().getColor(R.color.unselectedColor));
                    text.setTextColor(getResources().getColor(R.color.unselectedColor));
                }

                @Override
                public void onTabReselected(TabLayout.Tab reselectedTab) {

                }
            });



        }).attach();


    }

    //
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveLastSeenTimestamp(long timestamp) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        Log.d("PanicDebug", "Saving lastSeenTimestamp = " + lastSeenTimestamp);

        prefs.edit().putLong("lastSeenAlertTime", timestamp).commit(); // ← use commit to ensure sync
    }


    private long getLastSeenTimestamp() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getLong("lastSeenAlertTime", 0);
    }

    private long lastSeenTimestamp = 0;
    private String lastAlertKey = "";


    private void listenForPanicAlerts() {
        lastSeenTimestamp = getLastSeenTimestamp();

        DatabaseReference alertRef = FirebaseDatabase.getInstance().getReference("panic_alerts");

        alertRef.addChildEventListener(new ChildEventListener() {

@Override
public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
    String alertKey = snapshot.getKey(); // 🔑 Unique alert key
    PanicAlert alert = snapshot.getValue(PanicAlert.class);

    if (alert != null && alert.getTimestamp() > lastSeenTimestamp && !alertKey.equals(lastAlertKey)) {
        lastSeenTimestamp = alert.getTimestamp();
        saveLastSeenTimestamp(lastSeenTimestamp);
        lastAlertKey = alertKey; // ✅ store last key
        showLocalNotification(alert);

        Log.d("PanicDebug", "New alert notified: " + alertKey);
    } else {
        Log.d("PanicDebug", "Duplicate or old alert skipped: " + alertKey);
    }
}




            @Override public void onChildChanged(DataSnapshot snapshot, String previousChildName) {}
            @Override public void onChildRemoved(DataSnapshot snapshot) {}
            @Override public void onChildMoved(DataSnapshot snapshot, String previousChildName) {}
            @Override public void onCancelled(DatabaseError error) {}
        });
    }




    private void showLocalNotification(PanicAlert alert) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamVolume(
                    AudioManager.STREAM_NOTIFICATION,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
                    0
            );
        }

        String channelId = "panic_alert_channel_v2";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alert_sound);
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Panic Alerts", NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for panic alerts with custom sound");
            channel.setSound(soundUri, null);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // 🔗 Intent to open MapActivity with location
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("latitude", alert.getLatitude());
        intent.putExtra("longitude", alert.getLongitude());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle("🚨 Panic Alert Received!")
                .setContentText("Tap to view location on map.")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 1000, 500, 1000});

        NotificationManagerCompat.from(this).notify(1, builder.build());

        Log.d("PanicDebug", "Notifying with: "
                + "lat=" + alert.getLatitude()
                + ", lng=" + alert.getLongitude());

    }





    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private static final String TAG = "Geofencing";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int BACKGROUND_LOCATION_REQUEST_CODE = 2001;
    private void setupGeofence() {
        double dutLatitude = -29.862501;
        double dutLongitude = 31.016399;
        float radius = 100;

        Geofence geofence = new Geofence.Builder()
                .setRequestId("DUT_GEOFENCE")
                .setCircularRegion(dutLatitude, dutLongitude, radius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        geofencePendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                new Intent(this, GeofenceBroadcastReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Geofence added successfully");
                        Toast.makeText(this, "Geofence set for DUT 🚧", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to add geofence", e);
                        Toast.makeText(this, "Failed to set geofence: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }









}
