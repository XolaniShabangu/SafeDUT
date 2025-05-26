
package com.example.safedut;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;



import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.Toast;



public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MapActivity", "onCreate started");
        Log.d("MapActivity", "Intent: " + getIntent());


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

         lat = getIntent().getDoubleExtra("latitude", -1);
         lng = getIntent().getDoubleExtra("longitude", -1);

        Log.d("MapActivity", "Latitude: " + lat + ", Longitude: " + lng);

        if (lat == -1 || lng == -1) {
            Toast.makeText(this, "Invalid location data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } catch (Exception e) {
            Log.e("MapActivity", "Error initializing map: " + e.getMessage());
            Toast.makeText(this, "Map failed to load", Toast.LENGTH_SHORT).show();
        }


        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(this, "Invalid location received", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (lat == 0.0 && lng == 0.0) {

            Toast.makeText(this, "Invalid location received", Toast.LENGTH_SHORT).show();
        } else {
            LatLng location = new LatLng(lat, lng);
            googleMap.addMarker(new MarkerOptions().position(location).title("Panic Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17));
        }
    }
}
