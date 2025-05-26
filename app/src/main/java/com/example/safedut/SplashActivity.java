//package com.example.safedut;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import androidx.appcompat.app.AppCompatActivity;
//import android.Manifest;
//import android.content.pm.PackageManager;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import android.widget.Toast;
//import android.content.Intent;
//
//
//import com.google.firebase.FirebaseApp;
//
//public class SplashActivity extends AppCompatActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        FirebaseApp.initializeApp(this);
//        setContentView(R.layout.activity_splash);
//
//        // First: Check permission(LOCATION)
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    1001);
//        } else {
//            // Permission already granted, continue
//            goToLogin();
//        }
//    }
//
//
//
//    private void checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    1001);
//        }
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1001) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
//            }
//            // Either way, continue to login (optional: block if denied)
//            goToLogin();
//        }
//    }
//
//    private void goToLogin() {
//        new Handler().postDelayed(() -> {
//            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        }, 2000);
//    }
//
//
//
//
//}
//


package com.example.safedut;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_splash);

        // Check permissions (Location and Notification)
        checkPermissionsAndProceed();
    }


    private void checkPermissionsAndProceed() {

        boolean locationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;


        boolean notificationPermissionGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }


        if (!locationPermissionGranted || !notificationPermissionGranted) {
            List<String> permissionsToRequest = new ArrayList<>();


            if (!locationPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }


            if (!notificationPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }

            // Request the permissions
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    1001);
        } else {

            goToLogin();
            startLockdownService();


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            boolean locationPermissionGranted = false;
            boolean notificationPermissionGranted = false;


            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }


                if (permissions[i].equals(Manifest.permission.POST_NOTIFICATIONS) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionGranted = true;
                }
            }


            if (locationPermissionGranted && notificationPermissionGranted) {
                goToLogin();
            } else {

                Toast.makeText(this, "Both permissions are required to proceed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToLogin() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }

    private void startLockdownService() {
        Intent serviceIntent = new Intent(this, LockdownListenerService.class);
        startService(serviceIntent);
    }
}
