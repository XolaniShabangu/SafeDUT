package com.example.safedut;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LockdownListenerService extends Service {

    private static final String CHANNEL_ID = "lockdown_alerts_channel";
    private DatabaseReference lockdownRef;

    @Override
    public void onCreate() {
        super.onCreate();
        lockdownRef = FirebaseDatabase.getInstance().getReference("lockdown");
        createNotificationChannel();
        listenForLockdownChanges();
    }

    private void listenForLockdownChanges() {
        lockdownRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lockdown is active
                    String title = snapshot.child("title").getValue(String.class);
                    String instructions = snapshot.child("instructions").getValue(String.class);
                    String campus = snapshot.child("campus").getValue(String.class);
                    String severity = snapshot.child("severity").getValue(String.class);

                    if (title != null && instructions != null) {
                        String notificationTitle = "🚨 Lockdown Activated at" + campus;
                        String notificationMessage = severity + "Alert - " + instructions;

                        sendNotification(notificationTitle, notificationMessage);
                    }
                } else {

                    String notificationTitle = "✅ Lockdown Deactivated";
                    String notificationMessage = "The lockdown has been lifted. Proceed as normal.";
                    sendNotification(notificationTitle, notificationMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LockdownService", "Database error: " + error.getMessage());
            }
        });
    }


    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, DashboardActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE  // Updated flags
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lockdown Alerts";
            String description = "Notifications for lockdown events";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
